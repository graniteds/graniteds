package org.granite.test.tide.seam.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;


@Name("helloWorld")
@Scope(ScopeType.STATELESS)
public class HelloWorldAction {
    
    public String hello(String name) {
        return "Hello " + name;
    }
    
    public void sendEvent(String name) {
    	Events.instance().raiseEvent("testEvent", name);
    }
}
