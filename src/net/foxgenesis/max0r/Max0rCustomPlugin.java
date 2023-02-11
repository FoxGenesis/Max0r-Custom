package net.foxgenesis.max0r;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.foxgenesis.watame.ProtectedJDABuilder;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.PluginProperties;

/**
 * NEED_JAVADOC
 * 
 * @author Ashley
 *
 */
@PluginProperties(name = "Max0r Custom Plugin", description = "A custom plugin for the Max0r discord", version = "0.0.1", providesCommands = false)
public class Max0rCustomPlugin implements IPlugin {
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger("Max0r");

	@Override
	public void preInit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(ProtectedJDABuilder builder) {
		logger.info("Adding embed listener");
		builder.addEventListeners(new EmbedPermsListener());
		logger.info("Adding dad listener");
		builder.addEventListeners(new DadListener());
		logger.info("Adding spaz dick listener");
		builder.addEventListeners(new SpazDickListener());
	}

	@Override
	public void postInit(WatameBot bot) {}

	@Override
	public void onReady(WatameBot bot) {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
	}	
}
