/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.seam;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.servlet.http.HttpSession;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideServiceContext;
import org.granite.tide.TideStatusMessages;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataMergeContext;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.tide.invocation.ContextEvent;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.seam.async.AsyncContext;
import org.granite.tide.seam.lazy.SeamInitializer;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.Init.FactoryExpression;
import org.jboss.seam.core.Init.FactoryMethod;
import org.jboss.seam.framework.Home;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Reflections;


/**
 * @author William DRAI
 */
public abstract class AbstractSeamServiceContext extends TideServiceContext {

    private static final long serialVersionUID = 1L;
    
    public static final String COMPONENT_NAME = "org.granite.tide.seam.serviceContext";
    
    protected @Logger Log log;
    
    private UserEvents userEvents;
    private boolean isAsynchronousContext = true;
    
    
    private static final String RESULTS_EVAL_ATTRIBUTE = AbstractSeamServiceContext.class.getName() + "_resultsEval";
    private static final String RESULTS_EVAL_UNSPECIFIED_ATTRIBUTE = AbstractSeamServiceContext.class.getName() + "_resultsEval_unspecified";
    
    
    public AbstractSeamServiceContext() throws ServiceException {
        super();
    }
    
    /**
     * Determines the current sessionId for web invocations
     */
    @Override
    public void initCall() {
        super.initCall();
        
        if (userEvents != null)
            return;
        
        if (getSessionId() != null)
            userEvents = TideUserEvents.instance().getUserEvents(getSessionId());
        else {
            GraniteContext graniteContext = GraniteContext.getCurrentInstance();
            if (graniteContext instanceof HttpGraniteContext) {
                HttpSession session = ((HttpGraniteContext)graniteContext).getSession(false);
                if (session != null)
                    setSessionId(session.getId());
                isAsynchronousContext = false;
            }
        }
    }
    
    /**
     * Initialize current sessionId and event listeners for this context
     * 
     * @param sessionId current sessionId
     */
    @Override
    public void setSessionId(String sessionId) {
        super.setSessionId(sessionId);
        userEvents = TideUserEvents.instance().getUserEvents(sessionId);
    }
    
    /**
     * Clear current session from user events registry
     */
    @Destroy
    public void endSession() {
        if (!isAsynchronousContext && getSessionId() != null)
            TideUserEvents.instance().unregisterSession(getSessionId());
    }
    
    
    private Map<ContextResult, Boolean> getResultsEval(ScopeType scopeType) {
    	Context context = Contexts.getEventContext();
    	String att = RESULTS_EVAL_UNSPECIFIED_ATTRIBUTE;
    	if (scopeType == ScopeType.STATELESS)
    		att = RESULTS_EVAL_ATTRIBUTE;
    	else if (scopeType != ScopeType.UNSPECIFIED) {
    		context = scopeType.getContext();
    		att = RESULTS_EVAL_ATTRIBUTE;
    	}
    	
    	@SuppressWarnings("unchecked")
    	Map<ContextResult, Boolean> resultsEval = (Map<ContextResult, Boolean>)context.get(att);
    	if (resultsEval == null) {
    		resultsEval = new HashMap<ContextResult, Boolean>();
    		context.set(att, resultsEval);
    	}
    	return resultsEval;
    }
    
    
    /**
     * Constructs an asynchronous context object
     * @return current context
     */
    public AsyncContext getAsyncContext() {
    	List<ContextResult> resultsEval = new ArrayList<ContextResult>();
    	for (ScopeType evalScopeType : EVAL_SCOPE_TYPES)
    		resultsEval.addAll(getResultsEval(evalScopeType).keySet());
    	
        return new AsyncContext(getSessionId(), resultsEval);
    }
    
    /**
     * Restores an asynchronous context
     * @param asyncContext saved context
     */
    public void setAsyncContext(AsyncContext asyncContext) {
        AsyncPublisher asyncPublisher = getAsyncPublisher();
        if (asyncPublisher != null)
            asyncPublisher.initThread();
        
        Contexts.getSessionContext().set("org.jboss.seam.security.identity", asyncContext.getIdentity());
        setSessionId(asyncContext.getSessionId());
        for (ContextResult resultEval : asyncContext.getResults()) {
        	if (resultEval instanceof ScopedContextResult)
        		getResultsEval(((ScopedContextResult)resultEval).getScope()).put(resultEval, Boolean.FALSE);
        	else
        		getResultsEval(ScopeType.UNSPECIFIED).put(resultEval, Boolean.FALSE);
        }
    }
    
    
    /**
     * Retrieve current messages
     * 
     * @return list of messages
     */
    protected abstract TideStatusMessages getTideMessages();
    
    protected abstract void initTideMessages();
    
    protected abstract void clearTideMessages();
    
    
    /**
     * Implementation of component lookup for Seam service
     * 
     * @param componentName component name
     */
    @Override
    public Object findComponent(String componentName, Class<?> componentClass) {
        Component component = TideInit.lookupComponent(componentName);
        if (component == null)
            return null;
        return Component.getInstance(component.getName());
    }
    
    /**
     * Implementation of component lookup for Seam service
     * 
     * @param componentName component name
     */
    @Override
    public Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass) {
        Component component = TideInit.lookupComponent(componentName);
        if (component == null)
            return null;
        
        return componentClasses(component);
    }
    
    private Set<Class<?>> componentClasses(Object component) {
    	if (component instanceof Component) {
	    	Set<Class<?>> classes = new HashSet<Class<?>>();
	    	for (Class<?> i : ((Component)component).getBusinessInterfaces())
	    		classes.add(i);
	    	classes.add(((Component)component).getBeanClass());
	    	return classes;
    	}
    	
    	Set<Class<?>> classes = new HashSet<Class<?>>(1);
    	classes.add(component.getClass());
    	return classes;
    }
    
    
    @Observer("org.jboss.seam.beginConversation")
    public void observeBeginConversation() {
    	Contexts.getEventContext().set("org.granite.tide.conversation.wasCreated", true);
    }
    
    
    /**
     * Add an event in the current context
     * 
     * @param type event type
     * @param params event parameters
     */
    public void raiseEvent(String type, Object... params) {
        // Add the event to the current invocation
        TideInvocation tideInvocation = TideInvocation.get();
        if (tideInvocation == null)
            return;
        
        if (userEvents != null) {
            // Avoid stupid infinite loop when creating locale selector as the log needs the current locale...
            if (!type.endsWith("org.jboss.seam.international.localeSelector"))
                log.debug("Intercept event %s", type);
            String sessionId = getSessionId();
            if (sessionId != null && userEvents.hasEventType(type))
                tideInvocation.addEvent(new ContextEvent(type, params));
        }
        else if (Contexts.getSessionContext().isSet("org.granite.seam.login")) {
            // Force send of all events raised during login
            tideInvocation.addEvent(new ContextEvent(type, params));
        }
    }
    
    
    /**
     * Factory for Seam async publisher
     * 
     * @return servlet context of the current application
     */
    @Override
    protected AsyncPublisher getAsyncPublisher() {
        return (AsyncPublisher)Component.getInstance("org.granite.tide.seam.async.publisher");
    }
    
    
    /**
     * Synchronizes server context with data provided by the client
     * 
     * @param context invocation context
     * @param c client call
     * @param componentName name of the component which will be invoked
     */
    @Override
    public void prepareCall(ServiceInvocationContext context, IInvocationCall c, String componentName, Class<?> componentClass) {
        InvocationCall call = (InvocationCall)c;
        List<String> listeners = call.getListeners();
        List<ContextUpdate> updates = call.getUpdates();
        Object[] results = call.getResults();
        
        if (Contexts.isEventContextActive() && Contexts.isSessionContextActive() && 
        		(Contexts.getSessionContext().isSet("org.granite.tide.isFirstCall") || Contexts.getSessionContext().isSet("org.granite.seam.login"))) {
            // Login tried : force evaluation of existing session context
            for (Map.Entry<ContextResult, Boolean> me : getResultsEval(ScopeType.SESSION).entrySet()) {
                if (me.getKey().getExpression() == null && Contexts.getSessionContext().isSet(me.getKey().getComponentName()))
                    me.setValue(Boolean.TRUE);
            }
            Contexts.getSessionContext().remove("org.granite.seam.login");
            Contexts.getSessionContext().remove("org.granite.tide.isFirstCall");
            
            // Force quiet login
            if (Identity.instance().isLoggedIn() && Contexts.isEventContextActive())
               Contexts.getEventContext().set("org.jboss.seam.security.silentLogin", true);
        }
        if (Contexts.isEventContextActive() && Contexts.isConversationContextActive() && 
        		Contexts.getConversationContext().isSet("org.granite.tide.isFirstConversationCall")) {
            // Join conversation : force evaluation of existing conversation context
            for (Map.Entry<ContextResult, Boolean> me : getResultsEval(ScopeType.CONVERSATION).entrySet()) {
                if (me.getKey().getExpression() == null && Contexts.getConversationContext().isSet(me.getKey().getComponentName()))
                    me.setValue(Boolean.TRUE);
            }
            Contexts.getConversationContext().remove("org.granite.tide.isFirstConversationCall");
        }
        
        String sessionId = getSessionId();
        if (sessionId != null && listeners != null) {
            // Registers new event listeners
            for (String listener : listeners)
                TideUserEvents.instance().registerEventType(sessionId, listener);
            
            if (userEvents == null)
                userEvents = TideUserEvents.instance().getUserEvents(getSessionId());
        }
        
        if (results != null) {
        	Map<ContextResult, Boolean> resultsEval = getResultsEval(ScopeType.UNSPECIFIED);
            for (Object result : results) {
                ContextResult cr = (ContextResult)result;
                resultsEval.put(new ScopedContextResult(cr.getComponentName(), cr.getExpression(), ScopeType.UNSPECIFIED, null), Boolean.TRUE);
                
    //            if (!factories.containsKey(cr.getComponentName())) {
    //                FactoryExpression expr = Init.instance().getFactoryValueExpression(cr.getComponentName());
    //                if (expr != null) {
    //                    String vexpr = expr.getValueBinding().getExpressionString();
    //                    vexpr = vexpr.substring(2, vexpr.indexOf('.', 2));
    //                    factories.put(cr.getComponentName(), vexpr);
    //                    
    //                    resultsEval.put(new ContextResult(cr.getComponentName(), null), Boolean.TRUE);
    //                }
    //            }
            }
        }
        
        boolean instrumented = false;
        Component component = null;
        if (componentName != null) {
            component = TideInit.lookupComponent(componentName);
            if (component.isInterceptionEnabled())
                instrumented = true;
            
            // Forces evaluation of all results if they are related to the called component
            for (Map.Entry<ContextResult, Boolean> me : getResultsEval(component.getScope()).entrySet()) {
                if (me.getKey().getComponentName().equals(componentName))
//                    || componentName.equals(factories.get(me.getKey().getComponentName())))
                    me.setValue(Boolean.TRUE);
            }
        }
        
        initTideMessages();
        
        SeamInitializer.instance().restoreLoadedEntities();
        
        if (Conversation.instance().isLongRunning()) {
            // Merge call arguments with current conversation context
            Object[] args = context.getParameters();
            if (args != null) {
	            for (int i = 0; i < args.length; i++) {
	            	Object value = mergeExternal(args[i], null);
	            	args[i] = value;
	            }
            }
        }
        
        // Initialize an empty data context
        DataContext.init();
        
		DataUpdatePostprocessor dataUpdatePostprocessor = (DataUpdatePostprocessor)Component.getInstance("org.granite.tide.seam.data.dataUpdatePreprocessor", true);
		if (dataUpdatePostprocessor != null)
			DataContext.get().setDataUpdatePostprocessor(dataUpdatePostprocessor);
        
        // Initialize invocation context with received changes to apply to the server context and results to return 
        TideInvocation tideInvocation = TideInvocation.init();
        tideInvocation.update(updates);
        
        if (!instrumented) {
            // If no interception enabled, force the update of the context for the current component
            // In other cases it will be done by the interceptor 
            restoreContext(updates, component, null);
            tideInvocation.updated();
        }
    }

    /**
     * Builds the result object for the invocation
     * 
     * @param context invocation context
     * @param result result of the method invocation
     * @param componentName name of the invoked component
     * @return result object
     */
    @Override
    public IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass) {
        TideInvocation tideInvocation = TideInvocation.get();
        List<ContextUpdate> results = null;
        int scope = 3;
        boolean restrict = false;
        
        Component component = null;
        if (componentName != null) {
            // Determines scope of component
            component = TideInit.lookupComponent(componentName);
            if (Contexts.isMethodContextActive() && Contexts.getMethodContext().isSet(component.getName()))
                scope = 3;
            else if (Contexts.isEventContextActive() && Contexts.getEventContext().isSet(component.getName()))
            	scope = 3;
            else if (Contexts.isPageContextActive() && Contexts.getPageContext().isSet(component.getName()))
            	scope = 3;
            else if (Contexts.isConversationContextActive() && Contexts.getConversationContext().isSet(component.getName()))
            	scope = 2;
            
            restrict = component.beanClassHasAnnotation(Restrict.class);
        }
        
        if (!tideInvocation.isEvaluated()) {
            // Do evaluation now if the interceptor has not been called
            results = evaluateResults(null, null, componentName == null 
            		&& !(context != null && context.getMethod() != null && "resyncContext".equals(context.getMethod().getName())));
        }
        else
            results = tideInvocation.getResults();
        
        // Retrieve data updates made during the call
    	DataContext dataContext = DataContext.get();
		Object[][] updates = dataContext != null ? dataContext.getUpdates() : null;
		
		// Build the invocation result
        InvocationResult ires = new InvocationResult(result, results);
        ires.setScope(scope);
        ires.setRestrict(restrict);
        if (component != null) {
        	if (component.beanClassHasAnnotation(BypassTideMerge.class) || component.businessInterfaceHasAnnotation(BypassTideMerge.class))
        		ires.setMerge(false);
        	else if (context != null) {
	        	try {
	        		Method m = component.getBeanClass().getMethod(context.getMethod().getName(), context.getMethod().getParameterTypes());
	        		if (m.isAnnotationPresent(BypassTideMerge.class))
	        			ires.setMerge(false);
	        	}
	        	catch (NoSuchMethodException e) {
	        		log.warn("Could not find bean method", e);
	        	}
        		
        		for (Class<?> beanInterface : component.getBusinessInterfaces()) {
    	        	try {
    	        		Method m = beanInterface.getMethod(context.getMethod().getName(), context.getMethod().getParameterTypes());
    	        		if (m.isAnnotationPresent(BypassTideMerge.class)) {
    	        			ires.setMerge(false);
    	        			break;
    	        		}
    	        	}
    	        	catch (NoSuchMethodException e) {
    	        		log.warn("Could not find bean method", e);
    	        	}
        		}
        	}
        }
        
        if (Conversation.instance().isLongRunning()) {
	        // Put results in merge context to keep data in extended persistence context between remote calls
	        DataMergeContext.addResultEntity(result);
	        for (ContextUpdate cu : results)
	        	DataMergeContext.addResultEntity(cu.getValue());
        }
        
        ires.setUpdates(updates);
        
        // Adds events in result object
        ires.setEvents(tideInvocation.getEvents());
        
        // Save current set of entities loaded in a conversation scoped component to handle case of extended PM
        SeamInitializer.instance().saveLoadedEntities();
        
        // Adds context messages in result object
        TideStatusMessages statusMessages = getTideMessages();
        ires.setMessages(statusMessages.getMessages());
        ires.setKeyedMessages(statusMessages.getKeyedMessages());
        
        clearTideMessages();

        // Clean thread
        TideInvocation.remove();
        
        return ires;
    }

    /**
     * Intercepts a fault on the invocation
     * 
     * @param context invocation context
     * @param t exception thrown
     * @param componentName name of the invoked component
     */
    @Override
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {
        clearTideMessages();
        
        // Clean thread: very important to avoid phantom evaluations after exceptions
        TideInvocation.remove();
    }
    
    
    public void addResultEval(ScopedContextResult result) {
        getResultsEval(result.getScope()).put(result, Boolean.TRUE);
    }
    
    
    /**
     * Evaluate updates in current server context
     * 
     * @param updates list of updates
     * @param component the target component
     * @param target the target instance
     */
    public void restoreContext(List<ContextUpdate> updates, Component component, Object target) {
        if (updates == null)
            return;
        
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        
        // Restore context
        for (ContextUpdate update : updates) {
            
            log.debug("Before invocation: evaluating expression #0.#1", update.getComponentName(), update.getExpression());
            
            Component sourceComponent = TideInit.lookupComponent(update.getComponentName());
            String sourceComponentName = sourceComponent != null ? sourceComponent.getName() : update.getComponentName();
            
            Object previous = null;
            if (update.getExpression() != null) {
                // Set component property
                // Ignore expression on external components if target component is not interception enabled 
                // to avoid issues with bijection, persistence contexts and transactions
                if (target != null || (component != null && component.getName().equals(sourceComponentName))) {
                    // Get current values in Seam context
                    String[] path = update.getExpression().split("\\.");
                    Object instance = component != null && component.getName().equals(sourceComponentName) && target != null 
                    	? target 
                    	: Component.getInstance(sourceComponentName);
                    boolean disabled = instance != null && sourceComponent != null && 
                    	config.isComponentTideDisabled(sourceComponentName, componentClasses(sourceComponent), instance);
                    if (!disabled) {
                        Object bean = instance;
                        Object value = instance;
                        
                        List<Field> dmsFields = instance != null && path.length == 1 
                        	? org.granite.util.Reflections.getFields(instance.getClass(), DataModelSelection.class) : null;
                        List<String> dmsFieldNames = null;
                        if (dmsFields != null && !dmsFields.isEmpty()) {
                        	dmsFieldNames = new ArrayList<String>(dmsFields.size());
                            for (Field f : dmsFields)
                            	dmsFieldNames.add(f.getName());
                        }
                        
                        if (update.getValue() != null) {
                            boolean getPrevious = true;
                            if (update.getValue().getClass().getAnnotation(Entity.class) != null) {
                                org.granite.util.Entity entity = new org.granite.util.Entity(update.getValue());
                                if (entity.getIdentifier() == null)
                                    getPrevious = false;
                            }
                            if (getPrevious) {
                                try {
                                    for (int i = 0; i < path.length; i++) {
                                        if (value == null)
                                            break;
                                        if (i == 0 && dmsFieldNames != null && dmsFieldNames.contains(path[0])) {
                                        	Field field = org.granite.util.Reflections.getField(value.getClass(), path[i]);
                                        	field.setAccessible(true);
                                        	value = Reflections.get(field, value);
                                            if (i < path.length-1)
                                                bean = value;
                                        }
                                        else {
                                            // Use modified Reflections for getter because of a bug in Seam 2.0.0
                                            Method getter = org.granite.util.Reflections.getGetterMethod(value.getClass(), path[i]);
                                            value = Reflections.invoke(getter, value);
                                            if (i < path.length-1)
                                                bean = value;
                                        }
                                    }
                                }
                                catch (IllegalArgumentException e) {
                                    // No getter found to retrieve current value
                                    log.warn("Partial merge only: " + e.getMessage());
                                    value = null;
                                }
        	                    catch (Exception e) {
        	                    	throw new ServiceException("Could not get property: " + update.toString(), e);
        	                    }
                                previous = value;
                            }
                        }
                        
                        // Set new value
                        try {
	                        if (bean != null && path.length == 1 && dmsFieldNames != null && dmsFieldNames.contains(path[0])) {
	                        	Field field = org.granite.util.Reflections.getField(bean.getClass(), path[0]);
	                        	field.setAccessible(true);
	                            value = GraniteContext.getCurrentInstance().getGraniteConfig().getConverters().convert(update.getValue(), field.getType());
	                            // Merge entities into current persistent context if needed
	                            value = mergeExternal(value, previous);
	                        	Reflections.set(field, bean, value);
	                        }
	                        else if (bean != null) {
	                            Method setter = Reflections.getSetterMethod(bean.getClass(), path[path.length-1]);
	                            Type type = setter.getParameterTypes()[0];
	                        	if (bean instanceof Home<?, ?> && "id".equals(path[path.length-1])) {
	                        		// Special (ugly ?) handling for Home object to try to guess id type (setId is of type Object)
	                        		try {
	                        			Class<?> entityClass = ((Home<?, ?>)bean).getEntityClass();
	                        			org.granite.util.Entity entity = new org.granite.util.Entity(entityClass);
	                        			type = entity.getIdentifierType();
	                        		}
	                        		catch (Exception e) {
	                        			// Ignore
	                        		}
	                        	}
	                            value = GraniteContext.getCurrentInstance().getGraniteConfig().getConverters().convert(update.getValue(), type);
	                            // Merge entities into current persistent context if needed
	                            value = mergeExternal(value, previous);
	                            Reflections.invoke(setter, bean, value);
	                        }
	                    }
	                    catch (Exception e) {
	                    	throw new ServiceException("Could not restore property: " + update.toString(), e);
	                    }
                    }
                }
            }
            else {
                // Set context variable
                if (sourceComponent != null) {
                    ScopeType scope = sourceComponent.getScope();
                    if (!((update.getScope() == 2 && (scope == ScopeType.EVENT 
                                || scope == ScopeType.STATELESS 
                                || scope == ScopeType.CONVERSATION
                                || scope == ScopeType.BUSINESS_PROCESS
                                || scope == ScopeType.METHOD
                                || scope == ScopeType.PAGE))
                        || (update.getScope() == 1 && (scope == ScopeType.EVENT
                                || scope == ScopeType.STATELESS 
                                || scope == ScopeType.SESSION
                                || scope == ScopeType.METHOD
                                || scope == ScopeType.PAGE))
                        || (update.getScope() == 3 && (scope == ScopeType.EVENT
                        		|| scope == ScopeType.STATELESS
                        		|| scope == ScopeType.METHOD
                        		|| scope == ScopeType.PAGE)))) {
                        scope = ScopeType.EVENT;
                    }
                    
                    previous = scope.getContext().get(sourceComponentName);
                    
                    boolean disabled = previous != null && config.isComponentTideDisabled(sourceComponentName, componentClasses(sourceComponent), previous);
                    if (!disabled) {
                        Object value = mergeExternal(update.getValue(), previous);
                        
                        scope.getContext().set(sourceComponentName, value);
                    }
                }
                else {
                    Object[] prev = lookupInStatefulContexts(sourceComponentName, ScopeType.UNSPECIFIED);
                    ScopeType scope = ScopeType.UNSPECIFIED;
                    if (prev != null) {
                        previous = prev[0];
                        scope = (ScopeType)prev[1];
                    }
                    
                    boolean disabled = previous != null && config.isComponentTideDisabled(sourceComponentName, 
                    		componentClasses(previous), previous);
                    if (!disabled) {
                        if (scope == ScopeType.UNSPECIFIED) {
                            scope = ScopeType.EVENT;
                            scope.getContext().set(sourceComponentName + "_tide_unspecified_", true);
                        }
                        
                        Object value = mergeExternal(update.getValue(), previous);
                        
                        scope.getContext().set(sourceComponentName, value);
                    }
                }
            }
        }
    }
    
    
    private static final ScopeType[] EVAL_SCOPE_TYPES = {
    	ScopeType.UNSPECIFIED,
    	ScopeType.EVENT,
    	ScopeType.CONVERSATION,
    	ScopeType.SESSION,
    	ScopeType.BUSINESS_PROCESS,
    	ScopeType.APPLICATION
    };
    
    /**
     * Evaluate results from context
     * 
     * @param component the target component
     * @param target the target instance
     * @param nothing used by initializer to avoid interactions with context sync
     * 
     * @return list of updates to send back to the client
     */
    public List<ContextUpdate> evaluateResults(Component component, Object target, boolean nothing) {
        
        List<ContextUpdate> resultsMap = new ArrayList<ContextUpdate>();
        
        if (nothing)
            return resultsMap;
        
        List<String> exprs = new ArrayList<String>();
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        
        SeamInitializer.instance();
        
        for (ScopeType evalScopeType : EVAL_SCOPE_TYPES) {
	        for (Map.Entry<ContextResult, Boolean> me : getResultsEval(evalScopeType).entrySet()) {
	            if (!me.getValue())
	                continue;
	            
	            ContextResult res = me.getKey();
	            Component targetComponent = TideInit.lookupComponent(res.getComponentName());
	            
	            String targetComponentName = targetComponent != null ? targetComponent.getName() : res.getComponentName();
	            
	            if (res.getExpression() != null && component != null && targetComponent != null && !(component.getName().equals(targetComponentName))) {
	                // Ignore results concerning other components for this time
	                // In this case is has to be a non interception enabled component
	                continue;
	            }
	            
	            int idx = 0;
	            boolean add = true;
	            
	            // Force evaluation of consecutive properties
	            while (idx >= 0) {
	                idx = res.getExpression() != null ? res.getExpression().indexOf(".", idx+1) : -1;
	                
	                String expr = (idx > 0 ? res.getExpression().substring(0, idx) : res.getExpression());
	                String ex = expr != null ? res.getComponentName() + "." + expr : res.getComponentName();
	                
	                if (!exprs.contains(ex)) {
	                    log.debug("After invocation: evaluating expression #0", ex);
	                    
	                    String[] path = expr != null ? expr.split("\\.") : new String[0];
	                    
	                    try {
	                        Object value = null;
	                        ScopeType scopeType = res instanceof ScopedContextResult ? ((ScopedContextResult)res).getScope() : ScopeType.UNSPECIFIED;
	                        Boolean restrict = res.getRestrict();
	                        
            	            FactoryMethod factoryMethod = null;
            	            FactoryExpression factoryExpression = null;
            	            if (targetComponent == null) {
            	            	factoryExpression = TideInit.lookupFactoryExpression(res.getComponentName());
            	            	if (factoryExpression == null)
            	            		factoryMethod = TideInit.lookupFactory(res.getComponentName());
            	            }
            	            
                            if (targetComponent != null) {
                            	if (component != null && component.getName().equals(targetComponent.getName())) {
	                                value = target;
	                                scopeType = targetComponent.getScope();
	                            }
                            	else if (ScopeType.UNSPECIFIED.equals(scopeType)) {
                                    value = Component.getInstance(targetComponent.getName());
                                    scopeType = targetComponent.getScope();
                                    if (ScopeType.STATELESS.equals(scopeType))
                                        scopeType = ScopeType.EVENT;
                                    
                                    if (value != null && config.isComponentTideDisabled(targetComponentName, 
                                    		componentClasses(targetComponent), value))
                                        add = false;
                                }
                                else {
                                    value = Component.getInstance(targetComponent.getName(), scopeType);
                                    
                                    if (value != null && config.isComponentTideDisabled(targetComponentName, 
                                    		componentClasses(value), value))
                                        add = false;
                                }
                                
                                restrict = targetComponent.beanClassHasAnnotation(Restrict.class);
                            }
                            else if (factoryExpression != null) {
                                String expressionString = factoryExpression.getMethodBinding() != null 
                            		? factoryExpression.getMethodBinding().getExpressionString() 
                            		: factoryExpression.getValueBinding().getExpressionString();
                            	int iedx = expressionString.indexOf(".");
                            	String expressionBaseName = expressionString.substring(2, iedx);
                            	
                                if (ScopeType.UNSPECIFIED.equals(scopeType)) {
                                    value = Component.getInstance(res.getComponentName());
                                    scopeType = factoryExpression.getScope();
                                    if (ScopeType.STATELESS.equals(scopeType))
                                        scopeType = ScopeType.EVENT;
                                    
                                    if (value != null && config.isComponentTideDisabled(expressionBaseName, componentClasses(value), value))
                                        add = false;
                                }
                                else {
                                    value = Component.getInstance(res.getComponentName(), scopeType);
                                    
                                    if (value != null && config.isComponentTideDisabled(expressionBaseName, componentClasses(value), value))
                                        add = false;
                                }
                                
                                Component factoryComponent = TideInit.lookupComponent(expressionBaseName);
                            	restrict = factoryComponent != null ? factoryComponent.beanClassHasAnnotation(Restrict.class) : false;
                            }
                            else if (factoryMethod != null) {
                                if (ScopeType.UNSPECIFIED.equals(scopeType)) {
                                    value = Component.getInstance(factoryMethod.getMethod().getAnnotation(Factory.class).value());
                                    scopeType = factoryMethod.getScope();
                                    if (ScopeType.STATELESS.equals(scopeType))
                                        scopeType = ScopeType.EVENT;
                                    
                                    if (value != null && config.isComponentTideDisabled(factoryMethod.getComponent().getName(), 
                                    		componentClasses(factoryMethod.getComponent()), value))
                                        add = false;
                                }
                                else {
                                    value = Component.getInstance(res.getComponentName(), scopeType);
                                    
                                    if (value != null && config.isComponentTideDisabled(factoryMethod.getComponent().getName(), 
                                    		componentClasses(value), value))
                                        add = false;
                                }

                            	restrict = factoryMethod.getComponent().beanClassHasAnnotation(Restrict.class);
                            }
                            else {
                                Object[] val = lookupInStatefulContexts(res.getComponentName(), scopeType);
                                if (val != null) {
                                    value = val[0];
                                    scopeType = (ScopeType)val[1];
                                    
                                    if (value != null && config.isComponentTideDisabled(res.getComponentName(), 
                                    		componentClasses(value), value))
                                        add = false;
                                }
                            }
	                        
                            if (add) {
                            	Object v0 = null;
                            	String propName = null;
                                for (int i = 0; i < path.length; i++) {
                                    if (value == null)
                                        break;
                                    // Use modified Reflections for getter because of a bug in Seam 2.0.0
                                    v0 = value;
                                    propName = path[i];
                                    Method getter = null;
                                    try {
                                    	getter = org.granite.util.Reflections.getGetterMethod(value.getClass(), path[i]);
                                    }
                                    catch (IllegalArgumentException e) {
                                    	// GDS-566
                                    }
                                    if (getter != null)
                                    	value = Reflections.invoke(getter, value);
                                }
                                
	                        	getResultsEval(scopeType).put(res, false);
	                        	
	                            if (value instanceof TideDataModel) {
	                                // Unwrap value
	                                value = ((TideDataModel)value).getWrappedData();
	                            }
	                            else if (value != null) {
	                                if (classGetter != null) {
	                                    classGetter.initialize(v0, propName, value);
	                                    if (res.getExpression() != null) {
	                                        String[] fullPath = res.getExpression().split("\\.");
	                                        Object v = value;
	                                        for (int i = path.length; i < fullPath.length; i++) {
	                                            // Use modified Reflections for getter because of a bug in Seam 2.0.0
	                                            Method getter = org.granite.util.Reflections.getGetterMethod(v.getClass(), fullPath[i]);
	                                            v0 = v;
	                                            v = Reflections.invoke(getter, v);
//	                                            if (v == null)
//	                                                break;
	                                            classGetter.initialize(v0, fullPath[i], v);
	                                            if (v == null)
	                                            	break;
	                                        }
	                                    }
	                                }
	                            }
	                        
	                            int scope = (scopeType == ScopeType.CONVERSATION ? 2 : (scopeType == ScopeType.SESSION ? 1 : 3));
	                            
	                            resultsMap.add(new ContextUpdate(res.getComponentName(), expr, value, scope, Boolean.TRUE.equals(restrict)));
	                            add = false;
	                        }
	                        
	                        exprs.add(ex);
	                    }
	                    catch (Exception e) {
	                        throw new ServiceException("Could not evaluate result expression: " + ex, e);
	                    }
	                }
	            }
	            
	            me.setValue(Boolean.FALSE);
	        }
        }
        
        return resultsMap;
    }
    
    
    /**
     * Implementations of intercepted asynchronous calls
     * Send asynchronous event to client
     * @param asyncContext current context (session id)
     * @param targetComponentName target component name
     * @param methodName method name
     * @param paramTypes method argument types
     * @param params argument values
     * @return result
     */
    public Object invokeAsynchronous(AsyncContext asyncContext, String targetComponentName, Class<?> targetComponentClass, String methodName, Class<?>[] paramTypes, Object[] params) {
        setAsyncContext(asyncContext);
        
        // Just another ugly hack: the Seam interceptor has set this variable and we don't want it
        Contexts.getEventContext().remove("org.jboss.seam.async.AsynchronousIntercepter.REENTRANT");
        
        Component component = TideInit.lookupComponent(targetComponentName);
        
        // Forces evaluation of all results if they are related to the called component
        for (Map.Entry<ContextResult, Boolean> me : getResultsEval(component.getScope()).entrySet()) {
            if (me.getKey().getComponentName().equals(targetComponentName))
                me.setValue(Boolean.TRUE);
        }
        
        Object target = Component.getInstance(targetComponentName);
        
        Method method;
        try {
            method = target.getClass().getMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException nsme) {
           throw new IllegalStateException(nsme);
        }
        
        Object result = Reflections.invokeAndWrap(method, target, params);
        
        sendEvent(targetComponentName, targetComponentClass);
        
        return result;
    }
    

    @Override
    protected TidePersistenceManager getTidePersistenceManager(boolean create) {
        return SeamInitializer.instance().getTidePersistenceManager(); 
    }
    
    
    /**
     * Search for a named attribute in all contexts, in the
     * following order: method, event, page, conversation,
     * session, business process, application.
     * 
     * @return the first component found, or null
     */
    public static Object[] lookupInStatefulContexts(String name, ScopeType scope) {
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.METHOD.equals(scope)) && Contexts.isMethodContextActive()) {
            Object result = Contexts.getMethodContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getMethodContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.EVENT.equals(scope)) && Contexts.isEventContextActive()) {
            Object result = Contexts.getEventContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getEventContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.PAGE.equals(scope)) && Contexts.isPageContextActive()) {
            Object result = Contexts.getPageContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getPageContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.CONVERSATION.equals(scope)) && Contexts.isConversationContextActive()) {
            Object result = Contexts.getConversationContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getConversationContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.SESSION.equals(scope)) && Contexts.isSessionContextActive()) {
            Object result = Contexts.getSessionContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getSessionContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.BUSINESS_PROCESS.equals(scope)) && Contexts.isBusinessProcessContextActive()) {
            Object result = Contexts.getBusinessProcessContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getBusinessProcessContext().getType() };
        }
        
        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.APPLICATION.equals(scope)) && Contexts.isApplicationContextActive()) {
            Object result = Contexts.getApplicationContext().get(name);
            if (result != null)
                return new Object[] { result, Contexts.getApplicationContext().getType() };
        }
        
        return ScopeType.UNSPECIFIED.equals(scope) ? null : new Object[] { null, scope };
    }
}
