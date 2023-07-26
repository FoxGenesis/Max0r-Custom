package net.foxgenesis.max0r.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.util.Response;

public class RandomCats extends ListenerAdapter {

	private final URL catURL;

	public RandomCats() {
		try {
			catURL = new URL("https://cataas.com/cat/cute");
		} catch (MalformedURLException e) {
			throw new SeverePluginException(e);
		}
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				event.deferReply().queue();
				try (FileUpload upload = FileUpload.fromData(catURL.openStream(), "cute cat.png")) {
					event.getHook().sendFiles(upload).queue();
				} catch (IOException e) {
					e.printStackTrace();
					event.getHook().editOriginalEmbeds(Response.error("An error occured. Please try again later."))
							.queue();
				}
			}
		}
	}
}
