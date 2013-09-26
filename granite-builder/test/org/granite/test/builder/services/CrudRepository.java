package org.granite.test.builder.services;

import java.io.Serializable;


public interface CrudRepository<E, ID extends Serializable> extends BaseRepository<E, ID> {
	
	long count();
	
	void delete(ID id);
	
	void delete(Iterable<? extends E> entities);
	
	void delete(E entity);
	
	void deleteAll();
	
	boolean	exists(ID id);
	
	Iterable<E> findAll();
	
	Iterable<E>	findAll(Iterable<ID> ids);
	
	E findOne(ID id);
	
	<S extends E> Iterable<S> save(Iterable<S> entities);
	
	<S extends E> S	save(S entity);
}
