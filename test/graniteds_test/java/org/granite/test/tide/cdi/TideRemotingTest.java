package org.granite.test.tide.cdi;

import java.util.Map;

import org.granite.collections.BasicMap;
import org.granite.test.tide.cdi.entity.Contact;
import org.granite.test.tide.cdi.service.Hello2Service;
import org.granite.test.tide.cdi.service.Hello3Service;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class TideRemotingTest extends AbstractTideTestCase {
    
	@Test
    public void testSimpleCall() {
        InvocationResult result = invokeComponent("helloService", null, "hello", new Object[] { "joe" });
        
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
        InvocationResult result = invokeComponent(null, Hello3Service.class, "setMap", new Object[] { map },
                new Object[0], new String[0], null);
        Assert.assertEquals("Result", "ok", result.getResult());
	}
}
