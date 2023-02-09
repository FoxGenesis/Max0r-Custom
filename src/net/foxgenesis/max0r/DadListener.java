package net.foxgenesis.max0r;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.config.fields.BooleanField;
import net.foxgenesis.watame.util.DiscordUtils;

public class DadListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Dad Listener");

	private static final BooleanField enabled = new BooleanField("max0r.dad.enabled", guild -> true, true);

	private static final Pattern regex = Pattern.compile("\\bi[']?m\\s(\\S*)", Pattern.CASE_INSENSITIVE);

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.optFrom(guild)) {
				Member member = event.getMember();
				Message message = event.getMessage();
				Matcher matcher = regex.matcher(message.getContentStripped());

				if (matcher.find() && DiscordUtils.getBotMember(guild).canInteract(member)) {
					String toChange = matcher.group(1);

					logger.trace("Changing {}'s Nickname to {} because of dad functionality", member, toChange);

					member.modifyNickname(toChange).reason("Dad function").queue();
				}
			}
		}
	}
}
