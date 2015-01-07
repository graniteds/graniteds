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

import java.util.HashMap;
import java.util.Map;

import org.granite.test.tide.data.Contact;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration
public class TestTideRemotingController extends AbstractTideTestCase {
    
	@Test
    public void testSimpleCall() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "joe");
        InvocationResult result = invokeComponent("helloController", null, "hello", new Object[] { params });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testSimpleCall2() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "joe");
        InvocationResult result = invokeComponent("hello", null, "hello", new Object[] { params });
        
        Assert.assertEquals("Hello joe", result.getResult());
    }	
	
	@Test
    public void testObjectCall() {
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent("hello2Controller", null, "hello", new Object[] {}, 
        		new Object[] { new Object[] { "contact", null, c } },
        		new String[0], null 
        );
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testObjectCallLocal() {
		Map<String, Object> params = new HashMap<String, Object>();
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent("hello2Controller", null, "hello", new Object[] { params, true }, 
        		new Object[] { new Object[] { "hello2Controller", "contact", c } },
        		new String[0], null 
        );
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testObjectCallBody() {
		Map<String, Object> params = new HashMap<String, Object>();
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent("hello3Controller", null, "hello", new Object[] { c, params, true }, 
        		new Object[] { new Object[] { "hello3Controller", "contact", c } },
        		new String[0], null 
        );
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testObjectCallBody2() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("greet", "Hello");
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent("hello3Controller", null, "hello2", new Object[] { c, params, true }, 
        		new Object[] { new Object[] { "hello3Controller", "contact", c } },
        		new String[0], null 
        );
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
	
	@Test
    public void testObjectInterfaceCallBody() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("greet", "Hello");
		Contact c = new Contact();
		c.setEmail("joe");
        InvocationResult result = invokeComponent("hello4", null, "hello2", new Object[] { c, params }, 
        		new Object[] {}, new String[0], null 
        );
        
        Assert.assertEquals("Hello joe", result.getResult());
    }
}
