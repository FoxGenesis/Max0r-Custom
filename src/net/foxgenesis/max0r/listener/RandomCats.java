package net.foxgenesis.max0r.listener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.foxgenesis.cats.Size;
import net.foxgenesis.cats.TheCatAPI;
import net.foxgenesis.cats.bean.CatPicture;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import okhttp3.OkHttpClient;

public class RandomCats extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RandomCats.class);

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
				String breed = event.getOption("breed", OptionMapping::getAsString);
				String[] breeds = breed != null ? new String[] { breed } : null;

				api.search(client, Size.SMALL, null, null, 0, 1, null, breeds, false, true, true)
						.whenComplete((list, e) -> {
							if (e != null) {
								event.replyEmbeds(Response.error("An error occured. Please try again later.")).queue();
								logger.error("Error occured during api request", e);
							} else {
								CatPicture first = list[0];

								// Construct embed
								EmbedBuilder builder = new EmbedBuilder();
								builder.setColor(Colors.INFO);
								builder.setTitle("Meow!");
								builder.setImage(first.getUrl());
								builder.setFooter("via thecatapi.com");

								// Send response
								event.replyEmbeds(builder.build()).queue();
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
						api.getBreedList(client).whenComplete((breeds, e) -> {
							if (e == null) {
								String option = event.getFocusedOption().getValue().toLowerCase();

								List<Command.Choice> options = Arrays.stream(breeds)
										.filter(breed -> breed.getName().toLowerCase().startsWith(option))
										.map(breed -> new Command.Choice(breed.getName(), breed.getId())).limit(25)
										.toList();

								event.replyChoices(options).queue();
							} else {
								logger.error("Error occured during api request", e);
							}
						});
					}
				}
			}
		}
	}
}
