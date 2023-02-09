package net.foxgenesis.max0r;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.config.fields.BooleanField;

public class SpazDickListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Spaz Dick Listener");

	private static final BooleanField enabled = new BooleanField("max0r.spazdick.enabled", guild -> true, true);

	private static final Pattern regex = Pattern.compile("spaz dick", Pattern.CASE_INSENSITIVE);

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.optFrom(guild)) {
				Message message = event.getMessage();
				Matcher matcher = regex.matcher(message.getContentStripped());

				if (matcher.find()) {
					message.reply("https://cdn.discordapp.com/attachments/398590278404931588/1072984934702133278/Screenshot_20221007_174238.jpg").queue();
				}
			}
		}
	}
}
