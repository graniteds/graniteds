package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;


@RemoteDestination
public class ServiceImpl {
	
	private String prop;
	private int bla;

	public String getProp() {
		return prop;
	}
	
	public int getBla() {
		return bla;
	}
	
	public void setBla(int bla) {
		this.bla = bla;
	}
	
	public void doSomething() {		
	}
	
	public String doSomethingElse(String arg) {
		return null;
	}
	
	@SuppressWarnings("unused")
	private void testInternal() {		
	}
}
