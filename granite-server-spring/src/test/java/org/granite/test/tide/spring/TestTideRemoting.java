/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.spring;

import java.util.Map;

import org.granite.collections.BasicMap;
import org.granite.test.tide.data.Contact;
import org.granite.test.tide.spring.service.Hello2Service;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration
public class TestTideRemoting extends AbstractTideTestCase {
    
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
	
//	@Test
//	@Ignore
//	public void testAmbiguousMethod() {
//        InvocationResult result = invokeComponent("testService", null, "create", new Object[] { new Person() },
//                new Object[0], new String[0], null);
//        Assert.assertEquals("Result", "person", result.getResult());
//	}
}
