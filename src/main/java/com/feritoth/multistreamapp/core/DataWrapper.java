package com.feritoth.multistreamapp.core;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DataWrapper implements Serializable {

	private static final long serialVersionUID = 6983113921092513164L;
	
	@JsonProperty(value = "data")
	private CoreData coreElement;

	public DataWrapper() {
		super();
	}

	public DataWrapper(CoreData coreElement) {
		super();
		this.coreElement = coreElement;
	}

	public CoreData getCoreElement() {
		return coreElement;
	}

	public void setCoreElement(CoreData coreElement) {
		this.coreElement = coreElement;
	}

	@Override
	public String toString() {
		return "DataWrapper [coreElement=" + coreElement + "]";
	}
	
}