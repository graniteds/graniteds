package org.granite.test.tide.seam;

import javax.servlet.ServletContext;

import org.granite.test.tide.TestDataUpdatePostprocessor.WrappedUpdate;
import org.granite.test.tide.data.Person;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class TideRemotingJPADuppTest extends AbstractTideTestCase {
	
	@Override
	protected ServletContext initServletContext() {
		ServletContext servletContext = super.initServletContext();
		servletContext.setAttribute("dupp", true);
		return servletContext;
	}
    
	@Test
    public void testPersistCall() {
        InvocationResult result = invokeComponent("personAction", "create", new Object[] { "joe" });
        
        Assert.assertEquals("Update count", 1, result.getUpdates().length);
        Assert.assertEquals("Update type", EntityUpdateType.PERSIST.name(), result.getUpdates()[0][0]);
        Assert.assertEquals("Update class", WrappedUpdate.class, result.getUpdates()[0][1].getClass());
        Assert.assertEquals("Update class 2", Person.class, ((WrappedUpdate)result.getUpdates()[0][1]).getEntity().getClass());
    }
}
