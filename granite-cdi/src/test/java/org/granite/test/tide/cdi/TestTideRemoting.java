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
package org.granite.test.tide.cdi;

import java.util.Map;

import org.granite.collections.BasicMap;
import org.granite.test.tide.cdi.service.Hello2Service;
import org.granite.test.tide.cdi.service.Hello3Service;
import org.granite.test.tide.data.Contact;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class TestTideRemoting extends AbstractTideTestCase {
    
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
