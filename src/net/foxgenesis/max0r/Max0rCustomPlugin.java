package net.foxgenesis.max0r;

import java.util.EnumSet;

import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.VoiceChatListener;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.plugin.require.RequiresIntents;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import net.dv8tion.jda.api.requests.GatewayIntent;

public class Max0rCustomPlugin extends Plugin implements RequiresIntents {
	private NonPingableNameListener pingable;

	@Override
	protected void preInit() throws SeverePluginException {}

	@Override
	protected void init(IEventStore builder) throws SeverePluginException {
		PluginPropertyProvider provider = getPropertyProvider();

		logger.info("Adding listeners");
		pingable = new NonPingableNameListener(this, provider);

		builder.registerListeners(this, new EmbedPermsListener(this, provider), new DadListener(this, provider),
				pingable, new VoiceChatListener(this, provider));

	}

	@Override
	protected void postInit() throws SeverePluginException {}

	@Override
	protected void onReady() throws SeverePluginException {
		WatameBot.getJDA().getGuildCache().acceptStream(pingable::scanGuilds);
	}

	@Override
	protected void close() {}

	@Override
	public EnumSet<GatewayIntent> getRequiredIntents() {
		return EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,
				GatewayIntent.GUILD_VOICE_STATES);
	}
}
