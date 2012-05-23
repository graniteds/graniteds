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

package org.granite.config;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Factory;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.config.servlet3.FlexFilter;
import org.granite.jmx.GraniteMBeanInitializer;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.externalizer.BigDecimalExternalizer;
import org.granite.messaging.amf.io.util.externalizer.BigIntegerExternalizer;
import org.granite.messaging.amf.io.util.externalizer.LongExternalizer;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.SimpleServiceFactory;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.tide.TideComponentAnnotatedWithMatcher;
import org.granite.messaging.service.tide.TideComponentInstanceOfMatcher;
import org.granite.messaging.service.tide.TideComponentNameMatcher;
import org.granite.messaging.service.tide.TideComponentTypeMatcher;
import org.granite.util.ClassUtil;
import org.granite.util.ServletParams;
import org.granite.util.XMap;


/**
 * @author William DRAI
 */
public class GraniteConfigListener implements ServletContextListener {

    private static final String GRANITE_CONFIG_SHUTDOWN_KEY = GraniteConfig.class.getName() + "_SHUTDOWN";
    public static final String GRANITE_CONFIG_ATTRIBUTE = "org.granite.config.flexFilter";
    public static final String GRANITE_MBEANS_ATTRIBUTE = "registerGraniteMBeans";

    private static final Logger log = Logger.getLogger(GraniteConfigListener.class);

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();

            log.info("Initializing GraniteDS...");
            
            Class<?> flexFilterClass = (Class<?>)context.getAttribute(GRANITE_CONFIG_ATTRIBUTE);
            if (flexFilterClass != null)
            	context.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_DEFAULT_KEY, "org/granite/config/servlet3/granite-config-servlet3.xml");
            
            GraniteConfig gConfig = ServletGraniteConfig.loadConfig(context);
            ServletServicesConfig.loadConfig(context);
            
            if (flexFilterClass != null)
            	configureServices(context, flexFilterClass);
            
            if (gConfig.isRegisterMBeans()) {
            	GraniteMBeanInitializer.registerMBeans(context, 
            			ServletGraniteConfig.getServletConfig(context), 
            			ServletServicesConfig.getServletConfig(context));
            }

            log.info("GraniteDS initialized");
        }
        catch (Exception e) {
            throw new RuntimeException("Could not initialize Granite environment", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        log.info("Stopping GraniteDS...");

        @SuppressWarnings("unchecked")
        List<ShutdownListener> listeners = (List<ShutdownListener>)sce.getServletContext().getAttribute(GRANITE_CONFIG_SHUTDOWN_KEY);
        if (listeners != null) {
            try {
                for (ShutdownListener listener : listeners)
                    listener.stop();
            }
            catch (Exception e) {
                throw new RuntimeException("Could not destroy Granite environment", e);
            }
        }

        if (ServletParams.get(context, GRANITE_MBEANS_ATTRIBUTE, Boolean.TYPE, false))
        	GraniteMBeanInitializer.unregisterMBeans(context);

        log.info("GraniteDS stopped");
    }

    public static synchronized void registerShutdownListener(ServletContext context, ShutdownListener listener) {
        @SuppressWarnings("unchecked")
        List<ShutdownListener> listeners = (List<ShutdownListener>)context.getAttribute(GRANITE_CONFIG_SHUTDOWN_KEY);
        if (listeners == null) {
            listeners = new ArrayList<ShutdownListener>();
            context.setAttribute(GRANITE_CONFIG_SHUTDOWN_KEY, listeners);
        }
        listeners.add(listener);
    }
    
    
    private void configureServices(ServletContext context, Class<?> flexFilterClass) throws ServletException {
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(context);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(context);
    	
        FlexFilter flexFilter = flexFilterClass.getAnnotation(FlexFilter.class);
        
        ConfigProvider configProvider = null;
        
        boolean useTide = flexFilter.tide();
        String type = flexFilter.type();
        Class<?> factoryClass = null;
        Set<Class<?>> tideInterfaces = new HashSet<Class<?>>(Arrays.asList(flexFilter.tideInterfaces()));
        Set<Class<? extends Annotation>> tideAnnotations = new HashSet<Class<? extends Annotation>>(Arrays.asList(flexFilter.tideAnnotations()));
        
        if (!flexFilter.configProviderClass().equals(ConfigProvider.class)) {
        	try {
        		configProvider = ClassUtil.newInstance(flexFilter.configProviderClass(), new Class[] { ServletContext.class }, new Object[] { context });
        		
        		if (configProvider.useTide() != null)
        			useTide = configProvider.useTide();
        		
        		if (configProvider.getType() != null)
        			type = configProvider.getType();
        		
        		factoryClass = configProvider.getFactoryClass();
        		
        		if (configProvider.getTideInterfaces() != null)
        			tideInterfaces.addAll(Arrays.asList(configProvider.getTideInterfaces()));
        		
        		if (configProvider.getTideAnnotations() != null)
        			tideAnnotations.addAll(Arrays.asList(configProvider.getTideAnnotations()));
			}
			catch (Exception e) {
				log.error(e, "Could not set config provider of type %s", flexFilter.configProviderClass().getName());
			}
        }
        
        if (!flexFilter.factoryClass().equals(ServiceFactory.class))
        	factoryClass = flexFilter.factoryClass();
        
        if (factoryClass == null) {
        	factoryClass = SimpleServiceFactory.class;
        	useTide = false;
        }
        
    	for (Class<?> ti : tideInterfaces) {
    		try {
    			graniteConfig.getTideComponentMatchers().add(new TideComponentInstanceOfMatcher(ti.getName(), false));
    			log.debug("Enabled components implementing %s for Tide remoting", ti);
    		}
    		catch (Exception e) {
    			log.error(e, "Could not add tide-component interface %s", ti);
    		}
    	}
    	for (Class<? extends Annotation> ta : tideAnnotations) {
    		try {
    			graniteConfig.getTideComponentMatchers().add(new TideComponentAnnotatedWithMatcher(ta.getName(), false));
    			log.debug("Enabled components annotated with %s for Tide remoting", ta);
    		}
    		catch (Exception e) {
    			log.error(e, "Could not add tide-component annotation %s", ta);
    		}
    	}
    	for (String tn : flexFilter.tideNames()) {
    		try {
    			graniteConfig.getTideComponentMatchers().add(new TideComponentNameMatcher(tn, false));
    			log.debug("Enabled components with name %s for Tide remoting", tn);
    		}
    		catch (Exception e) {
    			log.error(e, "Could not add tide-component name %s", tn);
    		}
    	}
    	for (String tt : flexFilter.tideTypes()) {
    		try {
    			graniteConfig.getTideComponentMatchers().add(new TideComponentTypeMatcher(tt, false));
    			log.debug("Enabled components with type %s for Tide remoting", tt);
    		}
    		catch (Exception e) {
    			log.error(e, "Could not add tide-component type %s", tt);
    		}
    	}
        
    	for (Class<? extends ExceptionConverter> ec : flexFilter.exceptionConverters()) {
    		graniteConfig.registerExceptionConverter(ec, true);
			log.debug("Registered exception converter %s", ec);
    	}
    	if (configProvider != null) {
    		for (ExceptionConverter ec : configProvider.findInstances(ExceptionConverter.class)) {
    			graniteConfig.registerExceptionConverter(ec, true);
    			log.debug("Registered exception converter %s", ec.getClass());
    		}
    	}
    	
    	if (flexFilter.useBigDecimal())
    		graniteConfig.setExternalizersByType(BigDecimal.class.getName(), BigDecimalExternalizer.class.getName());
    	
    	if (flexFilter.useBigInteger())
    		graniteConfig.setExternalizersByType(BigInteger.class.getName(), BigIntegerExternalizer.class.getName());
    	
    	if (flexFilter.useLong())
    		graniteConfig.setExternalizersByType(Long.class.getName(), LongExternalizer.class.getName());
    	
    	if (!flexFilter.securityServiceClass().equals(SecurityService.class)) {
    		try {
    			graniteConfig.setSecurityService(ClassUtil.newInstance(flexFilter.securityServiceClass(), SecurityService.class));
        	}
        	catch (Exception e) {
        		throw new ServletException("Could not setup security service", e);
        	}
    	}
    	else if (graniteConfig.getSecurityService() == null && configProvider != null) {
			SecurityService securityService = configProvider.findInstance(SecurityService.class);
			graniteConfig.setSecurityService(securityService);
		}
    	if (graniteConfig.getSecurityService() == null) {
    		String securityServiceClassName = null;
    		// Try auto-detect
    		try {
    			ClassUtil.forName("org.mortbay.jetty.Request");
    			securityServiceClassName = "org.granite.messaging.service.security.Jetty6SecurityService";
    		}
    		catch (ClassNotFoundException e1) {
    			try {
    				ClassUtil.forName("org.eclipse.jetty.server.Request");
        			securityServiceClassName = "org.granite.messaging.service.security.Jetty7SecurityService";
    			}
    			catch (ClassNotFoundException e1b) {
	    			try {
	    				ClassUtil.forName("weblogic.servlet.security.ServletAuthentication");
	    				securityServiceClassName = "org.granite.messaging.service.security.WebLogicSecurityService";
	    			}
	    			catch (ClassNotFoundException e2) {
	        			try {
	        				ClassUtil.forName("com.sun.appserv.server.LifecycleEvent");
	            			securityServiceClassName = "org.granite.messaging.service.security.GlassFishV3SecurityService";
	        			}
	        			catch (ClassNotFoundException e3) {
	        				securityServiceClassName = "org.granite.messaging.service.security.Tomcat7SecurityService";
	        			}
	    			}
	        		try {
	        			graniteConfig.setSecurityService((SecurityService)ClassUtil.newInstance(securityServiceClassName));
	            	}
	            	catch (Exception e) {
	            		throw new ServletException("Could not setup security service " + securityServiceClassName, e);
	            	}
    			}
    		}
    	}
        
        if (!flexFilter.amf3MessageInterceptor().equals(AMF3MessageInterceptor.class)) {
        	try {
        		graniteConfig.setAmf3MessageInterceptor(ClassUtil.newInstance(flexFilter.amf3MessageInterceptor(), AMF3MessageInterceptor.class));
        	}
        	catch (Exception e) {
        		throw new ServletException("Could not setup amf3 message interceptor", e);
        	}
        }
        else if (graniteConfig.getAmf3MessageInterceptor() == null && configProvider != null) {
        	AMF3MessageInterceptor amf3MessageInterceptor = configProvider.findInstance(AMF3MessageInterceptor.class);
			graniteConfig.setAmf3MessageInterceptor(amf3MessageInterceptor);
        }
        
        Channel channel = servicesConfig.findChannelById("graniteamf");
        if (channel == null) {
        	channel = new Channel("graniteamf", "mx.messaging.channels.AMFChannel", 
        		new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint"), 
        		new XMap());
        	servicesConfig.addChannel(channel);
        }

    	XMap factoryProperties = new XMap();
    	String lookup = "java:global/{context.root}/{capitalized.component.name}Bean";
    	if (isJBoss6())
    		lookup = "{capitalized.component.name}Bean/local";
    	if (!("".equals(flexFilter.ejbLookup())))
    		lookup = flexFilter.ejbLookup();
    	if (lookup.indexOf("{context.root}") >= 0) {
    		try {
    			// Call by reflection because of JDK 1.4
    			Method m = context.getClass().getMethod("getContextPath");
    			String contextPath = (String)m.invoke(context);
    			lookup = lookup.replace("{context.root}", contextPath.substring(1));
    		}
    		catch (Exception e) {
    			log.error(e, "Could not get context path, please define lookup manually in @FlexFilter");
    		}
    	}
    	factoryProperties.put("lookup", lookup);

        if (useTide) {
        	Factory factory = servicesConfig.findFactoryById("tide-" + type + "-factory");
        	if (factory == null) {
        		factory = new Factory("tide-" + type + "-factory", factoryClass.getName(), factoryProperties);
        		servicesConfig.addFactory(factory);
        	}
        	
        	Service service = servicesConfig.findServiceById("granite-service");
        	if (service == null) {
        		service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        				"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
	        	List<String> channelIds = new ArrayList<String>();
	        	channelIds.add("graniteamf");
	        	List<String> tideRoles = flexFilter.tideRoles().length == 0 ? null : Arrays.asList(flexFilter.tideRoles());
	        	Destination destination = new Destination(type, channelIds, new XMap(), tideRoles, null, null);
	        	destination.getProperties().put("factory", "tide-" + type + "-factory");
	        	if (!("".equals(flexFilter.entityManagerFactoryJndiName())))
	        		destination.getProperties().put("entity-manager-factory-jndi-name", flexFilter.entityManagerFactoryJndiName());
	        	else if (!("".equals(flexFilter.entityManagerJndiName())))
	        		destination.getProperties().put("entity-manager-jndi-name", flexFilter.entityManagerJndiName());
	        	if (!("".equals(flexFilter.validatorClassName())))
	        		destination.getProperties().put("validator-class-name", flexFilter.validatorClassName());
	        	service.getDestinations().put(type, destination);
	        	servicesConfig.addService(service);
        	}
            
        	if (factoryClass.getName().equals("org.granite.tide.ejb.EjbServiceFactory"))
        		servicesConfig.scan(null);
        	
        	log.info("Registered Tide " + factoryClass + " service factory and " + type + " destination");
        }
        else {
        	Factory factory = new Factory(type + "-factory", factoryClass.getName(), factoryProperties);
        	servicesConfig.addFactory(factory);
        	
        	Service service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        		"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        	servicesConfig.addService(service);
            
            servicesConfig.scan(null);
        	
        	log.info("Registered " + factoryClass + " service factory");
        }
    }
	
	private static boolean isJBoss6() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/org/jboss/version.properties");
			if (is != null)
				return true;
		}
		catch (Throwable t) {
		}
		return false;
	}
}
