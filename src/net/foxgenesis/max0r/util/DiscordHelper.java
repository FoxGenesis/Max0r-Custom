package net.foxgenesis.max0r.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * Utility class for Discord related things.
 * 
 * @author Ashley
 *
 */
public final class DiscordHelper {

	/**
	 * NEED_JAVADOC
	 */
	public static final String[] DISCORD_DOMAINS = { "discordapp.com", "discordapp.net", "discord.com", "discord.new",
			"discord.gift", "discord.gifts", "discord.media", "discord.gg", "discord.co", "discord.app", "dis.gd" };

	/**
	 * NEED_JAVADOC
	 */
	public static final Pattern DISCORD_DOMAIN = Pattern.compile(
			"\\bdis(cord(app\\.(net|com)|\\.(com|gg|app|co|media|gift[s]?|new))|\\.gd)\\b", Pattern.CASE_INSENSITIVE);
	/**
	 * Predicate to check if a String is a valid Discord domain
	 */
	public static final Predicate<String> IS_VALID_DISCORD_DOMAIN = DISCORD_DOMAIN.asMatchPredicate();

	/**
	 * NEED_JAVADOC
	 * 
	 * @param guild
	 * @return
	 */
	public static Member getBotMember(@Nonnull Guild guild) {
		return guild.getMemberById(guild.getJDA().getSelfUser().getId());
	}

}
