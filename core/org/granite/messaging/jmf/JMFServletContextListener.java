/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.jmf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.granite.config.GraniteConfigListener;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.util.JMFAMFUtil;

/**
 * @author Franck WOLFF
 */
public class JMFServletContextListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(GraniteConfigListener.class);
    
    private static final String[] CODEC_EXTENSION_FACTORY_NAMES = {
    	"org.granite.hibernate.jmf.Hibernate3CodecExtensionFactory",
    	"org.granite.hibernate4.jmf.Hibernate4CodecExtensionFactory"
    };

	public static final String EXTENDED_OBJECT_CODECS_PARAM = "jmf-extended-object-codecs";
	public static final String DEFAULT_STORED_STRINGS_PARAM = "jmf-default-stored-strings";
	
	public static final String SHARED_CONTEXT_KEY = SharedContext.class.getName();
	public static final String DUMP_SHARED_CONTEXT_KEY = SharedContext.class.getName() + ":DUMP";

	public static SharedContext getSharedContext(ServletContext servletContext) {
		return (SharedContext)servletContext.getAttribute(SHARED_CONTEXT_KEY);
	}

	public static SharedContext getDumpSharedContext(ServletContext servletContext) {
		return (SharedContext)servletContext.getAttribute(DUMP_SHARED_CONTEXT_KEY);
	}
	
	public static ServletException newSharedContextNotInitializedException() {
		return new ServletException(
			"JMF shared context not initialized (configure " + JMFServletContextListener.class.getName() + " in your web.xml)"
		);
	}
	
	public void contextInitialized(ServletContextEvent event) {
		log.info("Loading JMF shared context");
		
        ServletContext servletContext = event.getServletContext();
        
        List<ExtendedObjectCodec> extendedObjectCodecs = loadExtendedObjectCodecs(servletContext);
        List<String> defaultStoredStrings = loadDefaultStoredStrings(servletContext);
        
        SharedContext sharedContext = new DefaultSharedContext(new DefaultCodecRegistry(extendedObjectCodecs), defaultStoredStrings, null);
        servletContext.setAttribute(SHARED_CONTEXT_KEY, sharedContext);
        
        SharedContext dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(), defaultStoredStrings, null);
        servletContext.setAttribute(DUMP_SHARED_CONTEXT_KEY, dumpSharedContext);
		
        log.info("JMF shared context loaded");
	}
	
	protected List<ExtendedObjectCodec> loadExtendedObjectCodecs(ServletContext servletContext) {
        String extendedObjectCodecsParam = servletContext.getInitParameter(EXTENDED_OBJECT_CODECS_PARAM);
		
        if (extendedObjectCodecsParam == null)
			return detectExtendedObjectCodecs();
		
		ClassLoader classLoader = getClass().getClassLoader();
        
		List<ExtendedObjectCodec> extendedObjectCodecs = new ArrayList<ExtendedObjectCodec>();
        for (String className : extendedObjectCodecsParam.trim().split("\\s*\\,\\s*")) {
        	if (className.length() == 0)
        		continue;
        	
        	log.info("Loading JMF extended object codec: %s", className);
        	try {
	        	@SuppressWarnings("unchecked")
				Class<? extends ExtendedObjectCodec> cls = (Class<? extends ExtendedObjectCodec>)classLoader.loadClass(className);
	        	extendedObjectCodecs.add(cls.getDeclaredConstructor().newInstance());
        	}
        	catch (Throwable t) {
        		log.warn(t, "Could not load JMF codec: %s", className);
        	}
        }
		return extendedObjectCodecs;
	}
	
	protected List<ExtendedObjectCodec> detectExtendedObjectCodecs() {
    	log.info("Auto detecting extended object codec...");
		
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String factoryName : CODEC_EXTENSION_FACTORY_NAMES) {
			try {
				CodecExtensionFactory factory = (CodecExtensionFactory)classLoader.loadClass(factoryName).newInstance();
				List<ExtendedObjectCodec> extendedObjectCodecs = factory.getCodecs();
				log.info("Using %s: %s", factoryName, extendedObjectCodecs);
				return extendedObjectCodecs;
			}
			catch (Throwable t) {
				log.debug(t, "Could not load factory: %s", factoryName);
			}
		}
		
		log.info("No extended object codec detected");
		return Collections.emptyList();
	}
	
	protected List<String> loadDefaultStoredStrings(ServletContext servletContext) {
        List<String> defaultStoredStrings = new ArrayList<String>(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS);
		
        String defaultStoredStringsParam = servletContext.getInitParameter(DEFAULT_STORED_STRINGS_PARAM);
		
        if (defaultStoredStringsParam != null) {
        	for (String value : defaultStoredStringsParam.trim().split("\\s*\\,\\s*")) {
            	if (value.length() > 0)
            		defaultStoredStrings.add(value);
        	}
        }
        
        return defaultStoredStrings;
	}
	

	public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        
        servletContext.removeAttribute(SHARED_CONTEXT_KEY);
	}
}
