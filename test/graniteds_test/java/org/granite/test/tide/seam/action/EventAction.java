package org.granite.test.tide.seam.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;


@Name("testEvent")
@Scope(ScopeType.STATELESS)
public class EventAction {
    
    public void raiseEvent(String name) {
    	Events.instance().raiseEvent("testEvent", name);
    }
    
    public void raiseAsynchronousEvent(String name) {
    	Events.instance().raiseAsynchronousEvent("testAsyncEvent", name);
    }
}
