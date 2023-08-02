package net.foxgenesis.max0r.listener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.foxgenesis.cats.Size;
import net.foxgenesis.cats.TheCatAPI;
import net.foxgenesis.cats.bean.Breed;
import net.foxgenesis.cats.bean.CatPicture;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.OkHttpClient;

public class RandomCats extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RandomCats.class);

	private static final Emoji EMOJI_HAPPY = Emoji.fromCustom("happeh", 478378484025131010L, false);
	private static final String FIELD_FORMAT = "**%s:** %s\n";
	private static final String FLAG_FORMAT = ":flag_%s:";

	private final TheCatAPI api;
	private final OkHttpClient client;

	public RandomCats(String apiKey) {
		api = new TheCatAPI(apiKey);

		client = new OkHttpClient().newBuilder().readTimeout(3, TimeUnit.SECONDS).callTimeout(3, TimeUnit.SECONDS)
				.connectTimeout(3, TimeUnit.SECONDS).build();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				String breedOption = event.getOption("breed", OptionMapping::getAsString);
				String[] breeds = breedOption != null ? new String[] { breedOption } : null;

				event.deferReply().queue();

				api.search(client, Size.SMALL, null, null, 0, 1, null, breeds, false, true, true)
						.whenCompleteAsync((list, e) -> {
							if (e != null) {
								event.replyEmbeds(Response.error("An error occured. Please try again later.")).queue();
								logger.error("Error occured during api request", e);
							} else {
								try {
									// Send response
									event.getHook().editOriginalEmbeds(createCatEmbed(list[0]))
											.flatMap(message -> message.addReaction(EMOJI_HAPPY)).queue();
								} catch (Exception e2) {
									event.replyEmbeds(Response.error("An error occured. Please try again later."))
											.queue();
									logger.error("Error occured during api request", e2);
								}
							}
						});
			}
		}
	}

	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				switch (event.getFocusedOption().getName()) {
					case "breed" -> {
						// Get all cat breeds
						api.getBreedList(client).whenCompleteAsync((breeds, e) -> {
							if (e == null) {
								// Construct a stream of breeds with all breed names and their alternative names
								Stream<Pair<String, Breed>> stream = Arrays.stream(breeds)
										.mapMulti((Breed breed, Consumer<Pair<String, Breed>> consumer) -> {
											// Add breed name
											consumer.accept(new Pair<String, Breed>(breed.getName(), breed));

											// Add all alternative names if present
											boolean hasAltNames = !(breed.getAlt_names() == null
													|| breed.getAlt_names().trim().isBlank());
											if (hasAltNames)
												for (String name : breed.getAlt_names().split("[,/]"))
													consumer.accept(new Pair<String, Breed>(name.trim(), breed));
										}).sorted((a, b) -> a.key.compareTo(b.key));

								// Filter stream if user has typed something
								String option = event.getFocusedOption().getValue();
								if (!(option == null || option.isBlank())) {
									String o = option.toLowerCase();
									stream = stream.filter(pair -> pair.key.toLowerCase().contains(o));
								}

								// Map results to choices and reply
								List<Command.Choice> choices = stream
										.map(pair -> new Command.Choice(pair.key, pair.value.getId())).toList();
								event.replyChoices(choices.subList(0, Math.min(25, choices.size()))).queue();
							}
						}).whenCompleteAsync((v, e) -> {
							if (e != null)
								logger.error("Error occured during api request", e);
						});
					}
				}
			}
		}
	}

	private static MessageEmbed createCatEmbed(CatPicture cat) {
		// Construct embed
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Colors.INFO);
		// builder.setTitle(":cat: Meow!");
		builder.setImage(cat.getUrl());
		builder.setFooter("via thecatapi.com", "https://thecatapi.com/favicon.ico");

		// Append breed information if present
		if (cat.getBreeds() != null && cat.getBreeds().length > 0) {
			Breed breed = cat.getBreeds()[0];

			StringBuilder b = new StringBuilder();
			b.append(FIELD_FORMAT.formatted("Breed", breed.getName()));
			b.append(FIELD_FORMAT.formatted("Origin",
					FLAG_FORMAT.formatted(breed.getCountry_code().toLowerCase()) + " " + breed.getOrigin()));

			// Add alternative names if present
			if (!(breed.getAlt_names() == null || breed.getAlt_names().trim().isBlank()))
				b.append(FIELD_FORMAT.formatted("Alternative Names", breed.getAlt_names()));

			b.append(FIELD_FORMAT.formatted("Temperament", breed.getTemperament()));

			b.append("\n");
			b.append("[Wikipedia Page](" + breed.getWikipedia_url() + ")");

			builder.setDescription(b.toString().trim());
		}
		return builder.build();
	}

	private static record Pair<T1, T2>(T1 key, T2 value) {}
}
