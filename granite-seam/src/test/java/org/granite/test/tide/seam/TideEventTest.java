/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.seam;

import java.util.concurrent.Future;

import org.granite.tide.invocation.InvocationResult;
import org.jboss.seam.contexts.Contexts;
import org.junit.Assert;
import org.junit.Test;


public class TideEventTest extends AbstractTideTestCase {
    
	@Test
    public void testSimpleEvent() {
        InvocationResult result = invokeComponent("testEvent", "raiseEvent", new Object[] { "hello" }, new String[] { "testEvent" }, null, null, null);
        
        Assert.assertEquals("Events raised", 1, result.getEvents().size());
        Assert.assertEquals("Event type", "testEvent", result.getEvents().get(0).getEventType());
        Assert.assertEquals("Event param", "hello", result.getEvents().get(0).getParams()[0]);
    }
    
	@Test
    public void testAsyncEvent() throws Exception {
        invokeComponent("testEvent", "raiseAsynchronousEvent", new Object[] { "hello" }, new String[] { "testAsyncEvent" }, null, null, null);
        
        Future<?> future = (Future<?>)Contexts.getEventContext().get("future");
        future.get();
        
        InvocationResult result = (InvocationResult)getLastMessage().getBody();
        Assert.assertEquals("Events raised", 1, result.getEvents().size());
        Assert.assertEquals("Event type", "testAsyncEvent", result.getEvents().get(0).getEventType());
        Assert.assertEquals("Event param", "hello", result.getEvents().get(0).getParams()[0]);
    }
}
