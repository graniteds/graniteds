package org.granite.test.builder.services;



public interface Repository<E, ID> {

	public Iterable<E> findAll(Iterable<ID> ids);
}
