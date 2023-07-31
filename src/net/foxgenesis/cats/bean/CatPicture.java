package net.foxgenesis.cats.bean;

import java.util.Arrays;
import java.util.Objects;

public class CatPicture {
	private String id;
	private String url;
	private int width;
	private int height;
	private Breed[] breeds;
	private Category[] categories;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Breed[] getBreeds() {
		return breeds;
	}

	public void setBreeds(Breed[] breeds) {
		this.breeds = breeds;
	}

	public Category[] getCategories() {
		return categories;
	}

	public void setCategories(Category[] categories) {
		this.categories = categories;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CatPicture other = (CatPicture) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "CatPicture [" + (id != null ? "id=" + id + ", " : "") + (url != null ? "url=" + url + ", " : "")
				+ "width=" + width + ", height=" + height + ", "
				+ (breeds != null ? "breeds=" + Arrays.toString(breeds) + ", " : "")
				+ (categories != null ? "categories=" + Arrays.toString(categories) : "") + "]";
	}
}
