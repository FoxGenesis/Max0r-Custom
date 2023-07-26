package net.foxgenesis.max0r;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration2.Configuration;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.RandomCats;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.SeverePluginException;

public class Max0rCustomPlugin extends Plugin {
	@Override
	protected void onPropertiesLoaded(Properties properties) {}

	@Override
	protected void onConfigurationLoaded(String id, Configuration properties) {}

	@Override
	protected void preInit() throws SeverePluginException {}

	@Override
	protected void init(IEventStore builder) throws SeverePluginException {
		logger.info("Adding listeners");
		builder.registerListeners(this, new EmbedPermsListener(), new DadListener(), new NonPingableNameListener(),
				new RandomCats());
	}

	@Override
	protected void postInit(WatameBot bot) throws SeverePluginException {}

	@Override
	protected void onReady(WatameBot bot) throws SeverePluginException {
		bot.getJDA().getGuildCache().acceptStream(NonPingableNameListener::scanGuilds);
	}

	@Override
	protected void close() {}

	@Override
	public Collection<CommandData> getCommands() {
		return Set.of(Commands.slash("cat", "Get a random image of a cat"));
	}
}
