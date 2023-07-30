package net.foxgenesis.max0r.listener;

import java.util.concurrent.TimeUnit;

import net.foxgenesis.cats.CatPicture;
import net.foxgenesis.cats.TheCatAPI;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;

public class RandomCats extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RandomCats.class);

	private final TheCatAPI api;
	private final OkHttpClient client;

	public RandomCats(String apiKey) {
		api = new TheCatAPI(apiKey);
		client = new OkHttpClient().newBuilder().callTimeout(3, TimeUnit.SECONDS).build();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				api.search(client, null, null, null, 0, 1, null, null, false, true, true).whenComplete((list, e) -> {
					if (e != null) {
						event.replyEmbeds(Response.error("An error occured. Please try again later.")).queue();
						logger.error("Error occured during api request", e);
					} else {
						CatPicture first = list.get(0);

						// Construct embed
						EmbedBuilder builder = new EmbedBuilder();
						builder.setColor(Colors.INFO);
						builder.setTitle("Meow!");
						builder.setImage(first.getURL());
						builder.setFooter("via thecatapi.com");

						// Send response
						event.replyEmbeds(builder.build()).queue();
					}
				});
			}
		}
	}
}
