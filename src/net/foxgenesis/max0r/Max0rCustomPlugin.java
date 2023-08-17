package net.foxgenesis.max0r;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.RandomCats;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.CommandProvider;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.PluginConfiguration;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import org.apache.commons.configuration2.Configuration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@PluginConfiguration(defaultFile = "/META-INF/cats/settings.properties", identifier = "catSettings", outputFile = "cats/settings.properties")
public class Max0rCustomPlugin extends Plugin implements CommandProvider {

	private String catAPIKey;
	private NonPingableNameListener pingable;

	@Override
	protected void onConstruct(Properties meta, Map<String, Configuration> configs) {
		for (String id : configs.keySet())
			switch (id) {
				case "catSettings" -> { catAPIKey = configs.get(id).getString("thecatapi_key"); }
			}
	}

	@Override
	protected void preInit() throws SeverePluginException {}

	@Override
	protected void init(IEventStore builder) throws SeverePluginException {
		PluginPropertyProvider provider = getPropertyProvider();

		logger.info("Adding listeners");
		pingable = new NonPingableNameListener(this, provider);

		builder.registerListeners(this, new EmbedPermsListener(this, provider), new DadListener(this, provider),
				pingable, new RandomCats(catAPIKey));

	}

	@Override
	protected void postInit(WatameBot bot) throws SeverePluginException {}

	@Override
	protected void onReady(WatameBot bot) throws SeverePluginException {
		bot.getJDA().getGuildCache().acceptStream(pingable::scanGuilds);
	}

	@Override
	protected void close() {}

	@Override
	public Collection<CommandData> getCommands() {
		return Set.of(
				// Search cat images
				Commands.slash("cat", "Get images of cats").addSubcommands(
						new SubcommandData("search", "Search for images of cats").addOption(OptionType.STRING, "breed",
								"Breed to search for", false, true),
						new SubcommandData("search-server", "Search for images of cats from uploaded images")
								.addOption(OptionType.USER, "user", "From server member")
								.addOption(OptionType.STRING, "breed", "Breed to search for", false, true)),

				// Upload cat pictures
				Commands.slash("catupload", "Upload a picture of a cat").setGuildOnly(true)
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
						.addOption(OptionType.ATTACHMENT, "file", "Cat picture to upload", true));
	}
}
