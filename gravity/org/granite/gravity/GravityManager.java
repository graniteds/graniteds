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

package org.granite.gravity;

import java.lang.reflect.Field;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.granite.config.GraniteConfig;
import org.granite.config.GraniteConfigListener;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.gravity.config.AbstractActiveMQTopicDestination;
import org.granite.gravity.config.AbstractJmsTopicDestination;
import org.granite.gravity.config.AbstractMessagingDestination;
import org.granite.gravity.config.servlet3.ActiveMQTopicDestination;
import org.granite.gravity.config.servlet3.JmsTopicDestination;
import org.granite.gravity.config.servlet3.MessagingDestination;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class GravityManager {

	private static final String GRAVITY_KEY = Gravity.class.getName();
	
	/**
	 * Parse gravity configuration (granite-config.xml), start gravity by using the specified factory and put it
	 * in ServletContext. If Gravity is already started, returns the previous instance from the servlet context.
	 * <br><br>
	 * This method is intended to be used in {@link HttpServlet#init(ServletConfig)} methods only and
	 * synchronizes on the current ServletContext instance.
	 * 
	 * @param servletConfig the servlet config passed in HttpServlet.init(ServletConfig config) method.
	 * @param channelFactory an implementation specific ChannelFactory instance.
	 * @return a newly created and started Gravity instance or previously started one.
	 * @throws ServletException if something goes wrong (GravityFactory not found, Gravity.start() error, etc.)
	 */
    public static Gravity start(ServletConfig servletConfig) throws ServletException {
    	Gravity gravity = null;
    	
    	ServletContext context = servletConfig.getServletContext();
    	synchronized (context) {
	    	
    		gravity = (Gravity)context.getAttribute(GRAVITY_KEY);
	    	
    		if (gravity == null) {
		        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(context);
	    		ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(context);
	            
	            Class<?> flexFilterClass = (Class<?>)context.getAttribute(GraniteConfigListener.GRANITE_CONFIG_ATTRIBUTE);
	            if (flexFilterClass != null)
	            	configureServices(context, flexFilterClass);
		
		        GravityConfig gravityConfig = new GravityConfig(graniteConfig);
		        
		        String gravityFactory = gravityConfig.getGravityFactory();
		        try {
					GravityFactory factory = TypeUtil.newInstance(gravityFactory, GravityFactory.class);
					gravity = factory.newGravity(gravityConfig, servicesConfig, graniteConfig);
				} catch (Exception e) {
					throw new ServletException("Could not create Gravity instance with factory: " + gravityFactory, e);
				}
		
		        try {
		            gravity.start();
		            context.setAttribute(GRAVITY_KEY, gravity);
		            GraniteConfigListener.registerShutdownListener(context, gravity);
		        }
		        catch (Exception e) {
		            throw new ServletException("Gravity initialization error", e);
		        }
	    	}
    	}

        return gravity;
    }
    
    /**
     * Reconfigure gravity with the new supplied configuration (after reloading granite-config.xml).
     * <br><br>
     * Only these configuration options are taken into account when reconfiguring Gravity:
     * <ul>
     * 	<li>channelIdleTimeoutMillis</li>
     * 	<li>longPollingTimeout</li>
     * 	<li>retryOnError</li>
     * 	<li>maxMessagesQueuedPerChannel</li>
     * 	<li>corePoolSize</li>
     * 	<li>maximumPoolSize</li>
     * 	<li>keepAliveTimeMillis</li>
     * </ul>
     * 
     * @param context the ServletContext where the gravity instance is registered.
     * @param gravityConfig the new (reloaded) GravityConfig. 
     */
    public static void reconfigure(ServletContext context, GravityConfig gravityConfig) {
    	synchronized (context) {
	    	Gravity gravity = getGravity(context);
	    	gravity.reconfigure(gravityConfig, ServletGraniteConfig.getConfig(context));
    	}
    }
    
    /**
     * Returns a previously started Gravity instance. This method isn't synchronized and should be used in
     * HttpServlet.doPost(...) methods only.
     * 
     * @param context the ServletContext from which to retrieve the Gravity instance. 
     * @return the unique and started Gravity instance (or null if {@link #start(ServletConfig, ChannelFactory)}
     * 		has never been called).
     */
    public static Gravity getGravity(ServletContext context) {
    	return (Gravity)context.getAttribute(GRAVITY_KEY);
    }
    
    
    private static void configureServices(ServletContext context, Class<?> flexFilterClass) throws ServletException {
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(context);
    	
        for (Field field : flexFilterClass.getDeclaredFields()) {
        	if (field.isAnnotationPresent(MessagingDestination.class)) {
        		MessagingDestination md = field.getAnnotation(MessagingDestination.class);
        		AbstractMessagingDestination messagingDestination = new AbstractMessagingDestination();
        		messagingDestination.setId(field.getName());
        		messagingDestination.setNoLocal(md.noLocal());
        		messagingDestination.setSessionSelector(md.sessionSelector());
        		messagingDestination.initServices(servicesConfig);
        	}
        	else if (field.isAnnotationPresent(JmsTopicDestination.class)) {
        		JmsTopicDestination md = field.getAnnotation(JmsTopicDestination.class);
        		AbstractJmsTopicDestination messagingDestination = new AbstractJmsTopicDestination();
        		messagingDestination.setId(field.getName());
        		messagingDestination.setNoLocal(md.noLocal());
        		messagingDestination.setSessionSelector(md.sessionSelector());
        		messagingDestination.setName(md.name());
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
        		messagingDestination.setName(md.name());
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
