package org.granite.test.tide.spring;

import java.util.Map;

import org.granite.collections.BasicMap;
import org.granite.test.tide.spring.entity.Contact;
import org.granite.test.tide.spring.entity.Person;
import org.granite.test.tide.spring.service.Hello2Service;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class TideRemotingTest extends AbstractTideTestCase {
    
	@Test
    public void testSimpleCall() {
        InvocationResult result = invokeComponent("hello", null, "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testTypesafeCall() {
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent(null, Hello2Service.class, "hello", new Object[] { c });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
	public void testRemotingMapGDS560() {
		Map<String, Object[]> map = new BasicMap<String, Object[]>();
		map.put("toto", new Object[] { "toto", "tutu" });
        InvocationResult result = invokeComponent("hello3", null, "setMap", new Object[] { map },
                new Object[0], new String[0], null);
        Assert.assertEquals("Result", "ok", result.getResult());
	}
	
	@Test
	@Ignore
	public void testAmbiguousMethod() {
        InvocationResult result = invokeComponent("testService", null, "create", new Object[] { new Person() },
                new Object[0], new String[0], null);
        Assert.assertEquals("Result", "person", result.getResult());
	}
}
