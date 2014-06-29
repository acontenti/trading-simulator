package it.apc.tradingsimulator;

import java.io.Serializable;

public class Stock implements Serializable{

	private static final long serialVersionUID = -8185969689672424334L;
	private String id;
	private double lastPrice = 0;
	private int quantity = 0;
	
	public Stock(String id, double lastPrice, int quantity) {
		super();
		this.id = id;
		this.lastPrice = lastPrice;
		this.quantity = quantity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
