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
package org.granite.tide;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.granite.config.ConvertersConfig;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataMergeContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.validators.EntityValidator;
import org.granite.tide.validators.InvalidValue;
import org.granite.util.TypeUtil;

import flex.messaging.messages.RemotingMessage;

 
/**
 * Base class for Tide service invokers
 * Adapts the Tide invocation model with Granite
 * 
 * @author William DRAI
 */
public class TideServiceInvoker<T extends ServiceFactory> extends ServiceInvoker<T> {
     
    private static final Logger log = Logger.getLogger(TideServiceInvoker.class);
    
    public static final String VALIDATOR_KEY = "org.granite.tide.validator.key";
    public static final String VALIDATOR_NOT_AVAILABLE = "org.granite.tide.validator.notAvailable";
    
    public static final String VALIDATOR_NAME = "validator-name";
    public static final String VALIDATOR_CLASS_NAME = "validator-class-name";
    
    /**
     * Current tide context
     */
    private TideServiceContext tideContext = null;
    
    private transient EntityValidator validator = null;
    
    
    public TideServiceInvoker(Destination destination, T factory) throws ServiceException {
        super(destination, factory);
        this.invokee = this;
        this.tideContext = lookupContext();
        this.tideContext.initCall();
        initValidator();
    }
    
    public TideServiceInvoker(Destination destination, T factory, TideServiceContext tideContext) throws ServiceException {
        super(destination, factory);
        this.invokee = this;
        this.tideContext = tideContext;
        this.tideContext.initCall();
        initValidator();
    }
    
    
    public Object initializeObject(Object parent, String[] propertyNames) {
        return tideContext.lazyInitialize(parent, propertyNames);
    }
    
    
    private static final InvalidValue[] EMPTY_INVALID_VALUES = new InvalidValue[0];
    
    protected void initValidator() {
    	Map<String, Object> applicationMap = GraniteContext.getCurrentInstance().getApplicationMap();
    	Boolean validatorNotAvailable = (Boolean)applicationMap.get(VALIDATOR_NOT_AVAILABLE);
    	validator = (EntityValidator)applicationMap.get(VALIDATOR_KEY);
    	
    	if (validator != null || Boolean.TRUE.equals(validatorNotAvailable))
    		return;
    	
        String className = this.destination.getProperties().get(VALIDATOR_CLASS_NAME);
        if (className != null) {
        	initValidatorWithClassName(className, null);
        	if (validator == null) {
        		log.warn("Validator class " + className + " not found: validation not enabled");
	            applicationMap.put(VALIDATOR_NOT_AVAILABLE, Boolean.TRUE);
        	}
        	else {
        		log.info("Validator class " + className + " initialized");
	        	applicationMap.put(VALIDATOR_KEY, validator);
        	}
        }
        else {
	    	String name = this.destination.getProperties().get(VALIDATOR_NAME);
	    	if (name != null) {
	    		try {
	    			validator = (EntityValidator)tideContext.findComponent(name, EntityValidator.class, null);
	    		}
	    		catch (ServiceException e) {
	    			name = null;
	    		}
	    	}
	    	
	    	if (validator == null) {
	    		className = "org.granite.tide.validation.BeanValidation";	    		
	    		initValidatorWithClassName(className, "javax.validation.ValidatorFactory");
	    	}
	    	
	    	if (validator == null) {
	    		if (name != null)
	    			log.warn("Validator component " + name + " not found: validation not enabled");
	    		else
	        		log.warn("Validator class " + className + " not found: validation not enabled");
	    		
    			applicationMap.put(VALIDATOR_NOT_AVAILABLE, Boolean.TRUE);
	    	}
        	else {
    			log.info("Validator class " + validator.getClass().getName() + " initialized");
	        	applicationMap.put(VALIDATOR_KEY, validator);
        	}
    	}
    }
    
    private void initValidatorWithClassName(String className, String constructorArgClassName) {
        try {
    		Object constructorArg = null;
    		Class<?> constructorArgClass = null;
    		if (constructorArgClassName != null) {
	    		try {
	    			constructorArgClass = TypeUtil.forName(constructorArgClassName);
	    			constructorArg = tideContext.findComponent(null, constructorArgClass, null);
	    		}
	    		catch (Exception e) {
	    			// Constructor arg not found 
	    		}
    		}
    		
            Class<?> validatorClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            try {
            	Constructor<?> c = validatorClass.getConstructor(constructorArgClass);
            	validator = (EntityValidator)c.newInstance(constructorArg);
            }
            catch (NoSuchMethodException e) {            	
            	validator = (EntityValidator)validatorClass.newInstance();
            }
            catch (InvocationTargetException e) {
            	log.error(e, "Could not initialize Tide validator " + className + " with argument of type " + constructorArgClassName);
            }
        }
        catch (ClassNotFoundException e) {
            // Ignore: Hibernate Validator not present
        }
        catch (NoClassDefFoundError e) {
            // Ignore: Hibernate Validator not present
        }
        catch (IllegalAccessException e) {
            // Ignore: Hibernate Validator not present
        }
        catch (InstantiationException e) {
            // Ignore: Hibernate Validator not present
        }
    }
    
    
    public InvalidValue[] validateObject(Object entity, String propertyName, Object value) {
    	initValidator();
        if (entity != null && validator != null)
            return validator.getPotentialInvalidValues(entity.getClass(), propertyName, value);
        
        return EMPTY_INVALID_VALUES;
    }

    
    public void login() {
    }
    
    public void logout() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpSession session = context.getSession(false);
        if (session != null)
            session.invalidate();
    }
    
    public void resyncContext() {
    }
    
    
    protected TideServiceContext lookupContext() {
        return null;
    }
    
    protected TideServiceContext getTideContext() {
        return tideContext;
    }
    
    
    
    @Override
    protected Object adjustInvokee(RemotingMessage request, String methodName, Object[] args) throws ServiceException {
        if ("invokeComponent".equals(methodName)) {
            String componentName = (String)args[0];
            String componentClassName = (String)args[1];
            String componentPath = (String)args[2];
            Class<?> componentClass = null;
            try {
	            if (componentClassName != null)
	            	componentClass = TypeUtil.forName(componentClassName);
            }
            catch (ClassNotFoundException e) {
            	throw new ServiceException("Component class not found " + componentClassName, e);
            }
            log.debug("Setting invokee to %s", componentName);
            
            Object instance = tideContext.findComponent(componentName, componentClass, componentPath);
            Set<Class<?>> componentClasses = instance != null ? tideContext.findComponentClasses(componentName, componentClass, componentPath) : null;
            
            GraniteContext context = GraniteContext.getCurrentInstance();
            if (instance != null && componentClasses != null && ((GraniteConfig)context.getGraniteConfig()).isComponentTideEnabled(componentName, componentClasses, instance))
                return tideContext.adjustInvokee(instance, componentName, componentClasses);
            
            if (instance != null)
                log.error("SECURITY CHECK: Remote call refused to a non Tide-enabled component: " + componentName + "." + args[1] + ", class: " + componentClasses + ", instance: " + instance);
            throw SecurityServiceException.newAccessDeniedException("Component [" + componentName + (componentClassName != null ? " of class " + componentClassName : "") + "] not found");
        }
        
        return super.adjustInvokee(request, methodName, args);
    }
    

    @Override
    protected Object[] beforeMethodSearch(Object invokee, String methodName, Object[] args) {
        if ("invokeComponent".equals(methodName)) { 
        	return tideContext.beforeMethodSearch(invokee, methodName, args);
        } 
        else if ("initializeObject".equals(methodName)) {
        	return new Object[] { methodName, new Object[] { args[0], args[1] } };
        } 
        else if ("validateObject".equals(methodName)) {
            return new Object[] { methodName, new Object[] { args[0], args[1], args[2] } };
        }
        	
        return new Object[] { methodName, new Object[] {} };
    }

    
    private static final String DATAENABLED_HANDLED = "org.granite.tide.invoker.dataEnabled";

    @Override
    protected void beforeInvocation(ServiceInvocationContext context) {
        RemotingMessage message = (RemotingMessage)context.getMessage();
        GraniteContext graniteContext = GraniteContext.getCurrentInstance();
        
        Object[] originArgs = (Object[])message.getBody();
        IInvocationCall call = (IInvocationCall)originArgs[originArgs.length-1];
        
        String operation = message.getOperation();
        String componentName = "invokeComponent".equals(operation) ? (String)originArgs[0] : null;
        String componentClassName = "invokeComponent".equals(operation) ? (String)originArgs[1] : null;
        Class<?> componentClass = null;
        try {
	        if (componentClassName != null)
	        	componentClass = TypeUtil.forName(componentClassName);
        }
        catch (ClassNotFoundException e) {
        	throw new ServiceException("Component class not found " + componentClassName, e);
        }
        
        graniteContext.getRequestMap().put(TideServiceInvoker.class.getName(), this);
        
        if (componentName != null || componentClass != null) {
	    	Converters converters = ((ConvertersConfig)graniteContext.getGraniteConfig()).getConverters();
	    	
	    	Set<Class<?>> componentClasses = tideContext.findComponentClasses(componentName, componentClass, null);
	    	for (Class<?> cClass : componentClasses) {
	    		try {
	    			Method m = cClass.getMethod(context.getMethod().getName(), context.getMethod().getParameterTypes());
	    			for (int i = 0; i < m.getGenericParameterTypes().length; i++)
	    				context.getParameters()[i] = converters.convert(context.getParameters()[i], m.getGenericParameterTypes()[i]);
	    			
	    			break;
	    		}
	    		catch (NoSuchMethodException e) {
	    		}
	    	}
	    	
	    	for (Class<?> cClass : componentClasses) {
	        	DataEnabled dataEnabled = cClass.getAnnotation(DataEnabled.class);
	        	if (dataEnabled != null && !dataEnabled.useInterceptor()) {
	        		graniteContext.getRequestMap().put(DATAENABLED_HANDLED, true);
	        		DataContext.init(dataEnabled.topic(), dataEnabled.params(), dataEnabled.publish());
	    			prepareDataObserver(dataEnabled);
	        		break;
	        	}
	    	}
        }
        
        Throwable error = null;
        try {
        	tideContext.prepareCall(context, call, componentName, componentClass);
        }
        catch (ServiceException e) {
        	error = e;
        }
        catch (Throwable e) {
        	if (e instanceof InvocationTargetException)
        		error = ((InvocationTargetException)e).getTargetException();
        	else
        		error = e;
        } 
        finally {
        	if (error != null)
        		throw factory.getServiceExceptionHandler().handleInvocationException(context, error);
        }
    }
        
    protected void prepareDataObserver(DataEnabled dataEnabled) {
		DataContext.observe();
    }


    @Override
    protected Object afterInvocation(ServiceInvocationContext context, Object result) {
    	Object res = null;
    	
    	String componentName = null;
        Class<?> componentClass = null;
    	try {
	        Object[] originArgs = (Object[])context.getMessage().getBody();
	        String operation = ((RemotingMessage)context.getMessage()).getOperation();
	        componentName = "invokeComponent".equals(operation) ? (String)originArgs[0] : null;
	        String componentClassName = "invokeComponent".equals(operation) ? (String)originArgs[1] : null;
	        try {
		        if (componentClassName != null)
		        	componentClass = TypeUtil.forName(componentClassName);
	        }
	        catch (ClassNotFoundException e) {
	        	throw new ServiceException("Component class not found " + componentClassName, e);
	        }
    	}
    	finally {
            Throwable error = null;
            try {
            	res = tideContext.postCall(context, result, componentName, componentClass);
            }
            catch (ServiceException e) {
            	error = e;
            }
            catch (Throwable e) {
            	if (e instanceof InvocationTargetException)
            		error = ((InvocationTargetException)e).getTargetException();
            	else
            		error = e;
            }
            finally {
            	if (error != null)
            		throw factory.getServiceExceptionHandler().handleInvocationException(context, error);
            }
    	}
    	
    	DataMergeContext.remove();
    	
    	// DataContext has been setup by beforeInvocation
		if (GraniteContext.getCurrentInstance().getRequestMap().get(DATAENABLED_HANDLED) != null)
	    	publishDataUpdates();
    	
		DataContext.remove();
    	
    	return res;
    }
    
    protected void publishDataUpdates() {
		DataContext.publish(PublishMode.ON_SUCCESS);
    }

    
    @Override
    protected void afterInvocationError(ServiceInvocationContext context, Throwable error) {
    	String componentName = null;
        Class<?> componentClass = null;
    	try {
	        Object[] originArgs = (Object[])context.getMessage().getBody();
	        String operation = ((RemotingMessage)context.getMessage()).getOperation();
	        componentName = "invokeComponent".equals(operation) ? (String)originArgs[0] : null;
	        String componentClassName = "invokeComponent".equals(operation) ? (String)originArgs[1] : null;
	        try {
		        if (componentClassName != null)
		        	componentClass = TypeUtil.forName(componentClassName);
	        }
	        catch (ClassNotFoundException e) {
	        	throw new ServiceException("Component class not found " + componentClassName, e);
	        }
    	}
    	finally {
    		tideContext.postCallFault(context, error, componentName, componentClass);
    	}
    	
    	DataMergeContext.remove();
		DataContext.remove();
    }
}
