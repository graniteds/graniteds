package org.granite.test.builder.services;

import java.io.Serializable;
import java.util.List;



public interface FilterableJpaRepository<E, ID extends Serializable> extends JpaRepository<E, ID> {
	
	public List<E> findByFilter(Object filter, int page);
}
