package net.foxgenesis.max0r;

import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.WatameBot.ProtectedJDABuilder;
import net.foxgenesis.watame.plugin.IPlugin;
import net.foxgenesis.watame.plugin.PluginProperties;

@PluginProperties(name = "Max0r Custom Plugin", description = "A custom plugin for the Max0r discord", version = "0.0.1")
public class Max0rCustomPlugin implements IPlugin {
	/**
	 * Logger
	 */
	// private static final Logger logger = LoggerFactory.getLogger("Max0r");

	@Override
	public void preInit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(ProtectedJDABuilder builder) {
		builder.addEventListeners(new EmbedPermsListener());
		builder.addEventListeners(new NitroScamListener());
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
