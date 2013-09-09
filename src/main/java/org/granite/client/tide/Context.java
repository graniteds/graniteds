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
    
    public Context(ContextManager contextManager, Context parentCtx, String contextId) {
        this.contextManager = contextManager;
        // TODO: conversation contexts 
        // parentCtx
        this.contextId = contextId;
    }
    
    public ContextManager getContextManager() {
    	return contextManager;
    }
    
    public EntityManager getEntityManager() {
    	return entityManager;
    }
    
    public void setDataManager(DataManager dataManager) {
    	this.dataManager = dataManager;
    }
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public Map<String, Object> getInitialBeans() {
        return Collections.unmodifiableMap(initialBeans);
    }
    
    
    public void initContext(Application platform, EventBus eventBus, InstanceStore instanceStore) {
    	this.application = platform;
    	this.eventBus = eventBus;
        this.instanceStore = instanceStore;
        platform.initContext(this, initialBeans);
        this.entityManager = new EntityManagerImpl("", dataManager, null, null);
        this.entityManager.setRemoteInitializer(new RemoteInitializerImpl(this));
    }
    
    public EventBus getEventBus() {
    	return eventBus;
    }
    
    public void postInit() {
        // TODO: postInit ?
    }
    
    public Context getParentContext() {
        return null;
    }
    
    public String getContextId() {
        return contextId;
    }
    
    public boolean isContextIdFromServer() {
        return isContextIdFromServer;
    }
    
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
        // TODO: conversation contexts
//        if (_remoteConversation != null)
//            _remoteConversation.id = contextId;
        this.isContextIdFromServer = fromServer;
        contextManager.updateContextId(previousContextId, this);
    }

    public <T> T byName(String name) {
        return instanceStore.byName(name, this);
    }
    
    public <T> T byNameNoProxy(String name) {
    	return instanceStore.getNoProxy(name);
    }
    
    public <T> T byType(Class<T> type) {
        return instanceStore.byType(type, this);
    }

    public <T> T[] allByType(Class<T> type) {
        return instanceStore.allByType(type, this, true);
    }
    public <T> T[] allByType(Class<T> type, boolean create) {
        return instanceStore.allByType(type, this, create);
    }
    
    public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return instanceStore.allByAnnotatedWith(annotationClass, this);
    }
    
    public List<String> allNames() {
    	return instanceStore.allNames();
    }
    
    public <T> T set(String name, T instance) {
    	return instanceStore.set(name, instance);
    }
    
    public <T> T set(T instance) {
    	return instanceStore.set(instance);
    }
    
    public void remove(String name) {
    	instanceStore.remove(name);
    }
    
    public void clear(boolean force) {
    	entityManager.clear();
        instanceStore.clear();
    }
    
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
    
    
    public void checkValid() {
    	if (finished)
            throw new InvalidContextException(contextId, "Invalid context");
    }
    
    
    public void callLater(Runnable runnable) {
    	application.execute(runnable);
    }
    
    
    public void markAsFinished() {
        this.finished = true;
    }
}
