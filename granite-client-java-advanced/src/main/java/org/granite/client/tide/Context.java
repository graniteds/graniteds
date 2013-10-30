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
package org.granite.client.tide;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.impl.EntityManagerImpl;
import org.granite.client.tide.data.impl.JavaBeanDataManager;
import org.granite.client.tide.data.impl.RemoteInitializerImpl;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.DefaultApplication;
import org.granite.client.tide.impl.SimpleEventBus;
import org.granite.client.tide.impl.SimpleInstanceStore;
import org.granite.logging.Logger;

/**
 * General Tide context implementation
 * It can either wrap a Spring or CDI container or be used separately
 *
 * Currently only one context can be active at a time
 *
 * A context is created by a context manager
 *
 * @see org.granite.client.tide.ContextManager
 *
 * @author William DRAI
 */
public class Context {
    
    static final Logger log = Logger.getLogger(Context.class);
       
    private String contextId = null;
    private boolean isContextIdFromServer = false;
    private boolean finished = false;
    
    private ContextManager contextManager = null;
    
    private InstanceStore instanceStore = new SimpleInstanceStore(this);
    private Map<String, Object> initialBeans = new HashMap<String, Object>();
    
    private Application application = new DefaultApplication();
	private EventBus eventBus = new SimpleEventBus();
    
	private DataManager dataManager = new JavaBeanDataManager();
    private EntityManager entityManager;
    
    
    protected Context() {
    	// CDI proxying...
    }

    /**
     * Create a context using the specified manager and context id
     * Should not be used directly
     * @param contextManager context manager
     * @param parentCtx parent context for conversation contexts (not supported yet)
     * @param contextId context id
     */
    public Context(ContextManager contextManager, Context parentCtx, String contextId) {
        this.contextManager = contextManager;
        // TODO: conversation contexts 
        // parentCtx
        this.contextId = contextId;
    }

    /**
     * Managed for this context
     * @return context manager
     */
    public ContextManager getContextManager() {
    	return contextManager;
    }

    /**
     * Entity manager for this context
     * @return entity manager
     */
    public EntityManager getEntityManager() {
    	return entityManager;
    }

    /**
     * Set the data manager for this context
     * @param dataManager data manager
     * @see org.granite.client.tide.data.spi.DataManager
     */
    public void setDataManager(DataManager dataManager) {
    	this.dataManager = dataManager;
    }

    /**
     * Data manager for this context
     * @return data manager
     */
    public DataManager getDataManager() {
        return dataManager;
    }

    /**
     * Map of beans defined before the initialization of the context so they can be registered in the DI container
     * @return map of initialization beans keyed by name
     */
    public Map<String, Object> getInitialBeans() {
        return Collections.unmodifiableMap(initialBeans);
    }

    /**
     * Initialize the context
     * @param application application for this context (depends on the target platform/framework)
     * @param eventBus event bus for this context (depends on the framework and/or the DI container)
     * @param instanceStore instance store (depends on the DI container)
     */
    public void initContext(Application application, EventBus eventBus, InstanceStore instanceStore) {
    	this.application = application;
    	this.eventBus = eventBus;
        this.instanceStore = instanceStore;
        application.initContext(this, initialBeans);
        this.entityManager = new EntityManagerImpl("", dataManager);
        this.entityManager.setRemoteInitializer(new RemoteInitializerImpl(this));
    }

    /**
     * Event bus for this context
     * @return event bus
     */
    public EventBus getEventBus() {
    	return eventBus;
    }
    
    public void postInit() {
        // TODO: postInit ?
    }

    /**
     * Parent context for conversation contexts
     * @return parent context
     */
    public Context getParentContext() {
        return null;
    }

    /**
     * Context id
     * @return context id
     */
    public String getContextId() {
        return contextId;
    }

    /**
     * Indicate that the context id has been defined by the server
     * Unused for now
     * @return true if id received from server
     */
    public boolean isContextIdFromServer() {
        return isContextIdFromServer;
    }

    /**
     * Indicate that the context is eligible for destruction
     * @return true is finished
     */
    public boolean isFinished() {
        return finished;
    }
    
    /**
     *  Update the context id
     *  @param contextId context id
     *  @param fromServer is this id received from the server ?
     */
    public void setContextId(String contextId, boolean fromServer) {
        String previousContextId = this.contextId;
        this.contextId = contextId;
        this.isContextIdFromServer = fromServer;
        contextManager.updateContextId(previousContextId, this);
    }

    /**
     * Return a component instance by its name in the container
     * InstanceStore implementations are free to (but don't have to) automatically create a suitable component instance
     * with the expected name when no instance exists
     * @param name component name
     * @param <T> component type
     * @return component instance
     */
    public <T> T byName(String name) {
        return instanceStore.byName(name, this);
    }

    /**
     * Return a component instance by its name in the container
     * Does not create a default proxy ({@link org.granite.client.tide.impl.ComponentImpl}) if no instance exists
     * @param name component name
     * @param <T> component type
     * @return component instance or null if not found
     */
    public <T> T byNameNoProxy(String name) {
    	return instanceStore.getNoProxy(name, this);
    }

    /**
     * Return a component instance looked up by its type
     * If more than one instance is found, throws a runtime exception
     * @param type expected component type
     * @param <T> expected component type
     * @return component instance
     */
    public <T> T byType(Class<T> type) {
        return instanceStore.byType(type, this);
    }

    /**
     * Return an array of all component instances implementing the expected type
     * @param type expected component type
     * @param <T> expected component type
     * @return array of component instances
     */
    public <T> T[] allByType(Class<T> type) {
        return instanceStore.allByType(type, this, true);
    }
    /**
     * Return an array of all component instances implementing the expected type
     * @param type expected component type
     * @param create if true, should create an instance if none is existing
     * @param <T> expected component type
     * @return array of component instances or null if no instance found
     */
    public <T> T[] allByType(Class<T> type, boolean create) {
        return instanceStore.allByType(type, this, create);
    }

    /**
     * Return a map of all component instances annotated with the specified annotation
     * @param annotationClass annotation
     * @return map of component instances keyed by name
     */
    public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return instanceStore.allByAnnotatedWith(annotationClass, this);
    }

    /**
     * Return a list of all component names in this context
     * @return list of names
     */
    public List<String> allNames() {
    	return instanceStore.allNames();
    }

    /**
     * Set a component instance as a managed instance with the specified name in the context
     * May not work with all containers (Spring and CDI are static and cannot be modified after initialization)
     * @param name component name
     * @param instance component instance
     * @param <T> component type
     * @return component instance
     */
    public <T> T set(String name, T instance) {
    	return instanceStore.set(name, instance);
    }

    /**
     * Set a component instance as a managed instance in the context
     * May not work with all containers (Spring and CDI are static and cannot be modified after initialization)
     * @param instance component instance
     * @param <T> component type
     * @return component instance
     */
    public <T> T set(T instance) {
    	return instanceStore.set(instance);
    }

    /**
     * Remove the component instance having the specified name from the context
     * May not work with all containers (Spring and CDI are static and cannot be modified after initialization)
     * @param name component name
     */
    public void remove(String name) {
    	instanceStore.remove(name);
    }

    /**
     * Clear all data and instances in the context
     */
    public void clear() {
    	entityManager.clear();
        instanceStore.clear();
    }

    /**
     * Initialize an instance when it is added to the context
     * @param instance component instance
     * @param name component name
     */
    public void initInstance(Object instance, String name) {
    	if (name != null && instance instanceof NameAware)
    		((NameAware)instance).setName(name);
    	if (instance instanceof ContextAware)
    		((ContextAware)instance).setContext(this);
    	if (instance instanceof Initializable)
    		((Initializable)instance).init();
    	if (instance.getClass().isAnnotationPresent(ApplicationConfigurable.class))
    		application.configure(instance);
    }

    /**
     * Check that this context is not finished
     * @throws org.granite.client.tide.InvalidContextException when context finished
     */
    public void checkValid() {
    	if (finished)
            throw new InvalidContextException(contextId, "Invalid context");
    }

    /**
     * Convenience method to defer execution of a method in the main UI thread
     * @param runnable runnable method
     */
    public void callLater(Runnable runnable) {
    	application.execute(runnable);
    }

    /**
     * Mark this context as eligible for destruction
     */
    public void markAsFinished() {
        this.finished = true;
    }
}
