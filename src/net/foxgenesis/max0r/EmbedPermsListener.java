package net.foxgenesis.max0r;

import static net.foxgenesis.max0r.util.StringUtils.URL_REGEX;

import java.util.regex.Matcher;

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
import net.foxgenesis.max0r.util.DiscordHelper;;

public class EmbedPermsListener extends ListenerAdapter {
	// private static final Logger logger =
	// LoggerFactory.getLogger("EmbedPermsListener");

	private static final BooleanField enabled = new BooleanField("max0r.embedperms.enabled", guild -> false, true);
	private static final StringField embedURL = new StringField("max0r.embedperms.url",
			guild -> "https://media.tenor.com/FdA_-MF4hIAAAAAC/bobux-roblox.gif", true);

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromGuild()) {
			Guild guild = event.getGuild();

			if (enabled.optFrom(guild)) {
				GuildChannel channel = event.getGuildChannel();
				Message message = event.getMessage();

				// Check if message contains url AND user does not have embed perms AND bot has
				// embed perms
				if (checkForUrls(message.getContentStripped()) && !hasEmbedPerms(event.getMember(), channel)
						&& hasEmbedPerms(DiscordHelper.getBotMember(guild), channel)) {
					// Send no embed permission image
					message.replyEmbeds(buildEmbed(guild)).queue();
				}
			}
		}
	}

	private static MessageEmbed buildEmbed(@Nonnull Guild guild) {
		return new EmbedBuilder().setColor(0).setImage(embedURL.optFrom(guild)).build();
	}

	private static boolean hasEmbedPerms(@Nonnull Member member, @Nonnull GuildChannel channel) {
		return member.getPermissions(channel).contains(Permission.MESSAGE_EMBED_LINKS);
	}

	private static boolean checkForUrls(@Nonnull String in) {
		Matcher urlMatcher = URL_REGEX.matcher(in);

		while (urlMatcher.find()) {
			if (urlMatcher.start() > 0) {
				if (in.charAt(urlMatcher.start() - 1) != '<' && in.charAt(urlMatcher.end() - 1) != '>') { return true; }
			} else
				return true;
		}

		return false;
	}
}
