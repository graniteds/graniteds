package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity2;


@RemoteDestination
public interface Entity2Repository {
	
	public void deleteEntities(Iterable<Entity2> entities);
}
