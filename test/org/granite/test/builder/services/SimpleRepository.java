package org.granite.test.builder.services;

import java.util.Map;

import org.granite.messaging.service.annotations.RemoteDestination;


@RemoteDestination
public interface SimpleRepository {
	
	public Map<String, String> doSomething(int page);
}
