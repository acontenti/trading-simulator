package it.apc.tradingsimulator;

import java.io.Serializable;

public class Stock implements Serializable{

	private static final long serialVersionUID = -8185969689672424334L;
	private String id;
	private double lastPrice = 0;
	private long quantity = 0;
	
	public Stock(String id, double lastPrice, long quantity) {
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

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
}
