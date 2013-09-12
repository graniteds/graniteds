package org.granite.test.tide.seam;

import java.util.Map;

import org.granite.collections.BasicMap;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.test.tide.data.Contact;
import org.granite.test.tide.data.Person;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class TideRemotingTest extends AbstractTideTestCase {
    
	@Test
    public void testSimpleCall() {
        InvocationResult result = invokeComponent("helloWorld", "hello", new Object[] { "joe" });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
    
	@Test
    public void testSimpleCallNoComponent() {
        boolean forbidden = false;
        try {
            invokeComponent("forbidden", "hello", new Object[] { "joe" });
        }
        catch (SecurityServiceException e) {
            forbidden = true;
        }
        Assert.assertTrue(forbidden);
    }
    
	@Test
    public void testInjectedCall() {
        Person person = new Person();
        person.initIdUid(12L, null);
        person.setLastName("test");
        InvocationResult result = invokeComponent("hello", "hello", new Object[0],
                new Object[] { new Object[] { "person", null, person, true }}, new String[0], null);
        Assert.assertEquals("Hello test", result.getResult());
    }
    
	@Test
    public void testInjectedCallNoComponent() {
        Contact contact = new Contact();
        contact.initIdUid(12L, null);
        contact.setEmail("test@test.com");
        InvocationResult result = invokeComponent("hello2", "hello", new Object[0],
                new Object[] { new Object[] { "contact", null, contact, true }}, new String[0], null);
        Assert.assertEquals("Hello test@test.com", result.getResult());
    }
    
	@Test
    public void testOutjectedCall() {
        InvocationResult result = invokeComponent("createPerson", "create", new Object[] { "test" },
                new Object[0], new String[0], null);
        Assert.assertEquals(1, result.getResults().size());
        Assert.assertEquals("person", result.getResults().get(0).getComponentName());
        Assert.assertNull(result.getResults().get(0).getExpression());
        Assert.assertTrue(result.getResults().get(0).getValue() instanceof Person);
        Assert.assertEquals("test", ((Person)result.getResults().get(0).getValue()).getLastName());
    }
    
	@Test
    public void testMessagesCall() {
        InvocationResult result = invokeComponent("createPerson", "create", new Object[] { "test" },
                new Object[0], new String[0], null);
        Assert.assertEquals(1, result.getMessages().size());
    }
	
	@Test
	public void testRemotingMapGDS560() {
		Map<String, Object[]> map = new BasicMap<String, Object[]>();
		map.put("toto", new Object[] { "toto", "tutu" });
        InvocationResult result = invokeComponent("hello3", "setMap", new Object[] { map },
                new Object[0], new String[0], null);
        Assert.assertEquals("Result", "ok", result.getResult());
	}
}
