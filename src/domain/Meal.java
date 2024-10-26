package domain;

public class Meal {

	private String name;
	private int price;
	private int Quantity;
	private int totalOrder;

	public Meal(String name, int price) {
		this.name = name;
		this.price = price;
	}
	public Meal(String name, int cantidad, int totalOrder) {
		this.name = name;
		this.Quantity = cantidad;
		this.totalOrder = totalOrder;
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

	public int getQuantity() {
		return Quantity;
	}

	public void setQuantity(int quantity) {
		Quantity = quantity;
	}

	public int getTotalOrder() {
		return totalOrder;
	}
	public void setTotalOrder(int totalOrder) {
		this.totalOrder = totalOrder;
	}

	public String toStringPedido() {
		return name + "," + Quantity +","+ totalOrder;
	}

	public String toStringPedidoForList() {
		return name + " Cantidad del pedido" + Quantity +" Total: "+ totalOrder;
	}
}
