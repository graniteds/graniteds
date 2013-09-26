package org.granite.test.builder.services;

import org.granite.test.builder.entities.Entity1;



public interface SecureEntity1Repository extends FilterableJpaRepository<Entity1, Long> {

	@Override
	public Entity1 saveAndFlush(Entity1 entity);
}
