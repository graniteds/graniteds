/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideServiceContext;
import org.granite.tide.TideTransactionManager;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.async.AsyncPublisher;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.TypeUtil;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 *  @author Sebastien Deleuze
 * 	@author William DRAI
 */
public class SpringServiceContext extends TideServiceContext implements ApplicationContextAware {

    private static final long serialVersionUID = 1L;
    
    protected transient ApplicationContext springContext = null;
    
    private String persistenceManagerBeanName = null;
    private String entityManagerFactoryBeanName = null;
    
    private static final Logger log = Logger.getLogger(SpringServiceContext.class);
                
    public SpringServiceContext() throws ServiceException {
        super();
        
        log.debug("Getting spring context from container");
        getSpringContext();
    }
    
    public SpringServiceContext(ApplicationContext springContext) throws ServiceException {
        super();
        
        this.springContext = springContext;
    }
    
    public void setApplicationContext(ApplicationContext springContext) {
    	this.springContext = springContext;
    }

    protected ApplicationContext getSpringContext() {
    	if (springContext == null) {
            GraniteContext context = GraniteContext.getCurrentInstance();
            ServletContext sc = ((HttpGraniteContext)context).getServletContext();
            springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
    	}
    	return springContext;    	
    }
    
    
    @Override
    protected AsyncPublisher getAsyncPublisher() {
        return null;
    }    
    
    @Override
    public Object findComponent(String componentName, Class<?> componentClass, String componentPath) {
    	Object bean = null;
    	String key = COMPONENT_ATTR + (componentName != null ? componentName : "_CLASS_" + componentClass.getName());
    	
    	GraniteContext context = GraniteContext.getCurrentInstance();
    	if (context != null) {
    		bean = context.getRequestMap().get(key);
    		if (bean != null)
    			return bean;
    	}
    	
    	try {
			bean = internalFindComponent(componentName, componentClass, componentPath);
			
	        if (context != null)
	        	context.getRequestMap().put(key, bean);
	        return bean;
        }
    	catch (NoSuchBeanDefinitionException nexc) {
            String msg = "Spring service named '" + componentName + "' does not exist.";
            ServiceException e = new ServiceException(msg, nexc);
            throw e;
        } 
    	catch (BeansException bexc) {
            String msg = "Unable to create Spring service named '" + componentName + "'";
            ServiceException e = new ServiceException(msg, bexc);
            throw e;
        }    
    }

	protected Object internalFindComponent(String componentName, Class<?> componentClass, String componentPath) {
		Object bean = null;
		
		if (componentClass != null) {
			Map<String, ?> beans = springContext.getBeansOfType(componentClass);
			if (beans.size() == 1)
				bean = beans.values().iterator().next();
			else if (beans.size() > 1 && componentName != null && !("".equals(componentName))) {
				if (beans.containsKey(componentName))
					bean = beans.get(componentName);
			}
			else if (beans.isEmpty() && springContext.getClass().getName().indexOf("Grails") > 0 && componentClass.getName().endsWith("Service")) {
				try {
					Object serviceClass = springContext.getBean(componentClass.getName() + "ServiceClass");	    			
					Method m = serviceClass.getClass().getMethod("getPropertyName");
					String compName = (String)m.invoke(serviceClass);
					bean = springContext.getBean(compName);
				}
				catch (NoSuchMethodException e) {
					log.error(e, "Could not get service class for %s", componentClass.getName());
				}
				catch (InvocationTargetException e) {
					log.error(e.getCause(), "Could not get service class for %s", componentClass.getName());
				}
				catch (IllegalAccessException e) {
					log.error(e.getCause(), "Could not get service class for %s", componentClass.getName());
				}
			}
		}
		if (bean == null && componentName != null && !("".equals(componentName)))
			bean = springContext.getBean(componentName);
		
		return bean;
	}
    
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> findComponentClasses(String componentName, Class<?> componentClass, String methodName) {
    	String key = COMPONENT_CLASS_ATTR + componentName;
    	Set<Class<?>> classes = null; 
    	GraniteContext context = GraniteContext.getCurrentInstance();
    	if (context != null) {
    		classes = (Set<Class<?>>)context.getRequestMap().get(key);
    		if (classes != null)
    			return classes;
    	}
    	
        Object bean = findComponent(componentName, componentClass, methodName);
        classes = buildComponentClasses(bean);        
        if (classes == null)
        	return null;
        
        if (context != null)
        	context.getRequestMap().put(key, classes);
        return classes;
    }
    
    protected Set<Class<?>> buildComponentClasses(Object bean) {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Class<?> i : bean.getClass().getInterfaces())
        	classes.add(i);
        
        try {
	        while (bean instanceof Advised)
	        	bean = ((Advised)bean).getTargetSource().getTarget();
	        
	        classes.add(AopUtils.getTargetClass(bean));
        }
        catch (Exception e) {
            log.warn(e, "Could not get AOP class for component " + bean.getClass());
        	return null;
        }
        
        return classes;
    }
    
    protected boolean isBeanAnnotationPresent(Object bean, Class<? extends Annotation> annotationClass) {
    	if (bean.getClass().isAnnotationPresent(annotationClass))
    		return true;
        
        try {
	        while (bean instanceof Advised)
	        	bean = ((Advised)bean).getTargetSource().getTarget();
	        
	        if (AopUtils.getTargetClass(bean).isAnnotationPresent(annotationClass))
	        	return true;
        }
        catch (Exception e) {
            log.warn(e, "Could not get AOP class for component " + bean.getClass());
        }
        
        return false;
    }
    
    protected boolean isBeanMethodAnnotationPresent(Object bean, String methodName, Class<?>[] methodArgTypes, Class<? extends Annotation> annotationClass) {
    	try {
    		Method m = bean.getClass().getMethod(methodName, methodArgTypes);
    		if (m.isAnnotationPresent(annotationClass))
    			return true;
    		
	        while (bean instanceof Advised)
	        	bean = ((Advised)bean).getTargetSource().getTarget();
	        
    		m = AopUtils.getTargetClass(bean).getMethod(methodName, methodArgTypes);
	        if (m.isAnnotationPresent(annotationClass))
	        	return true;
		}
    	catch (Exception e) {
    		log.warn("Could not find bean method", e);
    	}
    	
    	return false;
    }

    
    @Override
    public void prepareCall(ServiceInvocationContext context, IInvocationCall c, String componentName, Class<?> componentClass) {
    	DataContext.init();
    	
    	DataUpdatePostprocessor dupp = (DataUpdatePostprocessor)findComponent(null, DataUpdatePostprocessor.class, null);
    	if (dupp != null)
    		DataContext.get().setDataUpdatePostprocessor(dupp);
    }

    @Override
    public IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass) {
		List<ContextUpdate> results = null;
    	DataContext dataContext = DataContext.get();
		Object[][] updates = dataContext != null ? dataContext.getUpdates() : null;
		
        InvocationResult ires = new InvocationResult(result, results);
    	if (isBeanAnnotationPresent(context.getBean(), BypassTideMerge.class))
    		ires.setMerge(false);
    	else if (isBeanMethodAnnotationPresent(context.getBean(), context.getMethod().getName(), context.getMethod().getParameterTypes(), BypassTideMerge.class))
			ires.setMerge(false);
    	
        ires.setUpdates(updates);
        
        return ires;
    }

    @Override
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {        
    }
    
    
    public void setEntityManagerFactoryBeanName(String beanName) {
        this.entityManagerFactoryBeanName = beanName;
    }
    
    public void setPersistenceManagerBeanName(String beanName) {
        this.persistenceManagerBeanName = beanName;
    }
    
    /**
     *  Create a TidePersistenceManager
     *  
     *  @param create create if not existent (can be false for use in entity merge)
     *  @return a PersistenceContextManager
     */
    @Override
    protected TidePersistenceManager getTidePersistenceManager(boolean create) {
        if (!create)
            return null;
        
        TidePersistenceManager pm = (TidePersistenceManager)GraniteContext.getCurrentInstance().getRequestMap().get(TidePersistenceManager.class.getName());
        if (pm != null)
        	return pm;
        
        pm = createPersistenceManager();
        GraniteContext.getCurrentInstance().getRequestMap().put(TidePersistenceManager.class.getName(), pm);
        return pm;
    }
    
    private TidePersistenceManager createPersistenceManager() {
        if (persistenceManagerBeanName == null) {
        	if (entityManagerFactoryBeanName == null) {
        		// No bean or entity manager factory specified 
        		
        		// 1. Look for a TidePersistenceManager bean
        		Map<String, ?> pms = springContext.getBeansOfType(TidePersistenceManager.class);
        		if (pms.size() > 1)
        			throw new RuntimeException("More than one Tide persistence managers defined");
        		
        		if (pms.size() == 1)
        			return (TidePersistenceManager)pms.values().iterator().next();
        		
        		// 2. If not found, try to determine the Spring transaction manager        		
        		Map<String, ?> tms = springContext.getBeansOfType(PlatformTransactionManager.class);
        		if (tms.isEmpty())
        			log.debug("No Spring transaction manager found, specify a persistence-manager-bean-name or entity-manager-factory-bean-name");
        		else if (tms.size() > 1)
        			log.debug("More than one Spring transaction manager found, specify a persistence-manager-bean-name or entity-manager-factory-bean-name");
        		else if (tms.size() == 1) {
        			PlatformTransactionManager ptm = (PlatformTransactionManager)tms.values().iterator().next();
					
        			// If no entity manager, we define a Spring persistence manager 
					// that will try to infer persistence info from the Spring transaction manager
					return new SpringPersistenceManager(ptm);
        		}
        	}
        	
            String emfBeanName = entityManagerFactoryBeanName != null ? entityManagerFactoryBeanName : "entityManagerFactory";
            try {
            	// Lookup the specified entity manager factory
                Object emf = findComponent(emfBeanName, null, null);
                
                // Try to determine the Spring transaction manager
                TideTransactionManager tm = null;
        		Map<String, ?> ptms = springContext.getBeansOfType(PlatformTransactionManager.class);
        		if (ptms.size() == 1) {
        			log.debug("Found Spring transaction manager " + ptms.keySet().iterator().next());
        			tm = new SpringTransactionManager((PlatformTransactionManager)ptms.values().iterator().next());
        		}
                
				Class<?> emfClass = TypeUtil.forName("javax.persistence.EntityManagerFactory");
	            Class<?> pcmClass = TypeUtil.forName("org.granite.tide.data.JPAPersistenceManager");
	            Constructor<?>[] cs = pcmClass.getConstructors();
	            if (tm != null) {
	            	for (Constructor<?> c : cs) {
		            	if (c.getParameterTypes().length == 2 && emfClass.isAssignableFrom(c.getParameterTypes()[0])
		            		&& TideTransactionManager.class.isAssignableFrom(c.getParameterTypes()[1])) {
		            		log.debug("Created JPA persistence manager with Spring transaction manager");
		            		return (TidePersistenceManager)c.newInstance(((EntityManagerFactoryInfo)emf).getNativeEntityManagerFactory(), tm);
		            	}
	            	}
	            }
	            else {
		            for (Constructor<?> c : cs) {
		            	if (c.getParameterTypes().length == 1 && emfClass.isAssignableFrom(c.getParameterTypes()[0])) {
		            		log.debug("Created default JPA persistence manager");
		            		return (TidePersistenceManager)c.newInstance(emf);
		            	}
		            }
	            }
	            
	            throw new RuntimeException("Default Tide persistence manager not found");
            }
            catch (ServiceException e) {
            	if (entityManagerFactoryBeanName != null)
            		log.debug("EntityManagerFactory named %s not found, JPA support disabled", emfBeanName);
            	
            	return null;
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create default Tide persistence manager", e);
            }
        }
        
        return (TidePersistenceManager)findComponent(persistenceManagerBeanName, null, null);
    }
}
