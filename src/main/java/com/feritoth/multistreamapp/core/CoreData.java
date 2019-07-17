package com.feritoth.multistreamapp.core;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "data")
public class CoreData implements Serializable {

	private static final long serialVersionUID = 8298110033940150134L;
	
	private Timestamp timestamp;
	private double amount;	
	
	public CoreData() {
		super();
	}

	public CoreData(Timestamp timestamp, double amount) {
		super();
		this.timestamp = timestamp;
		this.amount = amount;
	}	

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "CoreData [timestamp=" + timestamp + ", amount=" + amount + "]";
	}
	
}