package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity2;


@RemoteDestination
public interface CustomRepository2 {
	
	public <S extends Entity2> S save(S entity);

}
