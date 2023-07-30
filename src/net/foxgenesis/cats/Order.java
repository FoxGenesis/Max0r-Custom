package net.foxgenesis.cats;

public enum Order {
	RANDOM(false), ASC(true), DESC(true);

	private final boolean usePages;

	Order(boolean usePages) {
		this.usePages = usePages;
	}
	
	public boolean usePages() {
		return usePages;
	}
}
