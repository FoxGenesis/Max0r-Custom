package net.foxgenesis.cats;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

	private final Optional<String> apiKey;
	private Breed[] breeds;

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
	public CompletableFuture<CatPicture[]> search(@NotNull OkHttpClient client, @NotNull Optional<Size> size,
			@NotNull Optional<MimeType[]> mime_types, @NotNull Optional<Order> order, @NotNull Optional<Integer> page,
			@NotNull Optional<Integer> limit, @NotNull Optional<int[]> categorys, @NotNull Optional<String[]> breeds,
			@NotNull Optional<Boolean> hasBreeds, @NotNull Optional<Boolean> includeBreeds,
			@NotNull Optional<Boolean> includeCategorys) {
		Objects.requireNonNull(client);

		// Construct query parameters
		Map<String, String> map = new HashMap<>();
		map.put("size", size.map(Object::toString).map(String::toLowerCase).orElse(""));
		map.put("mime_types", mime_types.map(ArrayUtils::commaSeparated).map(String::toLowerCase).orElse(""));
		map.put("order", order.map(Object::toString).orElse(""));
		map.put("page", page.map(p -> p + "").orElse(""));
		map.put("limit", limit.map(l -> l + "").orElse(""));
		map.put("category_ids", categorys.map(ArrayUtils::commaSeparated).orElse(""));
		map.put("breed_ids", breeds.map(ArrayUtils::commaSeparated).orElse(""));
		map.put("onlyBreeds", hasBreeds.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_breeds", includeBreeds.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_categories", includeBreeds.map(b -> "" + b.compareTo(false)).orElse(""));

		// Enqueue and map to result
		return submit(client, newRequest(Method.GET, "images/search", map, null))
				.thenApply(response -> readResponse(response, CatPicture[].class));
	}

	@NotNull
	public CompletableFuture<CatPicture> getPictureFromID(@NotNull OkHttpClient client, @NotNull String id,
			String subid, Size size, boolean includeVote, boolean includeFavorite) {
		return getPictureFromID(client, id, Optional.ofNullable(subid), Optional.ofNullable(size),
				Optional.ofNullable(includeVote), Optional.ofNullable(includeFavorite));
	}

	@NotNull
	public CompletableFuture<CatPicture> getPictureFromID(@NotNull OkHttpClient client, @NotNull String id,
			@NotNull Optional<String> subid, @NotNull Optional<Size> size, @NotNull Optional<Boolean> includeVote,
			@NotNull Optional<Boolean> includeFavorite) {
		Objects.requireNonNull(client);

		// Construct query parameters
		Map<String, String> map = new HashMap<>();
		map.put("size", size.map(Object::toString).map(String::toLowerCase).orElse(""));
		map.put("sub_id", subid.orElse(""));
		map.put("include_vote", includeVote.map(b -> "" + b.compareTo(false)).orElse(""));
		map.put("include_favorite", includeFavorite.map(b -> "" + b.compareTo(false)).orElse(""));

		// Enqueue and map to result
		return submit(client, newRequest(Method.GET, "images/" + id, map, null))
				.thenApply(response -> readResponse(response, CatPicture.class));
	}

	@NotNull
	public CompletableFuture<Breed[]> getBreedList(@NotNull OkHttpClient client) {
		Objects.requireNonNull(client);

		if (breeds != null)
			return CompletableFuture.completedFuture(Arrays.copyOf(breeds, breeds.length));

		// Enqueue and map to result
		logger.debug("Getting breed list");
		return submit(client, newRequest(Method.GET, "breeds", null, null))
				.thenApply(response -> readResponse(response, Breed[].class)).handle((breeds, err) -> {
					if (err == null) {
						this.breeds = breeds;
						return Arrays.copyOf(breeds, breeds.length);
					}
					return null;
				});
	}

	private Request newRequest(Method method, String endpoint, Map<String, String> params, Supplier<RequestBody> body) {
		Objects.requireNonNull(method);

		HttpUrl.Builder httpBuilder = HttpUrl.parse(API_URL + '/' + endpoint).newBuilder();
		if (params != null)
			for (Map.Entry<String, String> param : params.entrySet())
				httpBuilder.addQueryParameter(param.getKey(), param.getValue());

		Builder builder = new Request.Builder();
		builder.url(httpBuilder.build());
		builder.addHeader("Content-Type", "application/json");
		apiKey.ifPresent(key -> builder.addHeader("x-api-key", key));
		if (body != null)
			builder.method(method.name().toUpperCase(), body.get());

		return builder.build();
	}

	private static CompletableFuture<Response> submit(OkHttpClient client, Request request) {
		logger.debug("{} {}", request.method().toUpperCase(), request.url().toString());

		CompletableFuture<Response> callback = new CompletableFuture<>();
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

	private static <T> T readResponse(Response response, Class<? extends T> c) {
		ObjectMapper mapper = new ObjectMapper();
		try (JsonParser parser = mapper.createParser(response.body().string())) {
			return mapper.readValue(parser, c);
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}
}
