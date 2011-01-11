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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
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
        
    	for (Class<?> ti : flexFilter.tideInterfaces()) {
    		try {
    			graniteConfig.getTideComponentMatchers().add(new TideComponentInstanceOfMatcher(ti.getName(), false));
    			log.debug("Enabled components implementing %s for Tide remoting", ti);
    		}
    		catch (Exception e) {
    			log.error(e, "Could not add tide-component interface %s", ti);
    		}
    	}
    	for (Class<? extends Annotation> ta : flexFilter.tideAnnotations()) {
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
    		graniteConfig.registerExceptionConverter(ec);
			log.debug("Registered exception converter %s", ec);
    	}
    	
    	if (!flexFilter.securityServiceClass().equals(SecurityService.class)) {
    		try {
    			graniteConfig.setSecurityService(ClassUtil.newInstance(flexFilter.securityServiceClass(), SecurityService.class));
        	}
        	catch (Exception e) {
        		throw new ServletException("Could not setup security service", e);
        	}
    	}
    	else if (graniteConfig.getSecurityService() == null) {
    		String securityServiceClass = null;
    		// Try auto-detect
    		try {
    			ClassUtil.forName("org.mortbay.jetty.Request");
    			securityServiceClass = "org.granite.messaging.service.security.Jetty6SecurityService";
    		}
    		catch (ClassNotFoundException e1) {
    			try {
    				ClassUtil.forName("weblogic.servlet.security.ServletAuthentication");
        			securityServiceClass = "org.granite.messaging.service.security.WebLogicSecurityService";
    			}
    			catch (ClassNotFoundException e2) {
        			try {
        				ClassUtil.forName("weblogic.servlet.security.ServletAuthentication");
            			securityServiceClass = "org.granite.messaging.service.security.WebLogicSecurityService";
        			}
        			catch (ClassNotFoundException e3) {
            			securityServiceClass = "org.granite.messaging.service.security.Tomcat7SecurityService";
        			}
    			}
        		try {
        			graniteConfig.setSecurityService(ClassUtil.newInstance(securityServiceClass, SecurityService.class));
            	}
            	catch (Exception e) {
            		throw new ServletException("Could not setup security service", e);
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
        
        Channel channel = servicesConfig.findChannelById("graniteamf");
        if (channel == null) {
        	channel = new Channel("graniteamf", "mx.messaging.channels.AMFChannel", 
        		new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint"), 
        		new XMap());
        	servicesConfig.addChannel(channel);
        }

    	XMap factoryProperties = new XMap();
    	String lookup = "java:global/{context.root}/{capitalized.component.name}Bean";
    	if (context.getClass().getClassLoader().getClass().getName().startsWith("org.jboss"))
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

        if (flexFilter.tide()) {
        	Factory factory = servicesConfig.findFactoryById("tide-" + flexFilter.type() + "-factory");
        	if (factory == null) {
        		factory = new Factory("tide-" + flexFilter.type() + "-factory", 
        			flexFilter.factoryClass().getName(), factoryProperties);
        		servicesConfig.addFactory(factory);
        	}
        	
        	Service service = servicesConfig.findServiceById("granite-service");
        	if (service == null) {
        		service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        				"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
	        	List<String> channelIds = new ArrayList<String>();
	        	channelIds.add("graniteamf");
	        	List<String> tideRoles = flexFilter.tideRoles().length == 0 ? null : Arrays.asList(flexFilter.tideRoles());
	        	Destination destination = new Destination(flexFilter.type(), channelIds, new XMap(), tideRoles, null, null);
	        	destination.getProperties().put("factory", "tide-" + flexFilter.type() + "-factory");
	        	if (!("".equals(flexFilter.entityManagerFactoryJndiName())))
	        		destination.getProperties().put("entity-manager-factory-jndi-name", flexFilter.entityManagerFactoryJndiName());
	        	else if (!("".equals(flexFilter.entityManagerJndiName())))
	        		destination.getProperties().put("entity-manager-jndi-name", flexFilter.entityManagerJndiName());
	        	if (!("".equals(flexFilter.validatorClassName())))
	        		destination.getProperties().put("validator-class-name", flexFilter.validatorClassName());
	        	service.getDestinations().put(flexFilter.type(), destination);
	        	servicesConfig.addService(service);
        	}
            
        	if (flexFilter.factoryClass().getName().equals("org.granite.tide.ejb.EjbServiceFactory"))
        		servicesConfig.scan(null);
        	
        	log.info("Registered Tide service factory and destination");
        }
        else {
        	Factory factory = new Factory(flexFilter.type() + "-factory", flexFilter.factoryClass().getName(), factoryProperties);
        	servicesConfig.addFactory(factory);
        	
        	Service service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        		"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        	servicesConfig.addService(service);
            
            servicesConfig.scan(null);
        	
        	log.info("Registered " + flexFilter.factoryClass() + " service factory");
        }
    }
}
