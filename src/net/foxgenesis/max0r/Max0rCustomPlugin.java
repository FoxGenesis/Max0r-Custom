package net.foxgenesis.max0r;

import java.util.Properties;

import org.apache.commons.configuration2.PropertiesConfiguration;

import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;
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
	protected void init(ProtectedJDABuilder builder) throws SeverePluginException {
		logger.info("Adding embed listener");
		builder.addEventListeners(new EmbedPermsListener());
		logger.info("Adding dad listener");
		builder.addEventListeners(new DadListener());
		logger.info("Adding spaz dick listener");
		builder.addEventListeners(new SpazDickListener());
	}

	@Override
	protected void postInit(WatameBot bot) throws SeverePluginException {}

	@Override
	protected void onReady(WatameBot bot) throws SeverePluginException {}

	@Override
	public void close() {}
}
