package org.granite.tide.test.home;

import javax.persistence.PersistenceException;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.framework.EntityQuery;


@Name("baseQuery")
@AutoCreate
@BypassInterceptors
public class BaseQuery extends EntityQuery<BaseEntity> {

	private static final long serialVersionUID = 1L;

	
	@Override
	public void validate() {
	}
	
	@Override
	protected javax.persistence.Query createCountQuery() {
		throw new PersistenceException("No argument");
	}
	
	@Override
	protected javax.persistence.Query createQuery() {
		throw new PersistenceException("No argument");
	}
	
}
