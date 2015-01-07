/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.tide.simple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideServiceContext;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.util.AbstractContext;
import org.granite.util.TypeUtil;


/**
 * @author William DRAI
 */
public class SimpleServiceContext extends TideServiceContext {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = Logger.getLogger(SimpleServiceContext.class);

    private Map<String, SimpleComponent> cache = new ConcurrentHashMap<String, SimpleComponent>();

    private final SimpleIdentity identity;

    
    public SimpleServiceContext() throws ServiceException {
        super();
    	this.identity = new SimpleIdentity();
    }
    
    
    @Override
    protected AsyncPublisher getAsyncPublisher() {
        return null;
    }

    @Override
    protected TidePersistenceManager getTidePersistenceManager(boolean create) {
        return null;
    }

    @Override
    public Object findComponent(String componentName, Class<?> componentClass, String methodName) {
    	if ("identity".equals(componentName))
    		return identity;
    	
        SimpleComponent component = null;
        if (componentName != null)
            component = cache.get(componentName);
        else {
            for (Map.Entry<String, SimpleComponent> entry : cache.entrySet()) {
                if (entry.getValue().classes.contains(componentClass)) {
                    component = entry.getValue();
                    break;
                }
            }
        }
        if (component != null)
            return component.instance;

        log.debug(">> New SimpleServiceContext looking up: %s", componentName);

        try {
            SimpleScannedItemHandler itemHandler = SimpleScannedItemHandler.instance();
            Class<?> clazz = null;
            if (componentClass != null)
                clazz = itemHandler.getScannedClasses().get(componentClass);
            else
                clazz = itemHandler.getScannedClassesById().get(componentName);

            if (clazz == null)
                return null;

            component = new SimpleComponent();
            component.instance = TypeUtil.newInstance(clazz, Object.class);
            component.classes = new HashSet<Class<?>>();
            component.classes.add(clazz);
            cache.put(componentName, component);

            return component.instance;
        }
        catch (Exception e) {
        	log.error(e, "Component not found %s", componentName);
            throw new ServiceException("Could not lookup for: " + componentName, e);
        }
    }
    
    @Override
    public Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass, String methodName) {
    	if ("identity".equals(componentName)) {
    		Set<Class<?>> classes = new HashSet<Class<?>>(1);
    		classes.add(SimpleIdentity.class);
    		return classes;
    	}
    	
        SimpleComponent component = cache.get(componentName);
        if (component == null)
            findComponent(componentName, componentClass, methodName);
        return cache.get(componentName).classes;
    }

    @Override
    public void prepareCall(ServiceInvocationContext context, IInvocationCall c, String componentName, Class<?> componentClass) {
        // Initialize an empty data context
        DataContext.init();
    }

    
    private static class SimpleComponent {
        public Object instance;
        public Set<Class<?>> classes;
    }
    
    @Override
    public IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass) {
        List<ContextUpdate> results = new ArrayList<ContextUpdate>();
        DataContext dataContext = DataContext.get();
        Object[][] updates = dataContext != null ? dataContext.getUpdates() : null;

        InvocationResult ires = new InvocationResult(result, results);
        if (componentName != null || componentClass != null) {
            Set<Class<?>> componentClasses = findComponentClasses(componentName, componentClass, null);
            if (isBeanAnnotationPresent(componentClasses, context.getMethod().getName(), context.getMethod().getParameterTypes(), BypassTideMerge.class))
                ires.setMerge(false);
        }

        ires.setUpdates(updates);
        return ires;
    }

    @Override
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {
        AbstractContext.remove();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject(); 
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	cache = new ConcurrentHashMap<String, SimpleComponent>();
    }
}
