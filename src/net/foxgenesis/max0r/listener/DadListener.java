package net.foxgenesis.max0r.listener;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DadListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Dad Listener");

	private static final Pattern regex = Pattern.compile("\\bi[']?m\\s(\\S*)", Pattern.CASE_INSENSITIVE);

	private final PluginProperty enabled;

	public DadListener(PluginProperty enabled) {
		this.enabled = Objects.requireNonNull(enabled);
	}
	
	public DadListener(Plugin plugin, PluginPropertyProvider provider) {
		this.enabled = provider.upsertProperty(plugin, "dad.enabled", true, PropertyType.PLAIN);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.get(guild, () -> false, PropertyMapping::getAsBoolean)) {
				Member member = event.getMember();
				Message message = event.getMessage();
				Matcher matcher = regex.matcher(message.getContentStripped());

				if (matcher.find() && guild.getSelfMember().canInteract(member)) {
					String toChange = matcher.group(1);

					logger.trace("Changing {}'s Nickname to {} because of dad functionality", member, toChange);

					member.modifyNickname(toChange).reason("Dad function").queue();
				}
			}
		}
	}
}
