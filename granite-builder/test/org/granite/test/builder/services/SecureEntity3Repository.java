package org.granite.test.builder.services;

import org.granite.test.builder.entities.Entity2;



public interface SecureEntity3Repository extends FilterableJpaRepository<Entity2, Long> {

	@Override
	public <S extends Entity2> S save(S entity);
}
