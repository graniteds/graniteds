package org.granite.test.builder.services;


public interface Repository<E, ID> {

	public void delete(Iterable<? extends E> objs);
	
    public void deleteAllInBatch(Iterable<E> objs);
		
	public Iterable<E> findAll(Iterable<ID> ids);
	
	public E saveAndFlush(E obj);
}
