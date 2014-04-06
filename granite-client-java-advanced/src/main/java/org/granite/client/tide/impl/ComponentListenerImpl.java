/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.FaultException;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResponder;
import org.granite.tide.invocation.InvocationCall;

/**
 * @author William DRAI
 */
public class ComponentListenerImpl<T> implements ComponentListener<T> {
    
    private Context sourceContext;
    private Component component;
    private String componentName;
    private String operation;
    private Object[] args;
    private Handler<T> handler;
    private TideResponder<T> tideResponder;
    private Object info;
    private boolean waiting = false;
    private Runnable responseHandler = null;
    private T result = null;
    private Exception exception = null;
    
    
    public ComponentListenerImpl(Context sourceContext, Handler<T>handler, Component component, String operation, Object[] args, Object info, TideResponder<T> tideResponder) {        
        this.sourceContext = sourceContext;
        this.handler = handler;
        this.component = component;
        this.componentName = component != null ? component.getName() : null;
        this.operation = operation;
        this.args = args;
        this.tideResponder = tideResponder;
        this.info = info;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public Object[] getArgs() {
        return args;
    }
    public void setArgs(Object[] args) {
        this.args = args;
    }
    
    public Context getSourceContext() {
        return sourceContext;
    }
    
    public Component getComponent() {
        return component;
    }
    
    public T getResult() throws InterruptedException, ExecutionException {
        synchronized (this) {
        	if (responseHandler == null && exception == null) {
	        	waiting = true;
	        	wait();
        	}
        	if (responseHandler != null)
        		responseHandler.run();
        }
        if (exception instanceof ExecutionException)
        	throw (ExecutionException)exception;
        else if (exception instanceof InterruptedException)
        	throw (InterruptedException)exception;
    	return result;
    }
    
    public void setResult(T result) {
    	this.result = result;
    }
    
	@Override
    public void onResult(ResultEvent event) {
		Runnable h = handler.result(sourceContext, event, info, componentName, operation, tideResponder, this);
        synchronized (this) {
    		responseHandler = h;
        	if (waiting)
        		notifyAll();
        	else
        		sourceContext.callLater(h);
        }
    }
    
	@Override
    public void onFault(FaultEvent event) {
		Runnable h = handler.fault(sourceContext, event, info, componentName, operation, tideResponder, this);
		synchronized (this) {
			responseHandler = h;
			exception = new FaultException(event);
			if (waiting)
				notifyAll();
			else
        		sourceContext.callLater(h);
		}
    }

	@Override
	public void onFailure(final FailureEvent event) {
		Runnable h = handler.issue(sourceContext, event, info, componentName, operation, tideResponder, this);
		synchronized (this) {
			exception = new ExecutionException(event.getCause());
			if (waiting)
				notifyAll();
			else
				sourceContext.callLater(h);
		}
	}

	@Override
	public void onTimeout(TimeoutEvent event) {
		Runnable h = handler.issue(sourceContext, event, info, componentName, operation, tideResponder, this);
		synchronized (this) {
			exception = new InterruptedException("timeout");
			if (waiting)
				notifyAll();
			else
				sourceContext.callLater(h);
		}
	}

	@Override
	public void onCancelled(CancelledEvent event) {
		Runnable h = handler.issue(sourceContext, event, info, componentName, operation, tideResponder, this);
		synchronized (this) {
			exception = new InterruptedException("cancel");
			if (waiting)
				notifyAll();
			else
				sourceContext.callLater(h);
		}
	}
	
	@Override
    public Future<T> invoke(ServerSession serverSession) {
        
    	Object[] call = new Object[5];
    	call[0] = getComponent().getName();
    	String componentClassName = null;
    	if (getComponent().getClass() != ComponentImpl.class) {
    		RemoteAlias remoteAlias = getComponent().getClass().getAnnotation(RemoteAlias.class);
    		componentClassName = remoteAlias != null ? remoteAlias.value() : getComponent().getClass().getName();
    	}
    	call[1] = componentClassName;
    	call[2] = getOperation();
    	call[3] = getArgs();
    	call[4] = new InvocationCall();
    	
        RemoteService ro = serverSession.getRemoteService();
        ResponseMessageFuture rmf = ro.newInvocation("invokeComponent", call).addListener(this).invoke();
        
        serverSession.checkWaitForLogout();
        
        return new FutureResult<T>(rmf, this);
    }

}
