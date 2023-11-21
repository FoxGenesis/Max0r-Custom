package net.foxgenesis.max0r.api;

import java.util.Objects;

public class Insult {
	private int number;
	private String language;
	private String insult;
	private String created;
	private int shown;
	private String createdby;
	private int active;
	private String comment;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getInsult() {
		return insult;
	}

	public void setInsult(String insult) {
		this.insult = insult;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public int getShown() {
		return shown;
	}

	public void setShown(int shown) {
		this.shown = shown;
	}

	public String getCreatedby() {
		return createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		return Objects.hash(number);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Insult other = (Insult) obj;
		return number == other.number;
	}

	@Override
	public String toString() {
		return "Insult [number=" + number + ", " + (language != null ? "language=" + language + ", " : "")
				+ (insult != null ? "insult=" + insult + ", " : "")
				+ (created != null ? "created=" + created + ", " : "") + "shown=" + shown + ", "
				+ (createdby != null ? "createdby=" + createdby + ", " : "") + "active=" + active + ", "
				+ (comment != null ? "comment=" + comment : "") + "]";
	}

}
