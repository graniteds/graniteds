package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity2;


@RemoteDestination
public interface Repository2 extends Repository<Entity2, Long> {
}
