package org.granite.test.builder.services;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.test.builder.entities.Entity1;


@RemoteDestination
public interface Repository1 extends Repository<Entity1, Long> {
}
