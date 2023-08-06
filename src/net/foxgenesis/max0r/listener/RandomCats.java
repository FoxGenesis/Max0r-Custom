package net.foxgenesis.max0r.listener;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.foxgenesis.cats.Order;
import net.foxgenesis.cats.SearchRequest;
import net.foxgenesis.cats.TheCatAPI;
import net.foxgenesis.cats.bean.Breed;
import net.foxgenesis.cats.bean.CatPicture;
import net.foxgenesis.util.Pair;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.OkHttpClient;

public class RandomCats extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RandomCats.class);

	private static final Emoji EMOJI_HAPPY = Emoji.fromCustom("happeh", 478378484025131010L, false);

	private static final String FOOTER_TEXT = "via thecatapi.com";
	private static final String FOOTER_ICON = "https://thecatapi.com/favicon.ico";

	private static final String FIELD_FORMAT = "**%s:** %s\n";
	private static final String FLAG_FORMAT = ":flag_%s:";

	private final TheCatAPI api;
	private final OkHttpClient client;

	public RandomCats(String apiKey) {
		api = new TheCatAPI(apiKey);

		client = new OkHttpClient().newBuilder().callTimeout(3, TimeUnit.SECONDS).build();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		try {
			switch (event.getFullCommandName()) {
				case "cat search" -> {
					// Parse options
					String[] breeds = { event.getOption("breed", OptionMapping::getAsString) };

					// Create request
					SearchRequest.Builder.Default builder = new SearchRequest.Builder.Default();
					builder.setBreeds(breeds);

					// Search
					api.search(client, builder.build())
							.whenCompleteAsync((list, e) -> handleSearchResult(list, e, event));
				}
				case "cat search-server" -> {
					// Parse options
					String[] breeds = { event.getOption("breed", OptionMapping::getAsString) };
					String subid = Optional.ofNullable(event.getOption("user", OptionMapping::getAsMember))
							.map(Member::getId).orElse("");

					// Build request
					SearchRequest.Builder.Uploaded builder = new SearchRequest.Builder.Uploaded();
					builder.setBreeds(breeds);
					builder.setSubID(subid);
					builder.setOrder(Order.RANDOM);
					builder.setLimit(1);

					// Search
					api.search(client, builder.build())
							.whenCompleteAsync((list, e) -> handleSearchResult(list, e, event));
				}
				case "catupload" -> {
					Attachment attachment = event.getOption("file", OptionMapping::getAsAttachment);
					String subid = event.getMember().getId();

					event.deferReply().queue();

					// New client because we are doing a longer operation than normal
					OkHttpClient tempClient = client.newBuilder().callTimeout(10, TimeUnit.SECONDS).build();
					// Upload picture
					api.uploadPicture(tempClient, attachment, subid).orTimeout(10, TimeUnit.SECONDS)
							.whenCompleteAsync((response, e) -> {
								// Check for errors
								if (e != null) {
									event.getHook()
											.editOriginalEmbeds(
													Response.error("An error occured. Please try again later."))
											.queue();
									logger.error("Error occured during api request", e);
									return;
								}

								// Create embed
								EmbedBuilder builder = new EmbedBuilder();
								builder.setColor(response.isApproved() ? Colors.SUCCESS
										: response.isPending() ? Colors.WARNING : Colors.ERROR);
								builder.setTitle("Uploaded");
								builder.setImage(response.getUrl());
								builder.addField("Pending", "" + response.isPending(), true);
								builder.addField("Approved", "" + response.isApproved(), true);
								builder.setFooter(FOOTER_TEXT, FOOTER_ICON);

								// Display result
								event.getHook().editOriginalEmbeds(builder.build()).queue();
							});
				}
			}
		} catch (Exception e) {
			logger.error("Error in RandomCats", e);
			MessageEmbed embed = Response.error("An error occured. Please try again later.");

			if (event.isAcknowledged())
				event.getHook().editOriginalEmbeds(embed).setReplace(true).queue();
			else
				event.replyEmbeds(embed).queue();
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				switch (event.getFocusedOption().getName()) {
					case "breed" -> {
						// Get all cat breeds
						api.getBreedList(client).thenAcceptAsync(breeds -> {
							// Construct a stream of breeds with all breed names and their alternative names
							Stream<Pair<String, Breed>> stream = Arrays.stream(breeds)
									.mapMulti((Breed breed, Consumer<Pair<String, Breed>> consumer) -> {
										// Add breed name
										consumer.accept(new Pair<>(breed.getName(), breed));

										// Add all alternative names if present
										boolean hasAltNames = !(breed.getAlt_names() == null
												|| breed.getAlt_names().trim().isBlank());
										if (hasAltNames)
											for (String name : breed.getAlt_names().split("[,/]"))
												consumer.accept(new Pair<>(name.trim(), breed));
									}).sorted(Comparator.comparing(a -> a.key()));

							// Filter stream if user has typed something
							String option = event.getFocusedOption().getValue();
							if (!(option == null || option.isBlank())) {
								String o = option.toLowerCase();
								stream = stream.filter(pair -> pair.key().toLowerCase().contains(o));
							}

							// Map results to choices and reply
							List<Command.Choice> choices = stream
									.map(pair -> new Command.Choice(pair.key(), pair.value().getId())).toList();
							event.replyChoices(choices.subList(0, Math.min(25, choices.size()))).queue();
						}).whenCompleteAsync((v, e) -> {
							if (e != null)
								logger.error("Error occured during api request", e);
						});
					}
				}
			}
		}
	}

	private static void handleSearchResult(CatPicture[] list, Throwable e, IReplyCallback event) {
		// Check for errors
		if (e != null) {
			event.replyEmbeds(Response.error("An error occured. Please try again later.")).queue();
			logger.error("Error occured during api request", e);
			return;
		}
		if (list == null || list.length == 0) {
			event.replyEmbeds(Response.error("No image found")).queue();
			return;
		}

		// Get first result
		CatPicture cat = list[0];

		// Construct embed
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Colors.INFO);
		builder.setImage(cat.getUrl());
		builder.setFooter(FOOTER_TEXT, FOOTER_ICON);

		StringBuilder b = new StringBuilder();

		// Append breed information if present
		if (cat.getBreeds() != null && cat.getBreeds().length > 0) {
			Breed breed = cat.getBreeds()[0];

			b.append(FIELD_FORMAT.formatted("Breed", breed.getName()));
			b.append(FIELD_FORMAT.formatted("Origin",
					FLAG_FORMAT.formatted(breed.getCountry_code().toLowerCase()) + " " + breed.getOrigin()));

			// Add alternative names if present
			if (!(breed.getAlt_names() == null || breed.getAlt_names().trim().isBlank()))
				b.append(FIELD_FORMAT.formatted("Alternative Names", breed.getAlt_names()));

			b.append(FIELD_FORMAT.formatted("Temperament", breed.getTemperament()));

			if (!(breed.getWikipedia_url() == null || breed.getWikipedia_url().isBlank())) {
				b.append("\n");
				b.append("[Wikipedia Page](" + breed.getWikipedia_url() + ")");
			}
		}

		// Append discord user if present
		String subid = cat.getSub_id();
		if (!(subid == null || subid.isBlank())) {
			b.append("\n");

			int index = subid.indexOf(':');
			if (index == -1)
				index = subid.length();

			b.append(FIELD_FORMAT.formatted("Uploaded by", "<@" + subid.substring(0, index) + ">"));
		}

		// Build description
		builder.setDescription(b.toString().trim());

		// Send message
		event.replyEmbeds(builder.build())
				// Get sent message
				.flatMap(hook -> hook.retrieveOriginal())
				// Add happy emoji
				.flatMap(message -> message.addReaction(EMOJI_HAPPY)).queue();
	}
}
