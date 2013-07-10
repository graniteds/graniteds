package org.granite.test.jmf.model;

import org.granite.messaging.annotations.PropertiesOrder;

@PropertiesOrder("foo")
public class IllegalNamePropertiesOrderBean {

	private String name;
	
	public IllegalNamePropertiesOrderBean() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
