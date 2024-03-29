package net.foxgenesis.max0r;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.foxgenesis.max0r.listener.DadListener;
import net.foxgenesis.max0r.listener.EmbedPermsListener;
import net.foxgenesis.max0r.listener.InsultCommand;
import net.foxgenesis.max0r.listener.NonPingableNameListener;
import net.foxgenesis.max0r.listener.ExclamationNameListener;
import net.foxgenesis.max0r.listener.VoiceChatListener;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.SeverePluginException;
import net.foxgenesis.watame.plugin.require.CommandProvider;
import net.foxgenesis.watame.plugin.require.RequiresIntents;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Max0rCustomPlugin extends Plugin implements RequiresIntents, CommandProvider {
	private NonPingableNameListener pingable;

	@Override
	protected void preInit() throws SeverePluginException {}

	@Override
	protected void init(IEventStore builder) throws SeverePluginException {
		logger.info("Adding listeners");
		pingable = new NonPingableNameListener();

		builder.registerListeners(this, new EmbedPermsListener(), new DadListener(), pingable, new VoiceChatListener(),
				new InsultCommand(), new ExclamationNameListener());


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

	@Override
	@NotNull
	public Collection<CommandData> getCommands() {
		return Set.of(Commands.slash("insult", "Generate a random insult"));
	}
}
