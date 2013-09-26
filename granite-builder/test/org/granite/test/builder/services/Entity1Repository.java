package org.granite.test.builder.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity1;


@RemoteDestination
public interface Entity1Repository {
	
	public List<Entity1> getEntities(int page);
}
