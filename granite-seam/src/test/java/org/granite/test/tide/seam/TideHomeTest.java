package org.granite.test.tide.seam;

import javax.persistence.PersistenceException;

import org.granite.messaging.service.ServiceException;
import org.granite.tide.invocation.InvocationResult;
import org.granite.test.tide.seam.home.Entity1;
import org.granite.test.tide.seam.home.Entity2;
import org.junit.Assert;
import org.junit.Test;


public class TideHomeTest extends AbstractTideTestCase {
	
	@Test
    public void testHomeCallGDS566() {
    	Entity1 entity1 = new Entity1();
    	entity1.setId(1200L);
    	entity1.setSomeObject("$$Proxy$$test");
    	Entity2 entity2 = new Entity2();
    	entity2.setId(1201L);
    	
        InvocationResult result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity1.getId() }, 
    			new Object[] { "baseHome", "instance", entity1 } 
    		}, 
    		new String[] { "baseHome.instance" }, 
    		null);
        
        Assert.assertEquals("Entity1 id", 1200L, (long)((Entity1)result.getResults().get(0).getValue()).getId());
        Assert.assertEquals("Entity1 obj", "$$Proxy$$test", ((Entity1)result.getResults().get(0).getValue()).getSomeObject());
        
        result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity2.getId() }, 
    			new Object[] { "baseHome", "instance", entity2 } 
    		}, 
    		new String[] { "baseHome.instance", "baseHome.instance.someObject" }, 
    		null);
        
        Assert.assertEquals("Entity2 id", 1201L, (long)((Entity2)result.getResults().get(0).getValue()).getId());
        
        result = invokeComponent("baseHome", "update", 
    		new Object[] {}, 
    		new Object[] { 
				new Object[] { "baseHome", "id", entity1.getId() }, 
    			new Object[] { "baseHome", "instance", entity1 } 
    		}, 
    		new String[] { "baseHome.instance", "baseHome.instance.someObject" }, 
    		null);
        
        Assert.assertEquals("Entity1 id", 1200L, (long)((Entity1)result.getResults().get(0).getValue()).getId());
        Assert.assertEquals("Entity1 obj", "test", ((Entity1)result.getResults().get(0).getValue()).getSomeObject());
    }
	
	
	@Test
	public void testQueryCallGDS775() {
		boolean error = false;
		try {
			invokeComponent("baseQuery", "refresh", 
	    		new Object[] {}, 
	    		new Object[] { 
					new Object[] { "baseQuery", "firstResult", 0 }, 
	    			new Object[] { "baseQuery", "maxResults", 10 } 
	    		}, 
	    		new String[] { "baseQuery.resultList" }, 
	    		null);
		}
		catch (ServiceException e) {
			if (PersistenceException.class.isInstance(e.getCause()))
				error = true;
		}
		
		Assert.assertTrue("Persistence exception received", error);
	}
}
