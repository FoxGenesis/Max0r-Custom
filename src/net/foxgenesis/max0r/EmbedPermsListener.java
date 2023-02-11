package net.foxgenesis.max0r;

import static net.foxgenesis.util.StringUtils.CONTAINS_URL;

import java.util.function.BiPredicate;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.config.fields.BooleanField;
import net.foxgenesis.config.fields.StringField;
import net.foxgenesis.watame.util.DiscordUtils;;

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
	private static final BooleanField enabled = new BooleanField("max0r-embedperms-enabled", guild -> true, true);

	/**
	 * Configuration string containing url for no embed permissions image
	 */
	private static final StringField embedURL = new StringField("max0r-embedperms-url",
			guild -> "https://media.tenor.com/FdA_-MF4hIAAAAAC/bobux-roblox.gif", true);

	/**
	 * Function to build no embed permissions image
	 */
	private static final Function<@Nonnull Guild, @Nonnull MessageEmbed> noEmbedImage = guild -> new EmbedBuilder()
			.setColor(DiscordUtils.getBotMember(guild).getColor()).setImage(embedURL.optFrom(guild)).build();

	/**
	 * Predicate to check if a user has embed permissions in a guild channel
	 */
	private static BiPredicate<Member, @Nonnull GuildChannel> hasEmbedPerms = (member, channel) -> member
			.getPermissions(channel).contains(Permission.MESSAGE_EMBED_LINKS);

	@Override
	public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
		// Check if from guild
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			// Check if enabled
			if (enabled.optFrom(guild)) {
				GuildChannel channel = event.getGuildChannel();
				Message message = event.getMessage();

				// Check for url and user has no embed perms but we do
				if (CONTAINS_URL.test(message.getContentStripped()) && !hasEmbedPerms.test(event.getMember(), channel)
						&& hasEmbedPerms.test(DiscordUtils.getBotMember(guild), channel)) {

					message.replyEmbeds(noEmbedImage.apply(guild)).queue();
				}
			}
		}
	}
}
