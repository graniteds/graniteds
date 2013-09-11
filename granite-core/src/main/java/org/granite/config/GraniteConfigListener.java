/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.granite.config.GraniteConfig.JMF_EXTENSIONS_MODE;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.jmx.GraniteMBeanInitializer;
import org.granite.logging.Logger;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.DefaultAliasRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.ExtendedObjectCodecService;
import org.granite.messaging.reflect.Reflection;
import org.granite.scan.ServiceLoader;
import org.granite.util.JMFAMFUtil;
import org.granite.util.ServletParams;


/**
 * @author William DRAI
 */
public class GraniteConfigListener implements ServletContextListener, HttpSessionListener {

    private static final String GRANITE_CONFIG_SHUTDOWN_KEY = GraniteConfig.class.getName() + "_SHUTDOWN";
    public static final String GRANITE_CONFIG_ATTRIBUTE = "org.granite.config.serverFilter";
    public static final String GRANITE_CONFIG_PROVIDER_ATTRIBUTE = "org.granite.config.configProvider";
    public static final String GRANITE_MBEANS_ATTRIBUTE = "registerGraniteMBeans";
    
    public static final String GRANITE_SESSION_TRACKING = "org.granite.config.sessionTracking";
    public static final String GRANITE_SESSION_MAP = "org.granite.config.sessionMap";
    
    public static final String JMF_INITIALIZATION = "jmf-initialization";
	public static final String SHARED_CONTEXT_KEY = SharedContext.class.getName();
	public static final String DUMP_SHARED_CONTEXT_KEY = SharedContext.class.getName() + ":DUMP";

    private static final Logger log = Logger.getLogger(GraniteConfigListener.class);

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        try {
            log.info("Initializing GraniteDS...");
            
            ServiceConfigurator serviceConfigurator = (ServiceConfigurator)context.getAttribute(GRANITE_CONFIG_ATTRIBUTE);
            if (serviceConfigurator != null)
            	serviceConfigurator.initialize(context);
            
            GraniteConfig gConfig = ServletGraniteConfig.loadConfig(context);
            ServletServicesConfig.loadConfig(context);
            
            if (serviceConfigurator != null)
            	serviceConfigurator.configureServices(context);
            
            if ("true".equals(context.getInitParameter(GRANITE_SESSION_TRACKING))) {
	            Map<String, HttpSession> sessionMap = new ConcurrentHashMap<String, HttpSession>(200);
	            context.setAttribute(GRANITE_SESSION_MAP, sessionMap);
            }
            
            if (gConfig.isRegisterMBeans()) {
            	GraniteMBeanInitializer.registerMBeans(context, 
            			ServletGraniteConfig.getServletConfig(context), 
            			ServletServicesConfig.getServletConfig(context));
            }
            
            String jmfInitialization = context.getInitParameter(JMF_INITIALIZATION);
            if (jmfInitialization == null || "true".equals(jmfInitialization))
            	loadJMFSharedContext(context, gConfig);

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
    
    
    public static interface ServiceConfigurator {
    	
    	public void initialize(ServletContext context);
    	
    	public void configureServices(ServletContext context) throws ServletException;
    }
    
	
	private static void loadJMFSharedContext(ServletContext servletContext, GraniteConfig graniteConfig) {
		log.info("Loading JMF shared context");

		List<ExtendedObjectCodec> extendedObjectCodecs = null;
		
		if (graniteConfig.getJmfExtendedCodecsMode() == JMF_EXTENSIONS_MODE.REPLACE)
			extendedObjectCodecs = graniteConfig.getJmfExtendedCodecs();
		else {
			extendedObjectCodecs = new ArrayList<ExtendedObjectCodec>();
			
			if (graniteConfig.getJmfExtendedCodecsMode() == JMF_EXTENSIONS_MODE.PREPPEND)
				extendedObjectCodecs.addAll(graniteConfig.getJmfExtendedCodecs());
			
			for (ExtendedObjectCodecService service : ServiceLoader.load(ExtendedObjectCodecService.class))
				extendedObjectCodecs.addAll(Arrays.asList(service.getExtensions()));
			
			if (graniteConfig.getJmfExtendedCodecsMode() == JMF_EXTENSIONS_MODE.APPEND)
				extendedObjectCodecs.addAll(graniteConfig.getJmfExtendedCodecs());
		}
		
		log.debug("Using JMF extended codecs: %s", extendedObjectCodecs);
		
		List<String> defaultStoredStrings = null;
		if (graniteConfig.getJmfDefaultStoredStringsMode() == JMF_EXTENSIONS_MODE.REPLACE)
			defaultStoredStrings = graniteConfig.getJmfDefaultStoredStrings();
		else {
			defaultStoredStrings = new ArrayList<String>();
			
			if (graniteConfig.getJmfDefaultStoredStringsMode() == JMF_EXTENSIONS_MODE.PREPPEND)
				defaultStoredStrings.addAll(graniteConfig.getJmfDefaultStoredStrings());
			
			defaultStoredStrings.addAll(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS);
			
			if (graniteConfig.getJmfDefaultStoredStringsMode() == JMF_EXTENSIONS_MODE.APPEND)
				defaultStoredStrings.addAll(graniteConfig.getJmfDefaultStoredStrings());
		}
		
		log.debug("Using JMF default stored strings: %s", defaultStoredStrings);
		
		Reflection reflection = graniteConfig.getJmfReflection();
		
		log.debug("Using JMF reflection: %s", reflection.getClass().getName());
		
		AliasRegistry aliasRegistry = new DefaultAliasRegistry();
        
		SharedContext sharedContext = new DefaultSharedContext(new DefaultCodecRegistry(extendedObjectCodecs), defaultStoredStrings, reflection, aliasRegistry);
        servletContext.setAttribute(SHARED_CONTEXT_KEY, sharedContext);
        
        SharedContext dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(), defaultStoredStrings, reflection, aliasRegistry);
        servletContext.setAttribute(DUMP_SHARED_CONTEXT_KEY, dumpSharedContext);
		
        log.info("JMF shared context loaded");
	}
	
	public static SharedContext getSharedContext(ServletContext servletContext) {
		return (SharedContext)servletContext.getAttribute(SHARED_CONTEXT_KEY);
	}

	public static SharedContext getDumpSharedContext(ServletContext servletContext) {
		return (SharedContext)servletContext.getAttribute(DUMP_SHARED_CONTEXT_KEY);
	}
	
	public static ServletException newSharedContextNotInitializedException() {
		return new ServletException(
			"JMF shared context not initialized (remove or set to true '" + JMF_INITIALIZATION + "' param in your web.xml)"
		);
	}
	
	public void sessionCreated(HttpSessionEvent se) {
		@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionMap = (Map<String, HttpSession>)se.getSession().getServletContext().getAttribute(GRANITE_SESSION_MAP);
		if (sessionMap != null)
			sessionMap.put(se.getSession().getId(), se.getSession());
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionMap = (Map<String, HttpSession>)se.getSession().getServletContext().getAttribute(GRANITE_SESSION_MAP);
		if (sessionMap != null)
			sessionMap.remove(se.getSession().getId());
	}
}
