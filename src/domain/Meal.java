package domain;

public class Meal {

	private String name;
	private int price;

	public Meal(String name, int price) {
		this.name = name;
		this.price = price;
	}
	public Meal() {
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Meal [name=" + name + ", price=" + price + "]";
	}



}
