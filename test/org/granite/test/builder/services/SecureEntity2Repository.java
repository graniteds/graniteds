package org.granite.test.builder.services;

import org.granite.test.builder.entities.Entity1;



public interface SecureEntity2Repository extends SecureEntity1Repository {

	@Override
	public Entity1 save(Entity1 entity);
}
