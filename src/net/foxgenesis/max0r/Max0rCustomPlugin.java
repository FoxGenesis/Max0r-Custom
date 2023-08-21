package net.foxgenesis.max0r;

import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.VoiceChatListener;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.property.PluginPropertyProvider;

public class Max0rCustomPlugin extends Plugin {
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
	protected void postInit(WatameBot bot) throws SeverePluginException {}

	@Override
	protected void onReady(WatameBot bot) throws SeverePluginException {
		bot.getJDA().getGuildCache().acceptStream(pingable::scanGuilds);
	}

	@Override
	protected void close() {}
}
