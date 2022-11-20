package net.foxgenesis.max0r;

import static net.foxgenesis.max0r.util.DiscordHelper.DISCORD_DOMAINS;
import static net.foxgenesis.max0r.util.DiscordHelper.IS_VALID_DISCORD_DOMAIN;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.config.fields.BooleanField;
import net.foxgenesis.max0r.util.StringUtils;

/**
 * Listener to ban all discord nitro spam bots.
 * 
 * @author Ashley
 *
 */
public class NitroScamListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Nitro Scam Listener");

	/**
	 * NEED_JAVADOC
	 */
	private static final BooleanField enabled = new BooleanField("max0r.embedperms.enabled", guild -> true, true);

	/**
	 * NEED_JAVADOC
	 */
	private static final String BAN_MESSAGE = "You were banned for being suspected of a Nitro scam spam bot. "
			+ "This bot uses a series of checks to predict if an account is hijacked by a bot. If this was a "
			+ "mistake, please contact a member of the mod team.";

	/**
	 * NEED_JAVADOC
	 */
	private static final int SIMILARITY_THRESHOLD = 3;

	// ============================================================================================

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.optFrom(guild)) {
				Member member = event.getMember();
				// Check if message URLs are too close to valid discord domains
				int similarity = StringUtils.findURLWithGroups(event.getMessage().getContentDisplay()) // Get URLs in
																										// message
						.map(result -> result.group(2)) // Get domain
						.filter(IS_VALID_DISCORD_DOMAIN.negate()) // Not discord domain
						.map(NitroScamListener::compareToValidDomains) // Compare domain to valid discord domains
						.min(Integer::compare) // Get lowest value
						.orElse(10); // Or else 10

				// If domains are too similar, ban the user
				if (similarity <= SIMILARITY_THRESHOLD) {
					logger.debug("Banning {} from {} for suspected Nitro spam bot", member, guild);
					banNitroScamBot(event.getGuildChannel(), member);
				}
			}
		}
	}

	// ============================================================================================

	/**
	 * Ban a User for Discord Nitro spam.
	 * 
	 * @param channel - channel the message was sent in
	 * @param member  - member to ban
	 */
	private static void banNitroScamBot(@Nonnull GuildMessageChannel channel, @Nonnull Member member) {
		// Send ban message to DMs
		member.getUser().openPrivateChannel().submit()
				.thenCompose(dm -> dm.sendMessage(NitroScamListener.BAN_MESSAGE).submit())
				.whenComplete((message, error) -> {
					if (error != null)
						logger.warn("Attempted to DM a ban message to {} but DMs were disabled");
				}).join();

		// Send ban message to channel and ban user
		CompletableFuture
				.allOf(channel.sendMessage("Banned " + member + " for being suspected of a Nitro spambot").submit(),
						member.ban(0, "Automatic detection of being a Nitro spambot")
								.reason("Automatic detection of being a Nitro spambot").submit())
				.join();
	}

	/**
	 * Compare a String to valid Discord domain names.
	 * 
	 * @param url - String containing a url
	 * @return A integer representing how close {@code url} is to valid Discord
	 *         domain names
	 */
	private static int compareToValidDomains(@Nonnull String url) {
		return Arrays.stream(DISCORD_DOMAINS) // Get valid domains
				.map(b -> url.compareToIgnoreCase(b)) // Compare valid domains to input domain
				.map(Math::abs) // Map values to the positive range
				.min(Integer::compare) // Get lowest value
				.orElse(10); // Or else 10
	}
}
