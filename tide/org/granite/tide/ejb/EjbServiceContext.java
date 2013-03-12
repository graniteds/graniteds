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

package org.granite.tide.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.granite.logging.Logger;
import org.granite.messaging.service.EjbServiceMetadata;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideServiceContext;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.JPAPersistenceManager;
import org.granite.tide.invocation.ContextEvent;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.util.AbstractContext;


/**
 * @author William DRAI
 */
public class EjbServiceContext extends TideServiceContext  {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(EjbServiceContext.class);
    
    public static final String CAPITALIZED_DESTINATION_ID = "{capitalized.component.name}";
    public static final String DESTINATION_ID = "{component.name}";
    
    private transient ConcurrentHashMap<String, EjbComponent> ejbLookupCache = new ConcurrentHashMap<String, EjbComponent>();
    private final Set<String> remoteObservers = new HashSet<String>();
    
    private final InitialContext initialContext;
    private final String lookup;
    
    private final EjbIdentity identity;
    
    private String entityManagerFactoryJndiName = null;
    private String entityManagerJndiName = null;
    
    
    public EjbServiceContext() throws ServiceException {
        super();
    	lookup = "";
    	initialContext = null;
    	identity = new EjbIdentity();
    }
    
    public EjbServiceContext(String lookup, InitialContext ic) throws ServiceException {
        super();
        this.lookup = lookup;
        this.initialContext = ic;
    	identity = new EjbIdentity();
    }


    @Override
    protected AsyncPublisher getAsyncPublisher() {
        return null;
    }
    
    
    public void setEntityManagerFactoryJndiName(String entityManagerFactoryJndiName) {
        this.entityManagerFactoryJndiName = entityManagerFactoryJndiName;
    }

    public void setEntityManagerJndiName(String entityManagerJndiName) {
        this.entityManagerJndiName = entityManagerJndiName;
    }
    
    /**
     *  Create a TidePersistenceManager
     *  
     *  @param create create if not existent (can be false for use in entity merge)
     *  @return a TidePersistenceManager
     */
    @Override
	protected TidePersistenceManager getTidePersistenceManager(boolean create) {
        if (!create)
            return null;
        
        EntityManager em = getEntityManager();
        if (em == null)
            return null;
        
        return new JPAPersistenceManager(em);
    }
    
    
    /**
     * Find the entity manager using the jndi names stored in the bean. 
     * @return The found entity manager
     */
    private EntityManager getEntityManager() {
        try {
            InitialContext jndiContext = initialContext != null ? initialContext : new InitialContext();
            
            if (entityManagerFactoryJndiName != null) {
                EntityManagerFactory factory = (EntityManagerFactory) jndiContext.lookup(entityManagerFactoryJndiName);
                return factory.createEntityManager();
            } 
            else if (entityManagerJndiName != null) {
                return (EntityManager) jndiContext.lookup(entityManagerJndiName);
            }
        } 
        catch (NamingException e) {
            if (entityManagerFactoryJndiName != null) 
                throw new RuntimeException("Unable to find a EntityManagerFactory  for jndiName " + entityManagerFactoryJndiName);
            else if (entityManagerJndiName != null) 
                throw new RuntimeException("Unable to find a EntityManager for jndiName " + entityManagerJndiName);
        }
        
        return null;
    }

    
    public Object callComponent(Method method, Object... args) throws Exception {
		String name = method.getDeclaringClass().getSimpleName();
		name = name.substring(0, 1).toLowerCase() + name.substring(1);
		if (name.endsWith("Bean"))
			name = name.substring(0, name.length() - "Bean".length());
		Object invokee = findComponent(name, null);
		method = invokee.getClass().getMethod(method.getName(), method.getParameterTypes());
		return method.invoke(invokee, args);
    }
    
    public Set<String> getRemoteObservers() {
    	return remoteObservers;
    }
    
    /* (non-Javadoc)
	 * @see org.granite.tide.ejb.EJBServiceContextIntf#findComponent(java.lang.String)
	 */
    @Override
    public Object findComponent(String componentName, Class<?> componentClass) {
    	if ("identity".equals(componentName))
    		return identity;
    	
        EjbComponent component = ejbLookupCache.get(componentName);
        if (component != null)
            return component.ejbInstance;
        
        // Compute EJB JNDI binding.
        String name = componentName;
        if (lookup != null) {
            name = lookup;
            if (lookup.contains(CAPITALIZED_DESTINATION_ID))
                name = lookup.replace(CAPITALIZED_DESTINATION_ID, capitalize(componentName));
            if (lookup.contains(DESTINATION_ID))
                name = lookup.replace(DESTINATION_ID, componentName);
        }
        
        InitialContext ic = this.initialContext;
        if (ic == null) {
	        try {
	            ic = new InitialContext();
	        } 
	        catch (Exception e) {
	            throw new ServiceException("Could not get InitialContext", e);
	        }
        }
        
        log.debug(">> New EjbServiceInvoker looking up: %s", name);

        try {
            component = new EjbComponent();
            component.ejbInstance = ic.lookup(name);
            component.ejbClasses = new HashSet<Class<?>>();
            Class<?> scannedClass = null;
            EjbScannedItemHandler itemHandler = EjbScannedItemHandler.instance();
            for (Class<?> i : component.ejbInstance.getClass().getInterfaces()) {
            	if (itemHandler.getScannedClasses().containsKey(i)) {
            		scannedClass = itemHandler.getScannedClasses().get(i);
            		break;
            	}
            }
            if (scannedClass == null)
            	scannedClass = itemHandler.getScannedClasses().get(component.ejbInstance.getClass());
            // GDS-768: handling of proxied no-interface EJBs in GlassFish v3
            if (scannedClass == null && component.ejbInstance.getClass().getSuperclass() != null)
            	scannedClass = itemHandler.getScannedClasses().get(component.ejbInstance.getClass().getSuperclass());
            
            if (scannedClass != null) {
                component.ejbClasses.add(scannedClass);
                for (Map.Entry<Class<?>, Class<?>> me : itemHandler.getScannedClasses().entrySet()) {
                	if (me.getValue().equals(scannedClass))
                		component.ejbClasses.add(me.getKey());
                }
                component.ejbMetadata = new EjbServiceMetadata(scannedClass, component.ejbInstance.getClass());
            }
            else
            	log.warn("Ejb " + componentName + " was not scanned: remove method will not be called if it is a Stateful bean. Add META-INF/services-config.properties if needed.");
            
            EjbComponent tmpComponent = ejbLookupCache.putIfAbsent(componentName, component); 
            if (tmpComponent != null) 
            	component = tmpComponent; 
            return component.ejbInstance;
        }
        catch (NamingException e) {
        	log.error("EJB not found " + name + ": " + e.getMessage());
            throw new ServiceException("Could not lookup for: " + name, e);
        }
    }
    
    /* (non-Javadoc)
	 * @see org.granite.tide.ejb.EJBServiceContextIntf#findComponentClass(java.lang.String)
	 */
    @Override
    public Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass) {
    	if ("identity".equals(componentName)) {
    		Set<Class<?>> classes = new HashSet<Class<?>>(1);
    		classes.add(EjbIdentity.class);
    		return classes;
    	}
    	
        EjbComponent component = ejbLookupCache.get(componentName);
        if (component == null)
            findComponent(componentName, componentClass);
        return ejbLookupCache.get(componentName).ejbClasses;
    }

    
    private String capitalize(String s) {
        if (s == null || s.length() == 0)
            return s;
        if (s.length() == 1)
            return s.toUpperCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    /* (non-Javadoc)
	 * @see org.granite.tide.ejb.EJBServiceContextIntf#prepareCall(org.granite.messaging.service.ServiceInvocationContext, org.granite.tide.IInvocationCall, java.lang.String)
	 */
    @Override
    public void prepareCall(ServiceInvocationContext context, IInvocationCall c, String componentName, Class<?> componentClass) {
    	if ((c instanceof InvocationCall) && ((InvocationCall)c).getListeners() != null)
    		remoteObservers.addAll(((InvocationCall)c).getListeners());
    	Context.create(this);
    	        
        // Initialize an empty data context
        DataContext.init();
    }

    
    private static class EjbComponent {
        public Object ejbInstance;
        public Set<Class<?>> ejbClasses;
        public EjbServiceMetadata ejbMetadata;
    }
    
    /* (non-Javadoc)
	 * @see org.granite.tide.ejb.EJBServiceContextIntf#postCall(org.granite.messaging.service.ServiceInvocationContext, java.lang.Object, java.lang.String)
	 */
    @Override
    public IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass) {
    	try {    		
    		AbstractContext threadContext = AbstractContext.instance();
    		
    		List<ContextUpdate> results = new ArrayList<ContextUpdate>(threadContext.size());
        	DataContext dataContext = DataContext.get();
    		Object[][] updates = dataContext != null ? dataContext.getUpdates() : null;
    		
    		for (Map.Entry<String, Object> entry : threadContext.entrySet())
    			results.add(new ContextUpdate(entry.getKey(), null, entry.getValue(), 3, false));
    		
	        InvocationResult ires = new InvocationResult(result, results);
			Set<Class<?>> componentClasses = findComponentClasses(componentName, componentClass);
	    	if (isBeanAnnotationPresent(componentClasses, context.getMethod().getName(), context.getMethod().getParameterTypes(), BypassTideMerge.class))
				ires.setMerge(false);
	        
	        ires.setUpdates(updates);
	        ires.setEvents(new ArrayList<ContextEvent>(threadContext.getRemoteEvents()));
	        
	        if (componentName != null) {
	            EjbComponent component = ejbLookupCache.get(componentName);
	            if (component != null && component.ejbMetadata != null 
	            		&& component.ejbMetadata.isStateful() && component.ejbMetadata.isRemoveMethod(context.getMethod()))
	                ejbLookupCache.remove(componentName);
	        }
	        
	        return ires;
    	}
    	finally {
    		AbstractContext.remove();
    	}
    }

    /* (non-Javadoc)
	 * @see org.granite.tide.ejb.EJBServiceContextIntf#postCallFault(org.granite.messaging.service.ServiceInvocationContext, java.lang.Throwable, java.lang.String)
	 */
    @Override
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {
    	try {
	        if (componentName != null) {
	            EjbComponent component = ejbLookupCache.get(componentName);
	            if (t instanceof NoSuchEJBException || (component != null && component.ejbMetadata != null &&
	                    (component.ejbMetadata.isStateful() &&
	                    component.ejbMetadata.isRemoveMethod(context.getMethod()) &&
	                    !component.ejbMetadata.getRetainIfException(context.getMethod()))
	                )) {
	                ejbLookupCache.remove(componentName);
	            }
	        }
    	}
    	finally {
    		AbstractContext.remove();
    	}
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject(); 
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	ejbLookupCache = new ConcurrentHashMap<String, EjbComponent>();
    }
}
