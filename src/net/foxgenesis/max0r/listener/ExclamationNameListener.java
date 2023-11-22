package net.foxgenesis.max0r.listener;

import java.util.concurrent.CompletableFuture;
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
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.property.PluginProperty;
import net.foxgenesis.watame.property.PluginPropertyProvider;
import net.foxgenesis.property.PropertyMapping;
import net.foxgenesis.property.PropertyType;
import net.foxgenesis.util.MethodTimer;
import net.foxgenesis.util.StreamUtils;
import net.foxgenesis.watame.WatameBot;

/**
 * Listener class that ensures all members nicknames don't start with '!'.
 * 
 * @author Spaz-Master
 *
 */
public class ExclamationNameListener extends ListenerAdapter {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger("Exclamation Names");

	/**
	 * If enabled property
	 */
	private final PluginProperty enabled;

	/**
	 * Name replacement property
	 */
	private final PluginProperty replacement;

	/**
	 * Name replacement property
	 */
	private final PluginProperty roles;
	///TODO: add roles
	
	
	public ExclamationNameListener() {

		Plugin plugin = WatameBot.getSelfPlugin();
		PluginPropertyProvider provider = WatameBot.getPropertyProvider();
		this.replacement = provider.upsertProperty(plugin, "exclamation.replacement", true, PropertyType.PLAIN);
		this.enabled = provider.upsertProperty(plugin, "exclamation.enabled", true, PropertyType.NUMBER);
		this.roles =  provider.upsertProperty(plugin, "exclamation.roles", true, PropertyType.NUMBER);

	}

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) { 
		checkName(event.getMember()); 
	}

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent e) { 
		checkName(e.getMember()); 
	}

	/**
	 * Ensure that a member's name doesnt start with '!'
	 * 
	 * @param member - member to check
	 */
	private void checkName(Member member) {
		
		if (enabled.get(member.getGuild(), () -> false, PropertyMapping::getAsBoolean)) {
			Guild guild = member.getGuild();
			Member self = guild.getSelfMember();
			
			if(member.getEffectiveName().charAt(0) == '!' && self.canInteract(member)) {
				// Check if bot has permissions to change nicknames
				if (self.hasPermission(Permission.NICKNAME_MANAGE)) {
					member.modifyNickname(getReplacement(guild, member.getEffectiveName()))
							.reason("Name starts with '!'").queue();
				} else
					logger.warn("Unable to change nicknames in [{}]! Missing permissions!", guild.getName());
			}
			
		}
		
	}

	/**
	 * Scan all guilds for names that start with '!'.
	 * 
	 * @param guilds - stream of guilds to test
	 */
	public void scanGuilds(Stream<Guild> guilds) {
		logger.info("Scanning all guilds...");
		CompletableFuture.allOf(guilds.filter(this::isEnabled).map(this::scanGuild).toArray(CompletableFuture[]::new))
				.whenComplete((v, err) -> logger.info("Scan completed!"));
	}

	/**
	 * Scan a guild for names that start with '!'.
	 * 
	 * @param guild - guild to check
	 * @return Returns a {@link CompletableFuture} that completes normally when all
	 *         names have been changed
	 */
	public CompletableFuture<Void> scanGuild(Guild guild) {
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

		logger.info("Scanning guild [{}] for names that start with '!'", guild.getName());

		long start = System.nanoTime();

		// Find all members that we can interact with and has a name that start with '!'
		guild.findMembers(member -> self.canInteract(member) && member.getEffectiveName().charAt(0) == '!')
				.onSuccess(members -> {
					// If no members, complete
					if (members.isEmpty()) {
						cf.complete(null);
						return;
					}
					logger.info("Changing {} names that start with '!' in [{}]", members.size(), guild.getName());

					// Get replacement name
					//String replacement = getReplacement(guild, member.getEffectiveName());
					// Modify all nicknames of filtered members and wrap all RestActions into one
					RestAction.allOf(StreamUtils.getEffectiveStream(members)
							.map(member -> member.modifyNickname(getReplacement(guild, member.getEffectiveName())).reason("Name starts with '!'")).toList())
							.queue(v -> cf.complete(null));
				}).onError(err -> {
					logger.error("Error while scanning guild [" + guild.getName() + "] for members", err);
					cf.complete(null);
				});

		return cf.whenComplete((v, err) -> {
			long end = System.nanoTime();
			logger.info("Finished changing names that start with '!' from [{}] in {} seconds", guild.getName(),
					MethodTimer.formatToSeconds(end - start));
		});
	}

	private boolean isEnabled(Guild guild) {
		return enabled.get(guild, () -> false, PropertyMapping::getAsBoolean);
	}
	
	private String getReplacement(Guild guild, String originalName) {
		String replaced = originalName.replaceFirst("^(!+)", "z");
		String set = replacement.get(guild, () -> replaced, PropertyMapping::getAsString);
		return set.length() < 3 || set.length() > 32 ? replaced : set;
	}
}
