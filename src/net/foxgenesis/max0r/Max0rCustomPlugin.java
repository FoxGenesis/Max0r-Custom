package net.foxgenesis.max0r;

import java.util.Properties;

import org.apache.commons.configuration2.PropertiesConfiguration;

import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.SpazDickListener;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.SeverePluginException;

public class Max0rCustomPlugin extends Plugin {
	@Override
	protected void onPropertiesLoaded(Properties properties) {}

	@Override
	protected void onConfigurationLoaded(String id, PropertiesConfiguration properties) {}

	@Override
	protected void preInit() throws SeverePluginException {}

	@Override
	protected void init(IEventStore builder) throws SeverePluginException {
		logger.info("Adding listeners");
		builder.registerListeners(this, new EmbedPermsListener(), new DadListener(), new SpazDickListener(),
				new NonPingableNameListener());
	}

	@Override
	protected void postInit(WatameBot bot) throws SeverePluginException {}

	@Override
	protected void onReady(WatameBot bot) throws SeverePluginException {
		bot.getJDA().getGuildCache().acceptStream(NonPingableNameListener::scanGuilds);
	}

	@Override
	public void close() {}
}
