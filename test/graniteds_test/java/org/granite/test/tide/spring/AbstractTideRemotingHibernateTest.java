package org.granite.test.tide.spring;

import org.granite.test.tide.data.Person;
import org.granite.test.tide.spring.service.HibernatePersonService;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class AbstractTideRemotingHibernateTest extends AbstractTideTestCase {
    
	@Test
    public void testPersistCall() {
        InvocationResult result = invokeComponent(null, HibernatePersonService.class, "create", new Object[] { "joe" });
        
        Assert.assertEquals("Update count", 1, result.getUpdates().length);
        Assert.assertEquals("Update type", EntityUpdateType.PERSIST.name(), result.getUpdates()[0][0]);
        Assert.assertEquals("Update class", Person.class, result.getUpdates()[0][1].getClass());
    }
}
