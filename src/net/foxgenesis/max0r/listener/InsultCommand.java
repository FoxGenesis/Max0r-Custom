package net.foxgenesis.max0r.listener;

import net.foxgenesis.max0r.api.InsultApi;
import net.foxgenesis.watame.util.Response;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class InsultCommand extends ListenerAdapter implements AutoCloseable {
	private final InsultApi api;

	public InsultCommand() {
		api = new InsultApi();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		try {
			switch (event.getFullCommandName()) {
				case "insult" -> {
					event.deferReply().queue();

					api.getRandomInsult()
							.thenAcceptAsync(insult -> event.getHook().editOriginalEmbeds(Response.success(insult.getInsult())).queue())
							.whenCompleteAsync((v, e) -> {
								if (e != null) {
									MessageEmbed embed = Response.error("An error occured. Please try again later.");

									if (event.isAcknowledged())
										event.getHook().editOriginalEmbeds(embed).setReplace(true).queue();
									else
										event.replyEmbeds(embed).queue();
								}
							});
				}
			}
		} catch (Exception e) {
			MessageEmbed embed = Response.error("An error occured. Please try again later.");

			if (event.isAcknowledged())
				event.getHook().editOriginalEmbeds(embed).setReplace(true).queue();
			else
				event.replyEmbeds(embed).queue();
		}
	}

	@Override
	public void close() {
		api.close();
	}
}
