package net.foxgenesis.cats;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

import net.foxgenesis.cats.bean.Breed;
import net.foxgenesis.cats.bean.CatPicture;
import net.foxgenesis.util.ArrayUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TheCatAPI {
	private static final Logger logger = LoggerFactory.getLogger(TheCatAPI.class);

	private static final String API_URL = "https://api.thecatapi.com/v1";

	private final CopyOnWriteArraySet<Breed> breeds = new CopyOnWriteArraySet<>();
	private final Optional<String> apiKey;

	public TheCatAPI(@Nullable String key) {
		this.apiKey = Optional.ofNullable(key);
	}

	@NotNull
	public CompletableFuture<CatPicture[]> search(@NotNull OkHttpClient client, @Nullable Size size,
			@Nullable MimeType[] mime_types, @Nullable Order order, int page, int limit, @Nullable int[] categorys,
			@Nullable String[] breeds, boolean onlyBreeds, boolean includeBreeds, boolean includeCategorys) {
		return search(client, Optional.ofNullable(size), Optional.ofNullable(mime_types), Optional.ofNullable(order),
				Optional.ofNullable(page), Optional.ofNullable(limit), Optional.ofNullable(categorys),
				Optional.ofNullable(breeds), Optional.ofNullable(onlyBreeds), Optional.ofNullable(includeBreeds),
				Optional.ofNullable(includeCategorys));
	}

	@NotNull
	public CompletableFuture<CatPicture> getPictureFromID(@NotNull OkHttpClient client, @NotNull String id,
			String subid, Size size, boolean includeVote, boolean includeFavorite) {
		return getPictureFromID(client, id, Optional.ofNullable(subid), Optional.ofNullable(size),
				Optional.ofNullable(includeVote), Optional.ofNullable(includeFavorite));
	}

	@NotNull
	public CompletableFuture<CatPicture[]> search(@NotNull OkHttpClient client, @NotNull Optional<Size> size,
			@NotNull Optional<MimeType[]> mime_types, @NotNull Optional<Order> order, @NotNull Optional<Integer> page,
			@NotNull Optional<Integer> limit, @NotNull Optional<int[]> categorys, @NotNull Optional<String[]> breeds,
			@NotNull Optional<Boolean> hasBreeds, @NotNull Optional<Boolean> includeBreeds,
			@NotNull Optional<Boolean> includeCategorys) {
		// Construct query parameters
		Map<String, String> map = new HashMap<>();
		map.put("size", size.map(Object::toString).map(String::toLowerCase).orElse(""));
		map.put("mime_types", mime_types.map(ArrayUtils::commaSeparated).map(String::toLowerCase).orElse(""));
		map.put("order", order.map(Object::toString).orElse(""));
		map.put("page", page.map(p -> p + "").orElse(""));
		map.put("limit", limit.map(l -> l + "").orElse(""));
		map.put("category_ids", categorys.map(c -> joinInts(",", c)).orElse(""));
		map.put("breed_ids", breeds.map(ArrayUtils::commaSeparated).orElse(""));
		map.put("onlyBreeds", hasBreeds.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_breeds", includeBreeds.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_categories", includeBreeds.map(b -> "" + b.compareTo(false)).orElse(""));

		// Enqueue and map to result
		return submit(client, newRequest(Method.GET, "images/search", map, null))
				.thenApplyAsync(response -> readJSONResponse(response, CatPicture[].class));
	}

	@NotNull
	public CompletableFuture<CatPicture> getPictureFromID(@NotNull OkHttpClient client, @NotNull String id,
			@NotNull Optional<String> subid, @NotNull Optional<Size> size, @NotNull Optional<Boolean> includeVote,
			@NotNull Optional<Boolean> includeFavorite) {
		// Construct query parameters
		Map<String, String> map = new HashMap<>();
		map.put("size", size.map(Object::toString).map(String::toLowerCase).orElse(""));
		map.put("sub_id", subid.orElse(""));
		map.put("include_vote", includeVote.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_favorite", includeFavorite.map(b -> "" + b.compareTo(false)).orElse(""));

		// Enqueue and map to result
		return submit(client, newRequest(Method.GET, "images/" + id, map, null))
				.thenApplyAsync(response -> readJSONResponse(response, CatPicture.class));
	}

	@NotNull
	public CompletableFuture<Breed[]> getBreedList(@NotNull OkHttpClient client) {
		if (!breeds.isEmpty())
			return CompletableFuture.completedFuture(breeds.toArray(Breed[]::new));

		// Enqueue and map to result
		logger.debug("Getting breed list");
		return submit(client, newRequest(Method.GET, "breeds", null, null))
				.thenApplyAsync(response -> readJSONResponse(response, Breed[].class)).handleAsync((breeds, err) -> {
					if (err == null) {
						// Sort breeds by name
						Arrays.sort(breeds, (a, b) -> a.getName().compareTo(b.getName()));

						// Update breed list
						synchronized (this.breeds) {
							this.breeds.clear();
							this.breeds.addAll(List.of(breeds));
							return this.breeds.toArray(Breed[]::new);
						}
					}
					return null;
				});
	}

	private Request newRequest(Method method, String endpoint, Map<String, String> params, Supplier<RequestBody> body) {
		Objects.requireNonNull(method);

		// Create URL builder for the specified end point
		HttpUrl.Builder httpBuilder = HttpUrl.parse(API_URL + '/' + endpoint).newBuilder();

		// Add URL parameters
		if (params != null)
			for (Map.Entry<String, String> param : params.entrySet())
				httpBuilder.addQueryParameter(param.getKey(), param.getValue());

		// Construct a new request builder
		Builder builder = new Request.Builder();
		builder.url(httpBuilder.build());
		builder.addHeader("Content-Type", "application/json");

		// Add API key if present
		apiKey.ifPresent(key -> builder.addHeader("x-api-key", key));

		// Add request body if specified
		if (body != null)
			builder.method(method.name().toUpperCase(), body.get());

		// Build the request
		return builder.build();
	}

	/**
	 * Enqueue a {@link Request} to an {@link OkHttpClient} and map it's
	 * {@link Callback} to a {@link CompletableFuture}.
	 * 
	 * @param client  - HTTP client to use
	 * @param request - request to enqueue
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete normally when
	 *         the {@link Callback#onResponse(Call, Response)} is called. Otherwise
	 *         will complete exceptionally if the
	 *         {@link Callback#onFailure(Call, IOException)} is called.
	 */
	private static CompletableFuture<Response> submit(OkHttpClient client, Request request) {
		Objects.requireNonNull(client);
		CompletableFuture<Response> callback = new CompletableFuture<>();

		logger.debug("{} {}", request.method().toUpperCase(), request.url().toString());
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				callback.completeExceptionally(arg1);
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				callback.complete(arg1);
			}
		});
		return callback;
	}

	/**
	 * Map a {@link Response} to a JavaBean. The specified response's content type
	 * must be {@code application/json}.
	 * 
	 * @param <T>      JavaBean class
	 * @param response - response to get data from
	 * @param javaBean - JavaBean class to map JSON to
	 * 
	 * @return Returns an instance of the {@code javaBean} that was constructed with
	 *         the response's {@link okhttp3.ResponseBody#string() body} content
	 */
	private static <T> T readJSONResponse(Response response, Class<? extends T> javaBean) {
		// Ensure content type is application/json
		if (!response.body().contentType().subtype().equals("json"))
			throw new CompletionException(new IOException("Returned content type is not application/json"));

		// Construct the JavaBean from the response body
		ObjectMapper mapper = new ObjectMapper();
		try (JsonParser parser = mapper.createParser(response.body().string())) {
			return mapper.readValue(parser, javaBean);
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}

	private static String joinInts(String delim, int[] arr) {
		if (arr == null)
			return null;

		String out = "";
		for (int i = 0; i < arr.length; i++) {
			if (i != 0)
				out += ',';
			out += arr[i];
		}
		return out;
	}
}
