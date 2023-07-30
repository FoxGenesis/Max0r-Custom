package net.foxgenesis.cats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import net.foxgenesis.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public TheCatAPI(String key) {
		this.apiKey = Optional.ofNullable(key);
	}

	public CompletableFuture<List<CatPicture>> search(OkHttpClient client, Size size, MimeType[] mime_types,
			Order order, int page, int limit, int[] categorys, String[] breeds, boolean onlyBreeds,
			boolean includeBreeds, boolean includeCategorys) {
		return search(client, Optional.ofNullable(size), Optional.ofNullable(mime_types), Optional.ofNullable(order),
				Optional.ofNullable(page), Optional.ofNullable(limit), Optional.ofNullable(categorys),
				Optional.ofNullable(breeds), Optional.ofNullable(onlyBreeds), Optional.ofNullable(includeBreeds),
				Optional.ofNullable(includeCategorys));
	}

	public CompletableFuture<List<CatPicture>> search(OkHttpClient client, Optional<Size> size,
			Optional<MimeType[]> mime_types, Optional<Order> order, Optional<Integer> page, Optional<Integer> limit,
			Optional<int[]> categorys, Optional<String[]> breeds, Optional<Boolean> hasBreeds,
			Optional<Boolean> includeBreeds, Optional<Boolean> includeCategorys) {
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
		return submit(client, newRequest("get", "images/search", map, null)).thenApply(response -> {
			try {
				return new JSONArray(response.body().string());
			} catch (JSONException | IOException e) {
				throw new CompletionException(e);
			}
		}).thenApply(arr -> {
			List<CatPicture> list = new ArrayList<>(arr.length());
			for (int i = 0; i < arr.length(); i++)
				list.add(new CatPicture(arr.getJSONObject(i)));
			return list;
		});
	}

	private Request newRequest(String method, String endpoint, Map<String, String> params, Supplier<RequestBody> body) {
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
			builder.method(method.toUpperCase(), body.get());

		return builder.build();
	}

	private CompletableFuture<Response> submit(OkHttpClient client, Request request) {
		logger.trace("{} {}", request.method().toUpperCase(), request.url().toString());

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
}
