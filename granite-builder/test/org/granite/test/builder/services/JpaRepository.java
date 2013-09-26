package org.granite.test.builder.services;

import java.io.Serializable;
import java.util.List;


public interface JpaRepository<E, ID extends Serializable> extends CrudRepository<E, ID> {
	
	 void deleteAllInBatch(); 

	 void deleteInBatch(Iterable<E> entities); 
	 
	 @Override
	 List<E> findAll();

	 @Override
	 <S extends E> List<S> save(Iterable<S> entities); 
      
	 E saveAndFlush(E entity);
}
