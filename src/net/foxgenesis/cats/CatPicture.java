package net.foxgenesis.cats;

import java.util.Objects;

import org.json.JSONObject;

public class CatPicture {

	private JSONObject json;

	CatPicture(JSONObject json) {
		this.json = Objects.requireNonNull(json);
	}

	public String getID() {
		return json.getString("id");
	}

	public String getURL() {
		return json.getString("url");
	}

	public int getWidth() {
		return json.getInt("width");
	}

	public int getHeight() {
		return json.getInt("height");
	}

	public String getMimeType() {
		return json.getString("mime_type");
	}

	@Override
	public String toString() {
		return json.toString();
	}
}
