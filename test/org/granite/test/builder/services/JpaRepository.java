package org.granite.test.builder.services;

import java.io.Serializable;



public interface JpaRepository<E, ID extends Serializable> {
	
	public E saveAndFlush(E obj);
	
	public E save(E obj);
}
