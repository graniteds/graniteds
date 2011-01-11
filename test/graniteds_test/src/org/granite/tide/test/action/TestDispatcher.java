package org.granite.tide.test.action;

import java.util.concurrent.Future;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.async.ThreadPoolDispatcher;
import org.jboss.seam.contexts.Contexts;

@Scope(ScopeType.APPLICATION)
@Name("org.jboss.seam.async.dispatcher")
@Install(precedence=Install.FRAMEWORK)
public class TestDispatcher extends ThreadPoolDispatcher {
	
	@Override
	public Future<?> scheduleAsynchronousEvent(String type, Object... parameters) {
		Future<?> future = super.scheduleAsynchronousEvent(type, parameters);
		Contexts.getEventContext().set("future", future);
		return future;
   }

}
