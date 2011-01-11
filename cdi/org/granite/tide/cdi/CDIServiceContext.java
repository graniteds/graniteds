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

package org.granite.tide.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideServiceContext;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.cdi.lazy.CDIInitializer;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.ContextEvent;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.ClassUtil;


/**
 * @author William DRAI
 */
@SessionScoped
public class CDIServiceContext extends TideServiceContext {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(CDIServiceContext.class);
    
    @SuppressWarnings("serial")
    private static final AnnotationLiteral<Any> ANY_LITERAL = new AnnotationLiteral<Any>() {}; 
    @SuppressWarnings("serial")
    private static final AnnotationLiteral<Default> DEFAULT_LITERAL = new AnnotationLiteral<Default>() {}; 
    
    private @Inject BeanManager manager;
    
    private UserEvents userEvents;
    private @Inject TideUserEvents tideUserEvents;
    private boolean isAsynchronousContext = true;
    private boolean isFirstCall = false;
    private boolean isLogin = false;
    private @Inject CDIInitializer tideEntityInitializer;
    
    
    /**
     * Determines the current sessionId for web invocations
     */
    @Override
    public void initCall() {
        super.initCall();
        
        if (userEvents != null)
            return;
        
        if (getSessionId() != null)
            userEvents = tideUserEvents.getUserEvents(getSessionId());
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
        userEvents = tideUserEvents.getUserEvents(sessionId);
    }
    
    /**
     * Clear current session from user events registry
     */
    @PreDestroy
    public void endSession() {
        if (!isAsynchronousContext && getSessionId() != null)
            tideUserEvents.unregisterSession(getSessionId());
    }
    
    
    @Inject
    private ResultsEval resultsEval;
    
    private Map<ContextResult, Boolean> getResultsEval() {
    	return resultsEval.getResultsEval();
    }
    
    
//    /**
//     * Constructs an asynchronous context object
//     * @return current context
//     */
//    public AsyncContext getAsyncContext() {
//    	List<ContextResult> resultsEval = new ArrayList<ContextResult>();
//    	for (ScopeType evalScopeType : EVAL_SCOPE_TYPES)
//    		resultsEval.addAll(getResultsEval(evalScopeType).keySet());
//    	
//        return new AsyncContext(getSessionId(), resultsEval);
//    }
//    
//    /**
//     * Restores an asynchronous context
//     * @param asyncContext saved context
//     */
//    public void setAsyncContext(AsyncContext asyncContext) {
//        AsyncPublisher asyncPublisher = getAsyncPublisher();
//        if (asyncPublisher != null)
//            asyncPublisher.initThread();
//        
//        Contexts.getSessionContext().set("org.jboss.seam.security.identity", asyncContext.getIdentity());
//        setSessionId(asyncContext.getSessionId());
//        for (ContextResult resultEval : asyncContext.getResults()) {
//        	if (resultEval instanceof ScopedContextResult)
//        		getResultsEval(((ScopedContextResult)resultEval).getScope()).put(resultEval, Boolean.FALSE);
//        	else
//        		getResultsEval(ScopeType.UNSPECIFIED).put(resultEval, Boolean.FALSE);
//        }
//    }
    
    
    /**
     * Implementation of component lookup for CDI service
     * 
     * @param componentName component name
     */
    @Override
    public Object findComponent(String componentName, Class<?> componentClass) {
    	Bean<?> bean = findBean(componentName, componentClass);
    	if (bean == null)
    		return null;
    	CreationalContext<?> cc = manager.createCreationalContext(bean);
    	return manager.getReference(bean, Object.class, cc);
    }
    
    /**
     * Implementation of component lookup for CDI service
     * 
     * @param componentName component name
     */
    @Override
    public Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass) {
    	Bean<?> bean = findBean(componentName, componentClass);
    	return beanClasses(bean);
    }
    
    private Bean<?> findBean(String componentName, Class<?> componentClass) {
    	if (componentClass != null) {
        	Set<Bean<?>> beans = manager.getBeans(componentClass, ANY_LITERAL);
        	// If only one match, return match
        	if (beans.size() == 1)
        		return beans.iterator().next();
    	}

    	if (componentName != null && !("".equals(componentName))) {
	    	// If no previous match, return by name
	    	Set<Bean<?>> beans = manager.getBeans(componentName);
	    	if (!beans.isEmpty())
	    		return beans.iterator().next();
    	}
    	
    	if (componentClass != null) {
	    	// If more than one match and no named bean, return the Default one
	    	Set<Bean<?>> beans = manager.getBeans(componentClass, DEFAULT_LITERAL);
	    	if (beans.size() == 1)
	    		return beans.iterator().next();
    	}
    	
    	return null;
    }
    
    private Set<Class<?>> beanClasses(Object bean) {
    	if (bean instanceof Bean<?>) {
	    	Set<Class<?>> classes = new HashSet<Class<?>>();
	    	for (Type type : ((Bean<?>)bean).getTypes()) {
	    		if (type instanceof Class<?>)
	    			classes.add((Class<?>)type);
	    	}
	    	return classes;
    	}
    	
    	Set<Class<?>> classes = new HashSet<Class<?>>(1);
    	classes.add(bean.getClass());
    	return classes;
    }
    
    
//    public void observeBeginConversation(@Observes org.jboss.webbeans.conversation.) {
//    	Contexts.getEventContext().set("org.granite.tide.conversation.wasCreated", true);
//    }
    
    
    /**
     * Add an event in the current context
     * 
     * @param event the event
     */
    public void processEvent(Object event) {
        // Add the event to the current invocation
        TideInvocation tideInvocation = TideInvocation.get();
        if (tideInvocation == null)
            return;
        
        if (userEvents != null) {
            String sessionId = getSessionId();
            if (sessionId != null && userEvents.hasEventType(event.getClass()))
                tideInvocation.addEvent(new ContextEvent(event.getClass().getName(), 
                		new Object[] { event, null }));
        }
//        else if (Contexts.getSessionContext().isSet("org.granite.seam.login")) {
//            // Force send of all events raised during login
//            tideInvocation.addEvent(new ContextEvent(type, params));
//        }
    }
    
    
    /**
     * Factory for Seam async publisher
     * 
     * @return servlet context of the current application
     */
    @Override
    protected AsyncPublisher getAsyncPublisher() {
    	return null;
    }
    
    
    @Inject
    private ConversationState conversation;
    
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
        
        try {
	        if (manager.getContext(RequestScoped.class).isActive() && manager.getContext(SessionScoped.class).isActive() && isFirstCall && isLogin) {
	            // Login tried : force evaluation of existing session context
	            for (Map.Entry<ContextResult, Boolean> me : getResultsEval().entrySet()) {
	                if (me.getKey().getExpression() == null 
	                		&& findBean(me.getKey().getComponentName(), me.getKey().getComponentClass()).getScope().equals(SessionScoped.class))
	                    me.setValue(Boolean.TRUE);
	            }
	            isLogin = false;
	            isFirstCall = false;
	        }
        }
        catch (ContextNotActiveException e) {
        	// isActive() is not enough !!
        }
        try {
	        if (manager.getContext(RequestScoped.class).isActive() && manager.getContext(ConversationScoped.class).isActive() 
	        	&& conversation.isFirstCall()) { 
	            // Join conversation : force evaluation of existing conversation context
	            for (Map.Entry<ContextResult, Boolean> me : getResultsEval().entrySet()) {
	                if (me.getKey().getExpression() == null 
	                		&& findBean(me.getKey().getComponentName(), me.getKey().getComponentClass()).getScope().equals(ConversationScoped.class))
	                    me.setValue(Boolean.TRUE);
	            }
	        	conversation.setFirstCall(false);
	        }
        }
        catch (ContextNotActiveException e) {
        	// isActive() is not enough !!
        }
        
        String sessionId = getSessionId();
        if (sessionId != null && listeners != null) {
            // Registers new event listeners
            for (String listener : listeners) {
            	try {
            		Class<?> listenerClass = ClassUtil.forName(listener);
        			tideUserEvents.registerEventType(sessionId, listenerClass);
            	}
            	catch (ClassNotFoundException e) {
            		log.error("Could not register event " + listener, e);
            	}
            }
            
            if (userEvents == null)
                userEvents = tideUserEvents.getUserEvents(getSessionId());
        }
        
        if (results != null) {
        	Map<ContextResult, Boolean> resultsEval = getResultsEval();
            for (Object result : results) {
                ContextResult cr = (ContextResult)result;
                resultsEval.put(cr, Boolean.TRUE);
            }
        }
        
        try {
        	tideEntityInitializer.restoreLoadedEntities();
        }
        catch (ContextNotActiveException e) {
        	// Not in a conversation
        }
        
        TideInvocation tideInvocation = TideInvocation.init();
        tideInvocation.update(updates);
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
        int scope = 3;
        boolean restrict = false;

		List<ContextUpdate> results = new ArrayList<ContextUpdate>(tideInvocation.getResults());
    	DataContext dataContext = DataContext.get();
		Set<Object[]> dataUpdates = dataContext != null ? dataContext.getDataUpdates() : null;
		Object[][] updates = null;
		if (dataUpdates != null && !dataUpdates.isEmpty())
			updates = dataUpdates.toArray(new Object[dataUpdates.size()][]);
		
        Bean<?> bean = null;
        if (componentName != null || componentClass != null) {
            // Determines scope of component
            bean = findBean(componentName, componentClass);
            if (bean.getScope() == RequestScoped.class)
            	scope = 3;
            else if (bean.getScope() == ConversationScoped.class)
            	scope = 2;
            else if (bean.getScope() == SessionScoped.class)
            	scope = 1;
            
            try {
	            if (manager.getContext(RequestScoped.class).get(bean) != null)
	                scope = 3;
	            else if (manager.getContext(ConversationScoped.class).get(bean) != null)
	                scope = 2;
	            else if (manager.getContext(SessionScoped.class).get(bean) != null)
	                scope = 1;
            }
            catch (ContextNotActiveException e) {
            	scope = 3;
            }
        }
        
        InvocationResult res = new InvocationResult(result, results);
        res.setScope(scope);
        res.setRestrict(restrict);
        if (bean != null) {
        	if (bean.getBeanClass().isAnnotationPresent(BypassTideMerge.class))
        		res.setMerge(false);
        	else {
	        	try {
	        		if (context != null) {
		        		Method m = bean.getBeanClass().getMethod(context.getMethod().getName(), context.getMethod().getParameterTypes());
		        		if (m.isAnnotationPresent(BypassTideMerge.class))
		        			res.setMerge(false);
	        		}
	        	}
	        	catch (Exception e) {
	        		log.warn("Could not find bean method", e);
	        	}
        	}
        }
        
        res.setUpdates(updates);
        
        // Adds events in result object
        res.setEvents(tideInvocation.getEvents());
        
        try {
	        // Save current set of entities loaded in a conversation scoped component to handle case of extended PM
	        tideEntityInitializer.saveLoadedEntities();
        }
        catch (ContextNotActiveException e) {
        	// Not in a conversation
        }

        // Clean thread
        TideInvocation.remove();
        
        return res;
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
        // Clean thread: very important to avoid phantom evaluations after exceptions
        TideInvocation.remove();
    }
    
    
    public void addResultEval(ContextResult result) {
        getResultsEval().put(result, Boolean.TRUE);
    }
    
    
    /**
     * Evaluate updates in current server context
     * 
     * @param updates list of updates
     * @param target the target instance
     */
    @SuppressWarnings("serial")
    public void restoreContext(List<ContextUpdate> updates, Object target) {
        if (updates == null)
            return;
        
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        
        // Restore context
        for (ContextUpdate update : updates) {
            
            log.debug("Before invocation: evaluating expression #0.#1", update.getComponentName(), update.getExpression());
            
            Set<Bean<?>> beans = manager.getBeans(update.getValue().getClass(), new AnnotationLiteral<Any>() {});
            if (beans.isEmpty() && update.getComponentName() != null)
            	beans = manager.getBeans(update.getComponentName());
            
            if (!beans.isEmpty()) {
            	Bean<?> sourceBean = beans.iterator().next();
            
                Object previous = manager.getReference(sourceBean, Object.class, manager.createCreationalContext(sourceBean));
                boolean disabled = previous != null && sourceBean != null 
            		&& config.isComponentTideDisabled(sourceBean.getName(), beanClasses(sourceBean), previous);
	            if (!disabled)
	            	mergeExternal(update.getValue(), previous);
            }
        }
    }
    
    
    /**
     * Evaluate results from context
     * 
     * @param target the target instance
     * @param nothing used by initializer to avoid interactions with context sync
     * 
     * @return list of updates to send back to the client
     */
    public List<ContextUpdate> evaluateResults(Object target, boolean nothing) {
        
        List<ContextUpdate> resultsMap = new ArrayList<ContextUpdate>();
        
        if (nothing)
            return resultsMap;
        
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        
        for (Map.Entry<ContextResult, Boolean> me : getResultsEval().entrySet()) {
            if (!me.getValue())
                continue;
            
            ContextResult res = me.getKey();
            
            Class<?> componentClass = res.getComponentClass();
            Bean<?> targetBean = findBean(res.getComponentName(), componentClass);
            String targetComponentName = targetBean.getName();
            
            boolean add = true;
            Class<? extends Annotation> scopeType = targetBean.getScope();
            Boolean restrict = res.getRestrict();
            
            Object value = res instanceof ScopedContextResult 
            	? ((ScopedContextResult)res).getValue() 
            	: manager.getReference(targetBean, Object.class, null);
            
            if (value != null && config.isComponentTideDisabled(targetComponentName, beanClasses(targetBean), value))
                add = false;
            
            if (add) {
            	getResultsEval().put(res, false);
            	
                if (value != null && classGetter != null) {
                    classGetter.initialize(null, null, value);
                    
                    int scope = 3;
                    if (scopeType == ConversationScoped.class)
                    	scope = 2;
                    else if (scopeType == SessionScoped.class)
                    	scope = 1;
                    
                    resultsMap.add(new ContextUpdate(res.getComponentName(), null, value, scope, Boolean.TRUE.equals(restrict)));
                    add = false;
                }
            }
            
            me.setValue(Boolean.FALSE);
        }
        
        return resultsMap;
    }

	@Inject
	private CDIInitializer initializer;
	
	@Override
	protected TidePersistenceManager getTidePersistenceManager(boolean create) {
		return initializer.getPersistenceManager();
	}
	
	
	@Override
    protected boolean equals(Object obj1, Object obj2) {
		if (super.equals(obj1, obj2))
			return true;
		
		return (obj1 != null && obj2 != null 
				&& (obj1.getClass().isAnnotationPresent(TideBean.class) || obj2.getClass().isAnnotationPresent(TideBean.class)));
    }
    
    
//    /**
//     * Implementations of intercepted asynchronous calls
//     * Send asynchronous event to client
//     * @param asyncContext current context (session id)
//     * @param targetComponentName target component name
//     * @param methodName method name
//     * @param paramTypes method argument types
//     * @param params argument values
//     * @return result
//     */
//    public Object invokeAsynchronous(AsyncContext asyncContext, String targetComponentName, String methodName, Class<?>[] paramTypes, Object[] params) {
//        setAsyncContext(asyncContext);
//        
//        // Just another ugly hack: the Seam interceptor has set this variable and we don't want it
//        Contexts.getEventContext().remove("org.jboss.seam.async.AsynchronousIntercepter.REENTRANT");
//        
//        Component component = TideInit.lookupComponent(targetComponentName);
//        
//        // Forces evaluation of all results if they are related to the called component
//        for (Map.Entry<ContextResult, Boolean> me : getResultsEval(component.getScope()).entrySet()) {
//            if (me.getKey().getComponentName().equals(targetComponentName))
//                me.setValue(Boolean.TRUE);
//        }
//        
//        Object target = Component.getInstance(targetComponentName);
//        
//        Method method;
//        try {
//            method = target.getClass().getMethod(methodName, paramTypes);
//        }
//        catch (NoSuchMethodException nsme) {
//           throw new IllegalStateException(nsme);
//        }
//        
//        Object result = Reflections.invokeAndWrap(method, target, params);
//        
//        sendEvent(targetComponentName);
//        
//        return result;
//    }
//    
//
//    /**
//     * Search for a named attribute in all contexts, in the
//     * following order: method, event, page, conversation,
//     * session, business process, application.
//     * 
//     * @return the first component found, or null
//     */
//    public static Object[] lookupInStatefulContexts(String name, ScopeType scope) {
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.METHOD.equals(scope)) && Contexts.isMethodContextActive()) {
//            Object result = Contexts.getMethodContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getMethodContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.EVENT.equals(scope)) && Contexts.isEventContextActive()) {
//            Object result = Contexts.getEventContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getEventContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.PAGE.equals(scope)) && Contexts.isPageContextActive()) {
//            Object result = Contexts.getPageContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getPageContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.CONVERSATION.equals(scope)) && Contexts.isConversationContextActive()) {
//            Object result = Contexts.getConversationContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getConversationContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.SESSION.equals(scope)) && Contexts.isSessionContextActive()) {
//            Object result = Contexts.getSessionContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getSessionContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.BUSINESS_PROCESS.equals(scope)) && Contexts.isBusinessProcessContextActive()) {
//            Object result = Contexts.getBusinessProcessContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getBusinessProcessContext().getType() };
//        }
//        
//        if ((ScopeType.UNSPECIFIED.equals(scope) || ScopeType.APPLICATION.equals(scope)) && Contexts.isApplicationContextActive()) {
//            Object result = Contexts.getApplicationContext().get(name);
//            if (result != null)
//                return new Object[] { result, Contexts.getApplicationContext().getType() };
//        }
//        
//        return ScopeType.UNSPECIFIED.equals(scope) ? null : new Object[] { null, scope };
//    }
}
