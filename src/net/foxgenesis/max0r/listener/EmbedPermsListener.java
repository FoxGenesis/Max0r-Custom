package net.foxgenesis.max0r.listener;

import static net.foxgenesis.util.StringUtils.CONTAINS_URL;

import java.util.Arrays;

import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;;

/**
 * Listener to check if a user posts a URL but doesn't have embed permissions.
 * 
 * @author Ashley
 *
 */
public class EmbedPermsListener extends ListenerAdapter {
	private final PluginProperty enabled;
	private final PluginProperty embedURL;

	public EmbedPermsListener(Plugin plugin, PluginPropertyProvider provider) {
		enabled = provider.upsertProperty(plugin, "embedperms.enabled", true, PropertyType.NUMBER);
		embedURL = provider.upsertProperty(plugin, "embedperms.url", true, PropertyType.PLAIN);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// Check if from guild
		if (event.isFromGuild() && !event.isWebhookMessage()) {
			Guild guild = event.getGuild();

			// Check if enabled
			if (enabled.get(guild, () -> true, PropertyMapping::getAsBoolean)) {
				GuildMessageChannelUnion channel = event.getGuildChannel();

				// Check if we can view and send messages
				if (channel.canTalk() && guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_HISTORY,
						Permission.MESSAGE_EMBED_LINKS)) {
					Message message = event.getMessage();

					// Check if the user does not have embed permissions
					if (!(event.getMember() == null
							|| event.getMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS))) {

						// Check if the message contains a URL not wrapped in <>
						if (Arrays.stream(message.getContentStripped().split("<.*?>"))
								.anyMatch(CONTAINS_URL.and(Message.JUMP_URL_PATTERN.asPredicate().negate()))) {
							message.replyEmbeds(noEmbedImage(guild)).queue();
						}
					}
				}
			}
		}
	}

	/**
	 * Function to build a "No Embed Permissions" embed.
	 * 
	 * @param guild - guild to create for
	 * 
	 * @return Returns a {@link MessageEmbed} with the guilds specified "No Embed
	 *         Permissions" image inside an embed
	 */
	private final MessageEmbed noEmbedImage(Guild guild) {
		return new EmbedBuilder().setColor(guild.getSelfMember().getColor()).setImage(embedURL.get(guild,
				() -> "https://media.tenor.com/FdA_-MF4hIAAAAAC/bobux-roblox.gif", PropertyMapping::getAsString))
				.build();
	}
}
