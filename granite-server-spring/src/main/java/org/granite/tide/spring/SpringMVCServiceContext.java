/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.IInvocationCall;
import org.granite.tide.IInvocationResult;
import org.granite.tide.annotations.BypassTideMerge;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.TypeUtil;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;


/**
 * @author William DRAI
 */
public class SpringMVCServiceContext extends SpringServiceContext {

    private static final long serialVersionUID = 1L;
    
    private static final String REQUEST_VALUE = "__REQUEST_VALUE__";
    
    private static final Logger log = Logger.getLogger(SpringMVCServiceContext.class);

    
    public SpringMVCServiceContext() throws ServiceException {
        super();
    }
    
    public SpringMVCServiceContext(ApplicationContext springContext) throws ServiceException {
        super(springContext);
    }
    
    
    @Override
    public Object adjustInvokee(Object instance, String componentName, Set<Class<?>> componentClasses) {
    	for (Class<?> componentClass : componentClasses) {
	    	if (componentClass.isAnnotationPresent(org.springframework.stereotype.Controller.class)) {
	    		return new AnnotationMethodHandlerAdapter() {
	    			@Override
	    			protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object target, String objectName) throws Exception {
	    				return new ControllerRequestDataBinder(request, target, objectName);
	    			}
	    		};
	    	}
    	}
    	if (Controller.class.isInstance(instance) || (componentName != null && componentName.endsWith("Controller")))
    		return new SimpleControllerHandlerAdapter();
    	
    	return instance;
    }
    
    @Override
    protected Object internalFindComponent(String componentName, Class<?> componentClass) {
    	try {
    		return super.internalFindComponent(componentName, componentClass);
    	}
    	catch (NoSuchBeanDefinitionException e) {
//    		Map<String, HandlerMapping> handlerMappingsMap = springContext.getBeansOfType(HandlerMapping.class);
//    		if (handlerMappingsMap)
	    	if (componentName != null && componentName.endsWith("Controller")) {
	    		try {
	    			int idx = componentName.lastIndexOf(".");
	    			String controllerName = idx > 0 
	    				? componentName.substring(0, idx+1) + componentName.substring(idx+1, idx+2).toUpperCase() + componentName.substring(idx+2)
	    				: componentName.substring(0, 1).toUpperCase() + componentName.substring(1);
	    				
	    			return springContext.getBean(controllerName);
	    		}
	        	catch (NoSuchBeanDefinitionException nexc2) {
	        	}
	    	}
    	}
    	
    	return null;    	
    }
    
    
    
    private static final String SPRINGMVC_BINDING_ATTR = "__SPRINGMVC_LOCAL_BINDING__";
    
    @Override
    public Object[] beforeMethodSearch(Object instance, String methodName, Object[] args) {
    	if (instance instanceof HandlerAdapter) {
    		boolean grails = getSpringContext().getClass().getName().indexOf("Grails") > 0;
    		
    		String componentName = (String)args[0];
    		String componentClassName = (String)args[1];
            Class<?> componentClass = null;
            try {
    	        if (componentClassName != null)
    	        	componentClass = TypeUtil.forName(componentClassName);
            }
            catch (ClassNotFoundException e) {
            	throw new ServiceException("Component class not found " + componentClassName, e);
            }
    		Object component = findComponent(componentName, componentClass);
    		Set<Class<?>> componentClasses = findComponentClasses(componentName, componentClass);
    		Object handler = component;
    		if (grails && componentName.endsWith("Controller")) {
    			// Special handling for Grails controllers
    			handler = springContext.getBean("mainSimpleController");
    		}
    		HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
    		@SuppressWarnings("unchecked")
    		Map<String, Object> requestMap = (args[3] != null && args[3] instanceof Object[] && ((Object[])args[3]).length >= 1 && ((Object[])args[3]).length <= 2 && ((Object[])args[3])[0] instanceof Map) 
    			? (Map<String, Object>)((Object[])args[3])[0] 
    			: null;
    		boolean localBinding = false;
    		if (args[3] != null && args[3] instanceof Object[] && ((Object[])args[3]).length == 2 
    				&& ((Object[])args[3])[0] instanceof Map<?, ?> && ((Object[])args[3])[1] instanceof Boolean)
    			localBinding = (Boolean)((Object[])args[3])[1];
    		context.getRequestMap().put(SPRINGMVC_BINDING_ATTR, localBinding);
    		
    		Map<String, Object> valueMap = null;
    		if (args[4] instanceof InvocationCall) {
    			valueMap = new HashMap<String, Object>();
    			for (ContextUpdate u : ((InvocationCall)args[4]).getUpdates())
    				valueMap.put(u.getComponentName() + (u.getExpression() != null ? "." + u.getExpression() : ""), u.getValue());
    		}
    		
    		if (grails) {
				// Special handling for Grails controllers
	    		try {
	    			for (Class<?> cClass : componentClasses) {
	    				if (cClass.isInterface())
	    					continue;
		    			Method m = cClass.getDeclaredMethod("getProperty", String.class);
		    			@SuppressWarnings("unchecked")
		    			Map<String, Object> map = (Map<String, Object>)m.invoke(component, "params");
		    			if (requestMap != null)
		    				map.putAll(requestMap);
		        		if (valueMap != null)
		        			map.putAll(valueMap);
	    			}
	    		}
	    		catch (Exception e) {
	    			// Ignore, probably not a Grails controller
	    		}
    		}
     		ControllerRequestWrapper rw = new ControllerRequestWrapper(grails, context.getRequest(), componentName, (String)args[2], requestMap, valueMap);
    		return new Object[] { "handle", new Object[] { rw, context.getResponse(), handler }};
    	}
    	
    	return super.beforeMethodSearch(instance, methodName, args);
    }
    
    
    @Override
    public void prepareCall(ServiceInvocationContext context, IInvocationCall c, String componentName, Class<?> componentClass) {
    	super.prepareCall(context, c, componentName, componentClass);
		
    	if (componentName == null)
    		return;
    	
		Object component = findComponent(componentName, componentClass);
		
		if (context.getBean() instanceof HandlerAdapter) {
			// In case of Spring controllers, call interceptors
	        ApplicationContext webContext = getSpringContext();
	        String[] interceptorNames = webContext.getBeanNamesForType(HandlerInterceptor.class);
	        String[] webRequestInterceptors = webContext.getBeanNamesForType(WebRequestInterceptor.class);
	        HandlerInterceptor[] interceptors = new HandlerInterceptor[interceptorNames.length+webRequestInterceptors.length];
	
	        int j = 0;
	        for (int i = 0; i < webRequestInterceptors.length; i++)
	            interceptors[j++] = new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor)webContext.getBean(webRequestInterceptors[i]));
	        for (int i = 0; i < interceptorNames.length; i++)
	            interceptors[j++] = (HandlerInterceptor)webContext.getBean(interceptorNames[i]);
	        
			HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
			
			graniteContext.getRequestMap().put(HandlerInterceptor.class.getName(), interceptors);
			
			try {
		        for (int i = 0; i < interceptors.length; i++) {
		            HandlerInterceptor interceptor = interceptors[i];
		            interceptor.preHandle((HttpServletRequest)context.getParameters()[0], graniteContext.getResponse(), component);
		        }
			}
			catch (Exception e) {
				throw new ServiceException(e.getMessage(), e);
			}
		}
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public IInvocationResult postCall(ServiceInvocationContext context, Object result, String componentName, Class<?> componentClass) {
    	List<ContextUpdate> results = null;
    	
    	Object component = null;
    	if (componentName != null && context.getBean() instanceof HandlerAdapter) {
			component = findComponent(componentName, componentClass);
			
			HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
			
	    	Map<String, Object> modelMap = null;
	    	if (result instanceof ModelAndView) {
	    		ModelAndView modelAndView = (ModelAndView)result;
	    		modelMap = modelAndView.getModel();
	    		result = modelAndView.getViewName();
	        	
	    		if (context.getBean() instanceof HandlerAdapter) {
		    		try {
		    			HandlerInterceptor[] interceptors = (HandlerInterceptor[])graniteContext.getRequestMap().get(HandlerInterceptor.class.getName());
		    			
		    			if (interceptors != null) {
			                for (int i = interceptors.length-1; i >= 0; i--) {
			                    HandlerInterceptor interceptor = interceptors[i];
			                    interceptor.postHandle((HttpServletRequest)context.getParameters()[0], graniteContext.getResponse(), component, modelAndView);
			                }
	
				            triggerAfterCompletion(component, interceptors.length-1, interceptors, graniteContext.getRequest(), graniteContext.getResponse(), null);
		    			}
		    		}
		    		catch (Exception e) {
		    			throw new ServiceException(e.getMessage(), e);
		    		}
	    		}
	    	}
	    	
	    	if (modelMap != null) {
	    		Boolean localBinding = (Boolean)graniteContext.getRequestMap().get(SPRINGMVC_BINDING_ATTR);
	    		
		    	results = new ArrayList<ContextUpdate>();
		    	for (Map.Entry<String, Object> me : modelMap.entrySet()) {
					if (me.getKey().toString().startsWith("org.springframework.validation.")
							|| (me.getValue() != null && (
									me.getValue().getClass().getName().startsWith("groovy.lang.ExpandoMetaClass")
									|| me.getValue().getClass().getName().indexOf("$_closure") > 0
									|| me.getValue() instanceof Class)))									
						continue;
					String variableName = me.getKey().toString();
					if (Boolean.TRUE.equals(localBinding))
						results.add(new ContextUpdate(componentName, variableName, me.getValue(), 3, false));
					else
						results.add(new ContextUpdate(variableName, null, me.getValue(), 3, false));
		    	}
	    	}
	        
			boolean grails = getSpringContext().getClass().getName().indexOf("Grails") > 0;
			if (grails) {
				// Special handling for Grails controllers: get flash content
				try {
		    		Set<Class<?>> componentClasses = findComponentClasses(componentName, componentClass);
		    		for (Class<?> cClass : componentClasses) {
		    			if (cClass.isInterface())
		    				continue;
						Method m = cClass.getDeclaredMethod("getProperty", String.class);
						Map<String, Object> map = (Map<String, Object>)m.invoke(component, "flash");
						if (results == null)
							results = new ArrayList<ContextUpdate>();
						for (Map.Entry<String, Object> me : map.entrySet()) {
							Object value = me.getValue();
							if (value != null && value.getClass().getName().startsWith("org.codehaus.groovy.runtime.GString"))
								value = value.toString();
							results.add(new ContextUpdate("flash", me.getKey(), value, 3, false));
						}
		    		}
				}
				catch (Exception e) {
					throw new ServiceException("Flash scope retrieval failed", e);
				}
			}
    	}
		
    	DataContext dataContext = DataContext.get();
		Object[][] updates = dataContext != null ? dataContext.getUpdates() : null;
		
        InvocationResult ires = new InvocationResult(result, results);
        if (component == null)
        	component = context.getBean();
    	if (isBeanAnnotationPresent(component, BypassTideMerge.class))
    		ires.setMerge(false);
    	else if (!(context.getParameters().length > 0 && context.getParameters()[0] instanceof ControllerRequestWrapper)) {
    		if (isBeanMethodAnnotationPresent(component, context.getMethod().getName(), context.getMethod().getParameterTypes(), BypassTideMerge.class))
    			ires.setMerge(false);
    	}
    	
        ires.setUpdates(updates);
        
        return ires;
    }

    @Override
    public void postCallFault(ServiceInvocationContext context, Throwable t, String componentName, Class<?> componentClass) {
    	if (componentName != null && context.getBean() instanceof HandlerAdapter) {
			HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
			
			Object component = findComponent(componentName, componentClass);
			
			HandlerInterceptor[] interceptors = (HandlerInterceptor[])graniteContext.getRequestMap().get(HandlerInterceptor.class.getName());
	
	        triggerAfterCompletion(component, interceptors.length-1, interceptors, 
	        		graniteContext.getRequest(), graniteContext.getResponse(), 
	        		t instanceof Exception ? (Exception)t : null);
    	}
		
        super.postCallFault(context, t, componentName, componentClass);
    }

    
    private void triggerAfterCompletion(Object component, int interceptorIndex, HandlerInterceptor[] interceptors, HttpServletRequest request, HttpServletResponse response, Exception ex) {
		for (int i = interceptorIndex; i >= 0; i--) {
			HandlerInterceptor interceptor = interceptors[i];
			try {
				interceptor.afterCompletion(request, response, component, ex);
			}
			catch (Throwable ex2) {
				log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
			}
		}
    }

    
    
    private class ControllerRequestWrapper extends HttpServletRequestWrapper {
    	private String initialComponentName = null;
    	private String componentName = null;
    	private String methodName = null;
    	private Map<String, Object> requestMap = null;
    	private Map<String, Object> valueMap = null;
    	private boolean localBinding;
    	
		public ControllerRequestWrapper(boolean grails, HttpServletRequest request, String componentName, String methodName, Map<String, Object> requestMap, Map<String, Object> valueMap) {
			super(request);
			this.initialComponentName = componentName;
			this.componentName = componentName.substring(0, componentName.length()-"Controller".length());
			if (this.componentName.indexOf(".") > 0)
				this.componentName = this.componentName.substring(this.componentName.lastIndexOf(".")+1);
			if (grails)
				this.componentName = this.componentName.substring(0, 1).toLowerCase() + this.componentName.substring(1);
			this.methodName = methodName;
			this.requestMap = requestMap;
			this.valueMap = valueMap;
			this.localBinding = Boolean.TRUE.equals(request.getAttribute(SPRINGMVC_BINDING_ATTR));
		}
    	
		@Override
		public String getRequestURI() {
			return getContextPath() + "/" + componentName + "/" + methodName;
		}
		
		@Override
		public String getServletPath() {
			return "/" + componentName + "/" + methodName;
		}
		
		public Object getRequestValue(String key) {
			return requestMap != null ? requestMap.get(key) : null;
		}
		
		public Object getBindValue(String key) {
			if (valueMap == null)
				return null;
			
			return localBinding && valueMap.containsKey(initialComponentName + "." + key)
					? valueMap.get(initialComponentName + "." + key)
					: valueMap.get(key);
		}
		
		@Override
		public String getParameter(String name) {
			return requestMap != null && requestMap.containsKey(name) ? REQUEST_VALUE : null;
		}
		
		@Override
		public String[] getParameterValues(String name) {
			return requestMap != null && requestMap.containsKey(name) ? new String[] { REQUEST_VALUE } : null;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Map getParameterMap() {
			Map<String, Object> pmap = new HashMap<String, Object>();
			if (requestMap != null) {
				for (String name : requestMap.keySet())
					pmap.put(name, REQUEST_VALUE);
			}
			return pmap; 
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Enumeration getParameterNames() {
			Hashtable ht = new Hashtable();
			if (requestMap != null)
				ht.putAll(requestMap);
			return ht.keys();
		}
    }
    
    
    private class ControllerRequestDataBinder extends ServletRequestDataBinder {
    	
    	private ControllerRequestWrapper wrapper = null;
    	private Object target = null;

		public ControllerRequestDataBinder(ServletRequest request, Object target, String objectName) {
			super(target, objectName);
			this.wrapper = (ControllerRequestWrapper)request;
			this.target = target;
		}
		
		private Object getBindValue(boolean request, Class<?> requiredType) {
			GraniteContext context = GraniteContext.getCurrentInstance();
			ClassGetter classGetter = context.getGraniteConfig().getClassGetter();
			Object value = request ? wrapper.getRequestValue(getObjectName()) : wrapper.getBindValue(getObjectName());
			if (requiredType != null) {
				Converter converter = context.getGraniteConfig().getConverters().getConverter(value, requiredType);
				if (converter != null)
					value = converter.convert(value, requiredType);
			}
			if (value != null && !request)
				return SpringMVCServiceContext.this.mergeExternal(classGetter, value, null, null, null);
			return value;
		}

		@Override
		public void bind(ServletRequest request) {
			Object value = getBindValue(false, null);
			if (value != null)
				target = value;
		}
		
		@Override
		public Object getTarget() {
			return target;			
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object convertIfNecessary(Object value, Class requiredType, MethodParameter methodParam) throws TypeMismatchException {
			if (target == null && value == REQUEST_VALUE || (value instanceof String[] && ((String[])value)[0] == REQUEST_VALUE))
				return getBindValue(true, requiredType);
			return super.convertIfNecessary(value, requiredType, methodParam);
		}
    }
}
