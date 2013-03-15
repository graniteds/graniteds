package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity1;


@RemoteDestination
public interface CustomRepository1 {
	
	public <S extends Entity1> S save(S entity);

}
