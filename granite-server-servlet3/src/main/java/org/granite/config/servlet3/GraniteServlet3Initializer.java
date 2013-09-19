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
package org.granite.config.servlet3;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;

import org.granite.config.ConfigProvider;
import org.granite.config.GraniteConfig;
import org.granite.config.GraniteConfigListener;
import org.granite.config.GraniteConfigListener.ServiceConfigurator;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Factory;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.gravity.GravityManager.GravityServiceConfigurator;
import org.granite.gravity.config.AbstractActiveMQTopicDestination;
import org.granite.gravity.config.AbstractJmsTopicDestination;
import org.granite.gravity.config.AbstractMessagingDestination;
import org.granite.gravity.config.servlet3.ActiveMQTopicDestination;
import org.granite.gravity.config.servlet3.JmsTopicDestination;
import org.granite.gravity.config.servlet3.MessagingDestination;
import org.granite.gravity.security.GravityDestinationSecurizer;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.externalizer.BigDecimalExternalizer;
import org.granite.messaging.amf.io.util.externalizer.BigIntegerExternalizer;
import org.granite.messaging.amf.io.util.externalizer.LongExternalizer;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.SimpleServiceFactory;
import org.granite.messaging.service.security.RemotingDestinationSecurizer;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.tide.TideComponentAnnotatedWithMatcher;
import org.granite.messaging.service.tide.TideComponentInstanceOfMatcher;
import org.granite.messaging.service.tide.TideComponentNameMatcher;
import org.granite.messaging.service.tide.TideComponentTypeMatcher;
import org.granite.messaging.webapp.AMFMessageFilter;
import org.granite.messaging.webapp.AMFMessageServlet;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;

/**
 * @author William DRAI
 */
@HandlesTypes({ServerFilter.class})
public class GraniteServlet3Initializer implements ServletContainerInitializer {

	public void onStartup(Set<Class<?>> scannedClasses, ServletContext servletContext) throws ServletException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		if (scannedClasses != null) {
			classes.addAll(scannedClasses);
			classes.remove(ServerFilter.class);	// JBossWeb adds the annotation ???
		}
		if (classes.size() > 1)
			throw new ServletException("Application must have only one ServerFilter configuration class");
		
		if (!classes.isEmpty()) {
			// Configure GraniteDS only if we find a config class annotated with @ServerFilter
			Class<?> clazz = classes.iterator().next();
			ServerFilter serverFilter = clazz.getAnnotation(ServerFilter.class);
			
			servletContext.setAttribute(GraniteConfigListener.GRANITE_CONFIG_ATTRIBUTE, new Servlet3ServiceConfigurator(clazz));
			
		    servletContext.addListener(new GraniteConfigListener());
			
			if (servletContext.getFilterRegistration("AMFMessageFilter") == null) {
				FilterRegistration.Dynamic graniteFilter = servletContext.addFilter("AMFMessageFilter", AMFMessageFilter.class);
				graniteFilter.addMappingForUrlPatterns(null, true, serverFilter.graniteUrlMapping());
			}
			if (servletContext.getServletRegistration("AMFMessageServlet") == null) {
				ServletRegistration.Dynamic graniteServlet = servletContext.addServlet("AMFMessageServlet", AMFMessageServlet.class);
				graniteServlet.setLoadOnStartup(1);
				graniteServlet.addMapping(serverFilter.graniteUrlMapping());
			}
			
			try {
				if (servletContext.getServletRegistration("GravityServlet") == null) {
					Class<? extends Servlet> gravityAsyncServletClass = TypeUtil.forName("org.granite.gravity.servlet3.GravityAsyncServlet", Servlet.class);
					ServletRegistration.Dynamic gravityServlet = servletContext.addServlet("GravityServlet", gravityAsyncServletClass);
					gravityServlet.setLoadOnStartup(1);
					gravityServlet.setAsyncSupported(true);
					gravityServlet.addMapping(serverFilter.gravityUrlMapping());
				}
			}
			catch (ClassNotFoundException e) {
				servletContext.log("Could not setup GravityAsyncServlet", e);
			}
		}
	}
	
	
	public static class Servlet3ServiceConfigurator implements ServiceConfigurator, GravityServiceConfigurator {
		
		private static final Logger log = Logger.getLogger(Servlet3ServiceConfigurator.class);
		
		private final Class<?> serverFilterClass;
		
		public Servlet3ServiceConfigurator(Class<?> serverFilterClass) {
			this.serverFilterClass = serverFilterClass;
		}
		
		public void initialize(ServletContext servletContext) {
			servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_DEFAULT_KEY, "org/granite/config/servlet3/granite-config-servlet3.xml");
		}
		
	    public void configureServices(ServletContext servletContext) throws ServletException {
	        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
	        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
	    	
	        ServerFilter serverFilter = serverFilterClass.getAnnotation(ServerFilter.class);
	        
	        ConfigProvider configProvider = null;
	        
	        boolean useTide = serverFilter.tide();
	        String type = serverFilter.type();
	        Class<?> factoryClass = null;
	        Set<Class<?>> tideInterfaces = new HashSet<Class<?>>(Arrays.asList(serverFilter.tideInterfaces()));
	        Set<Class<? extends Annotation>> tideAnnotations = new HashSet<Class<? extends Annotation>>(Arrays.asList(serverFilter.tideAnnotations()));
	        
	        if (!serverFilter.configProviderClass().equals(ConfigProvider.class)) {
	        	try {
	        		configProvider = TypeUtil.newInstance(serverFilter.configProviderClass(), new Class[] { ServletContext.class }, new Object[] { servletContext });
	        		
	        		servletContext.setAttribute(GraniteConfigListener.GRANITE_CONFIG_PROVIDER_ATTRIBUTE, configProvider);
	        		
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
					log.error(e, "Could not set config provider of type %s", serverFilter.configProviderClass().getName());
				}
	        }
	        
	        if (!serverFilter.factoryClass().equals(ServiceFactory.class))
	        	factoryClass = serverFilter.factoryClass();
	        
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
	    	for (String tn : serverFilter.tideNames()) {
	    		try {
	    			graniteConfig.getTideComponentMatchers().add(new TideComponentNameMatcher(tn, false));
	    			log.debug("Enabled components with name %s for Tide remoting", tn);
	    		}
	    		catch (Exception e) {
	    			log.error(e, "Could not add tide-component name %s", tn);
	    		}
	    	}
	    	for (String tt : serverFilter.tideTypes()) {
	    		try {
	    			graniteConfig.getTideComponentMatchers().add(new TideComponentTypeMatcher(tt, false));
	    			log.debug("Enabled components with type %s for Tide remoting", tt);
	    		}
	    		catch (Exception e) {
	    			log.error(e, "Could not add tide-component type %s", tt);
	    		}
	    	}
	        
	    	for (Class<? extends ExceptionConverter> ec : serverFilter.exceptionConverters()) {
	    		graniteConfig.registerExceptionConverter(ec, true);
				log.debug("Registered exception converter %s", ec);
	    	}
	    	if (configProvider != null) {
	    		for (ExceptionConverter ec : configProvider.findInstances(ExceptionConverter.class)) {
	    			graniteConfig.registerExceptionConverter(ec, true);
	    			log.debug("Registered exception converter %s", ec.getClass());
	    		}
	    	}
	    	
	    	if (serverFilter.useBigDecimal())
	    		graniteConfig.setExternalizersByType(BigDecimal.class.getName(), BigDecimalExternalizer.class.getName());
	    	
	    	if (serverFilter.useBigInteger())
	    		graniteConfig.setExternalizersByType(BigInteger.class.getName(), BigIntegerExternalizer.class.getName());
	    	
	    	if (serverFilter.useLong())
	    		graniteConfig.setExternalizersByType(Long.class.getName(), LongExternalizer.class.getName());
	    	
	    	if (!serverFilter.securityServiceClass().equals(SecurityService.class)) {
	    		try {
	    			graniteConfig.setSecurityService(TypeUtil.newInstance(serverFilter.securityServiceClass(), SecurityService.class));
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
	    			TypeUtil.forName("org.mortbay.jetty.Request");
	    			securityServiceClassName = "org.granite.messaging.service.security.Jetty6SecurityService";
	    		}
	    		catch (ClassNotFoundException e1) {
	    			try {
	    				TypeUtil.forName("org.eclipse.jetty.server.Request");
	        			securityServiceClassName = "org.granite.messaging.service.security.Jetty7SecurityService";
	    			}
	    			catch (ClassNotFoundException e1b) {
		    			try {
		    				TypeUtil.forName("weblogic.servlet.security.ServletAuthentication");
		    				securityServiceClassName = "org.granite.messaging.service.security.WebLogicSecurityService";
		    			}
		    			catch (ClassNotFoundException e2) {
		        			try {
		        				TypeUtil.forName("com.sun.appserv.server.LifecycleEvent");
		            			securityServiceClassName = "org.granite.messaging.service.security.GlassFishV3SecurityService";
		        			}
		        			catch (ClassNotFoundException e3) {
		        				securityServiceClassName = "org.granite.messaging.service.security.Tomcat7SecurityService";
		        			}
		    			}
		        		try {
		        			graniteConfig.setSecurityService((SecurityService)TypeUtil.newInstance(securityServiceClassName));
		            	}
		            	catch (Exception e) {
		            		throw new ServletException("Could not setup security service " + securityServiceClassName, e);
		            	}
	    			}
	    		}
	    	}
	        
	        if (!serverFilter.amf3MessageInterceptor().equals(AMF3MessageInterceptor.class)) {
	        	try {
	        		graniteConfig.setAmf3MessageInterceptor(TypeUtil.newInstance(serverFilter.amf3MessageInterceptor(), AMF3MessageInterceptor.class));
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
	    	String lookup = null;
	        if (useTide) {
		    	lookup = "java:global/{context.root}/{capitalized.component.name}Bean";
		    	if (isJBoss6())
		    		lookup = "{capitalized.component.name}Bean/local";
		    	if (!("".equals(serverFilter.ejbLookup())))
		    		lookup = serverFilter.ejbLookup();
	        }
	        else {
		    	lookup = "java:global/{context.root}/{capitalized.destination.id}Bean";
		    	if (isJBoss6())
		    		lookup = "{capitalized.destination.id}Bean/local";
		    	if (!("".equals(serverFilter.ejbLookup())))
		    		lookup = serverFilter.ejbLookup();
	        }
	    	if (lookup.indexOf("{context.root}") >= 0) {
	    		try {
	    			// Call by reflection because of JDK 1.4
	    			Method m = servletContext.getClass().getMethod("getContextPath");
	    			String contextPath = (String)m.invoke(servletContext);
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
		        	List<String> tideRoles = serverFilter.tideRoles().length == 0 ? null : Arrays.asList(serverFilter.tideRoles());
		        	Destination destination = new Destination(type, channelIds, new XMap(), tideRoles, null, null);
		        	destination.getProperties().put("factory", "tide-" + type + "-factory");
		        	if (!("".equals(serverFilter.entityManagerFactoryJndiName())))
		        		destination.getProperties().put("entity-manager-factory-jndi-name", serverFilter.entityManagerFactoryJndiName());
		        	else if (!("".equals(serverFilter.entityManagerJndiName())))
		        		destination.getProperties().put("entity-manager-jndi-name", serverFilter.entityManagerJndiName());
		        	if (!("".equals(serverFilter.validatorClassName())))
		        		destination.getProperties().put("validator-class-name", serverFilter.validatorClassName());
		        	service.getDestinations().put(type, destination);
		        	
		        	if (destination.getSecurizer() == null && configProvider != null) {
	                	RemotingDestinationSecurizer securizer = configProvider.findInstance(RemotingDestinationSecurizer.class);
	                	destination.setSecurizer(securizer);
		        	}
		        	
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
	    
	    private static void initSecurizer(AbstractMessagingDestination messagingDestination, Class<? extends GravityDestinationSecurizer> securizerClass, ConfigProvider configProvider) {
			if (securizerClass != GravityDestinationSecurizer.class) {
				if (configProvider != null)
					messagingDestination.setSecurizer(configProvider.findInstance(securizerClass));
				if (messagingDestination.getSecurizer() == null)
					messagingDestination.setSecurizerClassName(securizerClass.getName());
			}
	    }

		
		public void configureGravityServices(ServletContext servletContext) throws ServletException {
	        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
	        
	        ConfigProvider configProvider = (ConfigProvider)servletContext.getAttribute(GraniteConfigListener.GRANITE_CONFIG_PROVIDER_ATTRIBUTE);
	        
	        for (Field field : serverFilterClass.getDeclaredFields()) {
	        	if (field.isAnnotationPresent(MessagingDestination.class)) {
	        		MessagingDestination md = field.getAnnotation(MessagingDestination.class);
	        		AbstractMessagingDestination messagingDestination = new AbstractMessagingDestination();
	        		messagingDestination.setId(field.getName());
	        		messagingDestination.setNoLocal(md.noLocal());
	        		messagingDestination.setSessionSelector(md.sessionSelector());
	        		initSecurizer(messagingDestination, md.securizer(), configProvider);
	        		messagingDestination.initServices(servicesConfig);
	        	}
	        	else if (field.isAnnotationPresent(JmsTopicDestination.class)) {
	        		JmsTopicDestination md = field.getAnnotation(JmsTopicDestination.class);
	        		AbstractJmsTopicDestination messagingDestination = new AbstractJmsTopicDestination();
	        		messagingDestination.setId(field.getName());
	        		messagingDestination.setNoLocal(md.noLocal());
	        		messagingDestination.setSessionSelector(md.sessionSelector());
	        		initSecurizer(messagingDestination, md.securizer(), configProvider);
	        		messagingDestination.initServices(servicesConfig);
	        		messagingDestination.setName(md.name());
	        		messagingDestination.setTextMessages(md.textMessages());
	        		messagingDestination.setAcknowledgeMode(md.acknowledgeMode());
	        		messagingDestination.setConnectionFactory(md.connectionFactory());
	        		messagingDestination.setTransactedSessions(md.transactedSessions());
	        		messagingDestination.setJndiName(md.topicJndiName());
	        		messagingDestination.initServices(servicesConfig);
	        	}
	        	else if (field.isAnnotationPresent(ActiveMQTopicDestination.class)) {
	        		ActiveMQTopicDestination md = field.getAnnotation(ActiveMQTopicDestination.class);
	        		AbstractActiveMQTopicDestination messagingDestination = new AbstractActiveMQTopicDestination();
	        		messagingDestination.setId(field.getName());
	        		messagingDestination.setNoLocal(md.noLocal());
	        		messagingDestination.setSessionSelector(md.sessionSelector());
	        		initSecurizer(messagingDestination, md.securizer(), configProvider);
	        		messagingDestination.initServices(servicesConfig);
	        		messagingDestination.setName(md.name());
	        		messagingDestination.setTextMessages(md.textMessages());
	        		messagingDestination.setAcknowledgeMode(md.acknowledgeMode());
	        		messagingDestination.setConnectionFactory(md.connectionFactory());
	        		messagingDestination.setTransactedSessions(md.transactedSessions());
	        		messagingDestination.setJndiName(md.topicJndiName());
	        		messagingDestination.setBrokerUrl(md.brokerUrl());
	        		messagingDestination.setCreateBroker(md.createBroker());
	        		messagingDestination.setDurable(md.durable());
	        		messagingDestination.setWaitForStart(md.waitForStart());
	        		messagingDestination.setFileStoreRoot(md.fileStoreRoot());
	        		messagingDestination.initServices(servicesConfig);
	        	}
	        }
		}
	}
}
