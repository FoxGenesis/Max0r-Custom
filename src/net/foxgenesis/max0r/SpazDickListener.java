package net.foxgenesis.max0r;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.property.IPropertyField;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;

public class SpazDickListener extends ListenerAdapter {
	private static final IPropertyField<String, Guild, IGuildPropertyMapping> enabled = WatameBot.getInstance()
			.getPropertyProvider().getProperty("max0r-spazdick-enabled");

	private static final Pattern regex = Pattern.compile("spaz dick", Pattern.CASE_INSENSITIVE);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.get(guild, false, IGuildPropertyMapping::getAsBoolean)) {
				Message message = event.getMessage();
				Matcher matcher = regex.matcher(message.getContentStripped());

				if (matcher.find()) {
					message.reply("https://cdn.discordapp.com/attachments/398590278404931588/1072984934702133278/Screenshot_20221007_174238.jpg").queue();
				}
			}
		}
	}
}
