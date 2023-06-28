package net.foxgenesis.max0r.listener;

import static net.foxgenesis.util.StringUtils.CONTAINS_URL;

import java.util.Arrays;
import java.util.function.BiPredicate;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.property.IProperty;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;;

/**
 * Listener to check if a user posts a URL but doesn't have embed permissions.
 * 
 * @author Ashley
 *
 */
public class EmbedPermsListener extends ListenerAdapter {

	/**
	 * Conditional to check if this functionality is enabled
	 */
	private static final IProperty<String, Guild, IGuildPropertyMapping> enabled = WatameBot.INSTANCE
			.getPropertyProvider().getProperty("max0r_embedperms_enabled");

	/**
	 * Configuration string containing url for no embed permissions image
	 */
	private static final IProperty<String, Guild, IGuildPropertyMapping> embedURL = WatameBot.INSTANCE
			.getPropertyProvider().getProperty("max0r_embedperms_url");

	/**
	 * Predicate to check if a user has embed permissions in a guild channel
	 */
	private static BiPredicate<Member, GuildChannel> hasEmbedPerms = (member, channel) -> member.hasPermission(channel,
			Permission.MESSAGE_EMBED_LINKS);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		// Check if from guild
		if (event.isFromGuild() && !event.isWebhookMessage()) {
			Guild guild = event.getGuild();

			// Check if enabled
			if (enabled.get(guild, true, IGuildPropertyMapping::getAsBoolean)) {
				GuildChannel channel = event.getGuildChannel();
				Message message = event.getMessage();

				// Check if the user has embed permissions and we do
				if (event.getMember() != null && !hasEmbedPerms.test(event.getMember(), channel)
						&& hasEmbedPerms.test(guild.getSelfMember(), channel)) {

					// Check if the message contains a URL not wrapped in <>
					if (Arrays.stream(message.getContentStripped().split("<.*?>"))
							.anyMatch(CONTAINS_URL.and(Message.JUMP_URL_PATTERN.asPredicate().negate()))) {
						message.replyEmbeds(noEmbedImage(guild)).queue();
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
	private static final MessageEmbed noEmbedImage(Guild guild) {
		return new EmbedBuilder().setColor(guild.getSelfMember().getColor()).setImage(embedURL.get(guild,
				"https://media.tenor.com/FdA_-MF4hIAAAAAC/bobux-roblox.gif", IGuildPropertyMapping::getAsString))
				.build();
	}
}
