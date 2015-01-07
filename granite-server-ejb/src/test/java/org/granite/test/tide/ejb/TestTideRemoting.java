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
package org.granite.test.tide.ejb;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.test.tide.TestInvocationListener;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.util.AbstractContext;
import org.junit.Assert;
import org.junit.Test;


public class TestTideRemoting extends AbstractTideTestCase {
    
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
		((TestInvocationListener)((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getInvocationListener()).setFailBefore(true);
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
