package net.foxgenesis.max0r.listener;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.foxgenesis.watame.util.Colors;
import net.foxgenesis.watame.util.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Request.Builder;

public class RandomCats extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(RandomCats.class);

	private static final String API_URL = "https://api.thecatapi.com/v1";

	private final Optional<String> apiKey;

	public RandomCats(String apiKey) {
		this.apiKey = Optional.ofNullable(apiKey);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		switch (event.getFullCommandName()) {
			case "cat" -> {
				event.deferReply().queue();

				// Create new URL request
				Builder builder = new Request.Builder().url(API_URL + "/images/search");
				apiKey.ifPresent(key -> builder.addHeader("x-api-key", key));

				// Enqueue our request
				logger.trace("GET {}", builder.getUrl$okhttp().toString());
				event.getJDA().getHttpClient().newCall(builder.build()).enqueue(new Callback() {

					@Override
					public void onFailure(Call call, IOException e) {
						logger.error("Error occured during api request", e);
						event.getHook().editOriginalEmbeds(Response.error("An error occured. Please try again later."))
								.queue();
					}

					@Override
					public void onResponse(Call call, okhttp3.Response data) throws IOException {
						String body = data.body().string();

						try {
							// Get first image
							JSONArray response = new JSONArray(body);
							JSONObject first = response.getJSONObject(0);

							// Construct embed
							EmbedBuilder builder = new EmbedBuilder();
							builder.setColor(Colors.INFO);
							builder.setTitle("Meow!");
							builder.setImage(first.getString("url"));
							builder.setFooter("via thecatapi.com");

							// Send response
							event.getHook().editOriginalEmbeds(builder.build()).queue();
						} catch (Exception e) {
							logger.error("Error while decoding body ({})", body);
							onFailure(call, new IOException(e));
						}
					}

				});
			}
		}
	}
}
