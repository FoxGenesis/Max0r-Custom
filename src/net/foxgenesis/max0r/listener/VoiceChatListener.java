package net.foxgenesis.max0r.listener;

import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyMapping;
import net.foxgenesis.watame.property.PluginPropertyProvider;
import net.foxgenesis.watame.util.Colors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceChatListener extends ListenerAdapter {

	private final PluginProperty enabled;
	private final PluginProperty loggingChannel;

	public VoiceChatListener() {
		Plugin plugin = WatameBot.getSelfPlugin();
		PluginPropertyProvider provider = WatameBot.getPropertyProvider();
		enabled = provider.upsertProperty(plugin, "voicelog.enabled", true, PropertyType.NUMBER);
		loggingChannel = provider.upsertProperty(plugin, "voicelog.channel", true, PropertyType.NUMBER);
	}

	@Override
	public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
		Guild guild = event.getGuild();
		if (enabled.get(guild, () -> false, PropertyMapping::getAsBoolean)) {
			loggingChannel.getOr(guild, WatameBot.getLoggingChannel()).map(PluginPropertyMapping::getAsMessageChannel)
					.ifPresent(modlog -> {
						State state = State.getStateForEvent(event);

						EmbedBuilder builder = new EmbedBuilder();
						builder.setColor(state.color);
						builder.setDescription(state.getDisplayString(event.getMember(), event.getChannelJoined(),
								event.getChannelLeft()));

						modlog.sendMessageEmbeds(builder.build()).queue();
					});
		}
	}

	private static enum State {
		JOINED(Colors.SUCCESS, "%s joined %s"), MOVED(Colors.INFO, "%s moved from %s to %s"),
		DISCONNECTED(Colors.ERROR, "%s disconnected from %s");

		private final int color;
		private final String format;

		State(int color, String format) {
			this.color = color;
			this.format = format;
		}

		public String getDisplayString(Member member, AudioChannelUnion current, AudioChannelUnion last) {
			return switch (this) {
				case JOINED -> format.formatted(member.getAsMention(), current.getAsMention());
				case DISCONNECTED -> format.formatted(member.getAsMention(), last.getAsMention());
				case MOVED -> format.formatted(member.getAsMention(), last.getAsMention(), current.getAsMention());
				default -> throw new IllegalArgumentException("Unexpected value: " + this);
			};
		}

		public static State getStateForEvent(GuildVoiceUpdateEvent event) {
			return event.getChannelJoined() == null ? State.DISCONNECTED
					: event.getChannelLeft() == null ? State.JOINED : State.MOVED;
		}
	}
}
