package net.foxgenesis.max0r.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import net.foxgenesis.http.ApiKey;
import net.foxgenesis.http.JsonApi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InsultApi extends JsonApi {

	public InsultApi() {
		super("Insult API");
	}
	
	public CompletableFuture<Insult> getRandomInsult() {
		return get(null, null, null, Insult.class);
	}

	@Override
	@Nullable
	protected ApiKey getApiKey() {
		return null;
	}

	@Override
	@NotNull
	protected String getBaseURL() {
		return "https://evilinsult.com/generate_insult.php";
	}

	@Override
	@Nullable
	protected Map<String, String> getDefaultQueryParameters() {
		return Map.of("lang", "en", "type", "json");
	}
}
