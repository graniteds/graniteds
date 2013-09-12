package org.granite.test.tide.ejb;

import org.granite.context.GraniteContext;
import org.granite.test.tide.TestInvocationListener;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.util.AbstractContext;
import org.junit.Assert;
import org.junit.Test;


public class TideRemotingTest extends AbstractTideTestCase {
    
	@Test
    public void testSimpleCall() {
        InvocationResult result = invokeComponent("helloService", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
    
	@Test
    public void testSimpleCall2() {
        InvocationResult result = invokeComponent("hello2Service", null, "hello2", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
	public void testBypassMerge() {
        InvocationResult result = invokeComponent("helloMergeService", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
        Assert.assertFalse("Merge", result.getMerge());
	}
	
	@Test
	public void testBypassMerge2() {
		InvocationResult result = invokeComponent("helloMerge2Service", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
        Assert.assertFalse("Merge", result.getMerge());
	}
	
	@Test
	public void testBypassMerge3() {
        InvocationResult result = invokeComponent("helloMerge3Service", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
        Assert.assertFalse("Merge", result.getMerge());
	}
	
	@Test
	public void testBypassMerge4() {
		InvocationResult result = invokeComponent("helloMerge4Service", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
        Assert.assertFalse("Merge", result.getMerge());
	}
	
	
	@Test
	public void testCleanupContextGDS1022() {
		((TestInvocationListener)GraniteContext.getCurrentInstance().getGraniteConfig().getInvocationListener()).setFailBefore(true);
		Exception error = null;
		try {
			invokeComponent("helloService", null, "hello", new Object[] { "jack" });
		}
		catch (Exception e) {
			error = e;
		}
		
		Assert.assertNotNull("Exception thrown", error);
		Assert.assertEquals("Exception thrown", "ForceFail", error.getMessage());
		Assert.assertNull("DataContext cleaned up", DataContext.get());
		Assert.assertNull("Context cleaned up", AbstractContext.instance());
	}
}
