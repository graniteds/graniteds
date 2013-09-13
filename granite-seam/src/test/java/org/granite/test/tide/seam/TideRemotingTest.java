/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
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
