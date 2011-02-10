package org.granite.test.tide.seam.home;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.framework.EntityHome;


@Name("baseHome")
@AutoCreate
@BypassInterceptors
public class BaseHome extends EntityHome<BaseEntity> {

	private static final long serialVersionUID = 1L;

	
	@Override
	public void create() {
	}	
	
	@Override
	public BaseEntity find() {
		if (Long.valueOf(1200L).equals(getId())) {
	    	Entity1 entity1 = new Entity1();
	    	entity1.setId(1200L);
	    	entity1.setSomeObject("$$Proxy$$test");
	    	return entity1;
		}
		if (Long.valueOf(1201L).equals(getId())) {
	    	Entity2 entity2 = new Entity2();
	    	entity2.setId(1201L);
	    	return entity2;
		}
		return null;
	}
	
	@Override
	public String update() {
		return "test";
	}
	
	@Override
	protected void joinTransaction() {
	}
	
}
