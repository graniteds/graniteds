package org.granite.tide.test;

import java.util.concurrent.Future;

import org.granite.tide.invocation.InvocationResult;
import org.jboss.seam.contexts.Contexts;
import org.junit.Assert;
import org.junit.Test;


public class TideEventTest extends TideTestCase {
    
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
