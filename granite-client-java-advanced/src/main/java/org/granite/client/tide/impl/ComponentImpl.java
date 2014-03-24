/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ArgumentPreprocessor;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.InvocationInterceptor;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;

/**
 * Default implementation of remote component proxy
 * Generated typesafe remote service proxies should extend this class
 *
 * Component proxies are meant to be defined in a DI container (Spring/CDI) or directly in the Tide context
 * <pre>
 * {@code
 * Component myComponent = tideContext.set("myComponent", new ComponentImpl(serverSession));
 * myComponent.call("myMethod", arg1, arg2);
 * }
 * </pre>
 *
 * @author William DRAI
 */
public class ComponentImpl implements Component, ContextAware, NameAware, InvocationHandler {
    
	private static final Logger log = Logger.getLogger(ComponentImpl.class);


    private String name;
    private Context context;
    private final ServerSession serverSession;


    /**
     * Default constructor necessary for testing and CDI proxying...
     */
    public ComponentImpl() {   
    	this.serverSession = null;
    }

    /**
     * Create a new proxy attached to the specified server session
     * @param serverSession server session
     */
    public ComponentImpl(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }

    /**
     * Set the remote name of the component
     * By default the component will use its name in its owning context as the remote name
     * @param name name
     */
    public void setName(String name) {
    	this.name = name;
    }
    /**
     * Remote name of the component
     * @return name
     */
    public String getName() {
    	return name;
    }

    /**
     * Set the context where the component is set
     * @param context Tide context
     */
    public void setContext(Context context) {
    	this.context = context;
    }

    /**
     * Context where the component is set
     * @return Tide context
     */
    protected Context getContext() {
    	return context;
    }

    /**
     * Server session to which the component is attached
     * @return server session
     */
    protected ServerSession getServerSession() {
    	return serverSession;
    }
    
    
    @SuppressWarnings("unchecked")
    public <T> Future<T> call(String operation, Object... args) {
        Context context = this.context;
        
        if (args != null && args.length > 0 && args[0] instanceof Context) {
            context = (Context)args[0];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 1; i < args.length-1; i++)
            	newArgs[i-1] = args[i];
            args = newArgs;
        }
        
        return (Future<T>)callComponent(context, operation, args);
    }

    
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!method.getDeclaringClass().isAnnotationPresent(RemoteAlias.class))
			return method.invoke(proxy, args);
		
		return callComponent(getContext(), method.getName(), args);
	}


    /**
     *  Calls a remote component
     * 
     *  @param context the source context
     *  @param operation name of the called metho
     *  @param args method arguments
     *
     *  @return future returning the result of the call
     */
    @SuppressWarnings("unchecked")
	protected <T> Future<T> callComponent(Context context, String operation, Object[] args) {
    	context.checkValid();
        
        log.debug("callComponent %s.%s", getName(), operation);
        
        TideResponder<T> responder = null;
        if (args != null && args.length > 0 && args[args.length-1] instanceof TideResponder) {
        	responder = (TideResponder<T>)args[args.length-1];
            Object[] newArgs = new Object[args.length-1];
            for (int i = 0; i < args.length-1; i++)
            	newArgs[i] = args[i];
            args = newArgs;
        }
        
		// Force generation of uids by merging all arguments in the current context
        MergeContext mergeContext = context.getEntityManager().initMerge(serverSession);
        List<Object> argsList = Arrays.asList(args);
		for (int i = 0; i < args.length; i++) {
			if (argsList.get(i) instanceof PropertyHolder)
				argsList.set(i, ((PropertyHolder)args[i]).getObject());
		}
		argsList = (List<Object>)context.getEntityManager().mergeExternal(mergeContext, argsList, null, null, null, false);
		for (int i = 0; i < args.length; i++)
			args[i] = argsList.get(i);
		
        Method method = null;
        // TODO: improve method matching
        for (Method m : getClass().getMethods()) {
            if (m.getName().equals(operation) && m.getParameterTypes().length == args.length) {
                method = m;
                break;
            }
        }
        if (method != null) {
            // Call argument preprocessors if necessary before sending arguments to server
            ArgumentPreprocessor[] apps = context.allByType(ArgumentPreprocessor.class);
            if (apps != null) {
                for (ArgumentPreprocessor app : apps)
                    args = app.preprocess(method, args);
            }
        }
        
        return invoke(context, operation, args, responder);
    }

    /**
     * Execute the invocation of the remote component
     * @param context the source context
     * @param operation method name
     * @param args method arguments
     * @param tideResponder Tide responder for the remote call
     * @param <T> expected type of result
     * @return future triggered asynchronously when response is received
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> Future<T> invoke(Context context, String operation, Object[] args, TideResponder<T> tideResponder) {
        log.debug("invokeComponent %s > %s.%s", context.getContextId(), getName() != null ? getName() : getClass().getName(), operation);
        
        ComponentListener.Handler handler = new ComponentListener.Handler<T>() {
			@Override
            public Runnable result(Context context, ResultEvent event, Object info, String componentName,
                    String operation, TideResponder<T> tideResponder, ComponentListener<T> componentListener) {
            	return new ResultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentListener);
            }
            
            @Override
            public Runnable fault(Context context, FaultEvent event, Object info, String componentName,
                    String operation, TideResponder<T> tideResponder, ComponentListener<T> componentListener) {
            	return new FaultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentListener);
            }
            
            @Override
            public Runnable issue(Context context, IssueEvent event, Object info, String componentName,
                    String operation, TideResponder<T> tideResponder, ComponentListener<T> componentListener) {
            	return new FaultHandler(serverSession, context, componentName, operation, event, info, tideResponder, componentListener);
            }
        };
        ComponentListener<T> componentListener = new ComponentListenerImpl<T>(context, handler, this, operation, args, null, tideResponder);
        
        InvocationInterceptor[] interceptors = context.allByType(InvocationInterceptor.class);
        if (interceptors != null) {
            for (InvocationInterceptor interceptor : interceptors)
                interceptor.beforeInvocation(context, this, operation, args, componentListener);
        }
        
        context.getContextManager().destroyFinishedContexts();
        
//        // Force generation of uids by merging all arguments in the current context
//        for (int i = 0; i < args.length; i++) {
//            if (args[i] instanceof PropertyHolder)
//                args[i] = ((PropertyHolder)args[i]).getObject();
//            args[i] = entityManager.mergeExternal(args[i], null);
//        }
//        
//        // Call argument preprocessors before sending arguments to server
//        var method:Method = Type.forInstance(component).getInstanceMethodNoCache(op);
//        for each (var app:IArgumentPreprocessor in allByType(IArgumentPreprocessor, true))
//            componentResponder.args = app.preprocess(method, args);
        
        return componentListener.invoke(serverSession);
    }

    /**
     * Create a result event for this component
     * @param result result to wrap in an event
     * @param <T> expected type of the result
     * @return result event
     */
    public <T> TideResultEvent<T> newResultEvent(T result) {
    	return new TideResultEvent<T>(getContext(), getServerSession(), null, result);
    }
    
}
