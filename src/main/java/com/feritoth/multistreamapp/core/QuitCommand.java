package com.feritoth.multistreamapp.core;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "data")
public class QuitCommand implements Serializable {

	private static final long serialVersionUID = -2171048661743940213L;
	
	private Timestamp timestamp;
	private String haltMessage;
	
	public QuitCommand() {
		super();
	}

	public QuitCommand(Timestamp timestamp, String haltMessage) {
		super();
		this.timestamp = timestamp;
		this.haltMessage = haltMessage;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getHaltMessage() {
		return haltMessage;
	}

	public void setHaltMessage(String haltMessage) {
		this.haltMessage = haltMessage;
	}

	@Override
	public String toString() {
		return "QuitCommand [timestamp=" + timestamp + ", haltMessage="	+ haltMessage + "]";
	}

}