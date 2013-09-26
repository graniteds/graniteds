package org.granite.test.builder.services;

import org.granite.test.builder.entities.Entity1;



public interface SecureEntity2Repository extends SecureEntity1Repository {

	@Override
	public <S extends Entity1> S save(S entity);
}
