package it.apc.tradingsimulator;

public class StockRow {

	private String id;
	private String name;
	private double price = 0;
	private double change = 0;
	private double pchange = 0;
	private long quantity = 0;
	
	public StockRow (String id, String name, double price, double change, double pchange, long quantity) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.change = change;
		this.pchange = pchange;
		this.quantity = quantity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public double getPchange() {
		return pchange;
	}

	public void setPchange(double pchange) {
		this.pchange = pchange;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
}
