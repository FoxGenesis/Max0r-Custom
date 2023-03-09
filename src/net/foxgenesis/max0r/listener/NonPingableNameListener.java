package net.foxgenesis.max0r.listener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.foxgenesis.property.IProperty;
import net.foxgenesis.property.IPropertyProvider;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.util.StreamUtils;
import net.foxgenesis.watame.WatameBot;
import net.foxgenesis.watame.property.IGuildPropertyMapping;

/**
 * Listener class that ensures all members nicknames are pingable.
 * 
 * @author Ashley
 *
 */
public class NonPingableNameListener extends ListenerAdapter {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger("Non-Pingable Names");

	/**
	 * Regex to test if a name should be changed
	 */
	private static final Predicate<String> shouldChangeName = Pattern.compile("\\b[\\x21-\\x7E]{3}").asPredicate()
			.negate();

	/**
	 * If enabled property
	 */
	private static final IProperty<String, Guild, IGuildPropertyMapping> enabled;

	/**
	 * Name replacement property
	 */
	private static final IProperty<String, Guild, IGuildPropertyMapping> replacement;

	static {
		IPropertyProvider<String, Guild, IGuildPropertyMapping> provider = WatameBot.INSTANCE
				.getPropertyProvider();

		enabled = provider.getProperty("max0r_pingable_enabled");
		replacement = provider.getProperty("max0r_pingable_replacement");
	}

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) { checkName(event.getMember()); }

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) { checkName(e.getMember()); }

	/**
	 * Ensure that a member's name is pingable
	 * 
	 * @param member - member to check
	 */
	private static void checkName(Member member) {
		Guild guild = member.getGuild();
		Member self = guild.getSelfMember();

		// Check if enabled, should change nickname and can interact with member
		if (isEnabled(guild) && shouldChangeName.test(member.getEffectiveName()) && self.canInteract(member)) {

			// Check if bot has permissions to change nicknames
			if (self.hasPermission(Permission.NICKNAME_MANAGE)) {
				member.modifyNickname(getReplacement(guild))
						.reason("Non-Pingable name").queue();;
			} else
				logger.warn("Unable to change nicknames in [{}]! Missing permissions!", guild.getName());
		}
	}

	/**
	 * Scan all guilds for non-pingable names.
	 * 
	 * @param guilds - stream of guilds to test
	 */
	public static void scanGuilds(Stream<Guild> guilds) {
		logger.info("Scanning all guilds...");
		CompletableFuture.allOf(guilds.filter(NonPingableNameListener::isEnabled)
				.map(NonPingableNameListener::scanGuild).toArray(CompletableFuture[]::new))
				.whenComplete((v, err) -> logger.info("Scan completed!"));
	}

	/**
	 * Scan a guild for non-pingable names.
	 * 
	 * @param guild - guild to check
	 * @return Returns a {@link CompletableFuture} that completes normally when all
	 *         names have been changed
	 */
	public static CompletableFuture<Void> scanGuild(Guild guild) {
		CompletableFuture<Void> cf = new CompletableFuture<>();

		// Check if members are loaded
		if (!guild.isLoaded()) {
			logger.warn("Guild [{}] is not loaded or GUILD_MEMBERS intent is missing! Skipping...", guild.getName());
			cf.complete(null);
			return cf;
		}

		Member self = guild.getSelfMember();

		// Check if we can change nicknames
		if (!self.hasPermission(Permission.NICKNAME_MANAGE)) {
			logger.warn("Missing Permission [NICKNAME_MANAGE] in guild [{}]! Skipping...", guild.getName());
			cf.complete(null);
			return cf;
		}

		logger.info("Scanning guild [{}] for non-pingable names...");

		long start = System.nanoTime();

		// Find all members that we can interact with and has a non-pingable name
		guild.findMembers(member -> self.canInteract(member) && shouldChangeName.test(member.getEffectiveName()))
				.onSuccess(members -> {
					// If no members, complete
					if (members.isEmpty()) {
						cf.complete(null);
						return;
					}
					logger.info("Changing {} non-pingable names in [{}]", members.size(), guild.getName());

					// Get replacement name
					String replacement = getReplacement(guild);
					// Modify all nicknames of filtered members and wrap all RestActions into one
					RestAction.allOf(StreamUtils.getEffectiveStream(members)
							.map(member -> member.modifyNickname(replacement).reason("Non-Pingable name")).toList())
							.queue(v -> cf.complete(null));
				}).onError(err -> {
					logger.error("Error while scanning guild [" + guild.getName() + "] for members", err);
					cf.complete(null);
				});

		return cf.whenComplete((v, err) -> {
			long end = System.nanoTime();
			logger.info("Finished changing non-pingable names from [{}] in {} seconds", guild.getName(),
					MethodTimer.formatToSeconds(end - start));
		});
	}

	private static boolean isEnabled(Guild guild) {
		return enabled.get(guild, false, IGuildPropertyMapping::getAsBoolean);
	}
	
	private static String getReplacement(Guild guild) {
		String set = NonPingableNameListener.replacement.get(guild, "Pingable name only",
				IGuildPropertyMapping::getAsString);
		return set.length() < 3 ? "Pingable name only" : set;
	}
}
