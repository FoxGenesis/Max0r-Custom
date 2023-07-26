package net.foxgenesis.max0r.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.util.ResourceUtils;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;

public class RandomCats extends ListenerAdapter {

	private final URL catURL;

	public RandomCats() {
		try {
			catURL = new URL("https://api.thecatapi.com/v1/images/search");
		} catch (MalformedURLException e) {
			throw new SeverePluginException(e);
		}
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				event.deferReply().queue();

				try {
					MessageEmbed embed = new EmbedBuilder().setColor(Colors.INFO).setTitle("Meow!")
							.setImage(getRandomCatImage()).build();

					event.getHook().editOriginalEmbeds(embed).queue();

				} catch (Exception e) {
					e.printStackTrace();
					event.getHook().editOriginalEmbeds(Response.error("An error occured. Please try again later."))
							.queue();
				}
			}
		}
	}

	private String getRandomCatImage() throws IOException {
		String response = ResourceUtils.toString(catURL.openStream());

		JSONArray array = new JSONArray(response);

		return array.getJSONObject(0).getString("url");
	}
}
