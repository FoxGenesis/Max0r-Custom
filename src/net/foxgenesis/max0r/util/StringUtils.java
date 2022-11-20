package net.foxgenesis.max0r.util;

import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Utility class for Strings
 * 
 * @author Ashley
 *
 */
public final class StringUtils {
	/**
	 * Compiled regex checking for URL character sequences. This pattern is case
	 * insensitive. <blockquote>
	 * {@code \b((http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])}
	 * </blockquote>
	 */
	public static final Pattern URL_REGEX = Pattern.compile(
			"\\b((http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])",
			Pattern.CASE_INSENSITIVE);

	/**
	 * NEED_JAVADOC
	 */
	public static final Predicate<String> CONTAINS_URL = URL_REGEX.asPredicate();

	/**
	 * Compiled regex checking for URL character sequences. This pattern is case
	 * insensitive and contains named groups for {@code protocol}, {@code domain}
	 * and {@code path}. <blockquote>
	 * {@code \b(?<protocol>http|https|ftp|file)://(?<domain>[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|])(?<path>\S*)}
	 * </blockquote>
	 */
	public static final Pattern URL_REGEX_NAMED_GROUP = Pattern.compile(
			"\\b(?<protocol>http[s]?|ftp|file)://(?<domain>[-a-zA-Z0-9+&@#%?=~_|!:,.;]*[-a-zA-Z0-9+&@#%=~_|])(?<path>\\S*)",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Find all URLs within a String and split its components into groups.
	 * 
	 * @param str - String to check
	 * @return A {@link Stream} of {@link MatchResult MatchResults} containing URL
	 *         groups: {@code protocol}({@code 1}), {@code domain}({@code 2}) and
	 *         {@code path}({@code 3})
	 */
	public static Stream<MatchResult> findURLWithGroups(@Nonnull String str) {
		return URL_REGEX_NAMED_GROUP.matcher(str).results();
	}
}
