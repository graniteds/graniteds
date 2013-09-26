package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;


@RemoteDestination
public interface Service {

	public String getProp();
	
	public int getBla();
	
	public void setBla(int bla);
	
	public void doSomething();
	
	public String doSomethingElse(String arg);
}
