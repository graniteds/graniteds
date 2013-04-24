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

import org.granite.config.GraniteConfigListener;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.util.JMFAMFUtil;

/**
 * @author Franck WOLFF
 */
public class JMFServletContextListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger(GraniteConfigListener.class);

	public static final String EXTENDED_OBJECT_CODECS = "extended-object-codecs";
	public static final String DEFAULT_STORED_STRINGS = "default-stored-strings";
	public static final String ATTRIBUTE_KEY = SharedContext.class.getName();

	public void contextInitialized(ServletContextEvent event) {
		log.info("Loading JMF shared context");
		
        ServletContext servletContext = event.getServletContext();
        
        List<ExtendedObjectCodec> extendedObjectCodecs = loadExtendedObjectCodec(servletContext);
        CodecRegistry codecRegistry = new DefaultCodecRegistry(extendedObjectCodecs);
        
        List<String> defaultStoredStrings = loadDefaultStoredStrings(servletContext);
        SharedContext sharedContext = new DefaultSharedContext(codecRegistry, defaultStoredStrings);
        
        servletContext.setAttribute(ATTRIBUTE_KEY, sharedContext);
		
        log.info("JMF shared context loaded");
	}
	
	protected List<ExtendedObjectCodec> loadExtendedObjectCodec(ServletContext servletContext) {
        String extendedObjectCodecsParam = servletContext.getInitParameter(EXTENDED_OBJECT_CODECS);
		
        if (extendedObjectCodecsParam == null)
			return Collections.emptyList();
		
		ClassLoader classLoader = getClass().getClassLoader();
        
		List<ExtendedObjectCodec> extendedObjectCodecs = new ArrayList<ExtendedObjectCodec>();
        for (String className : extendedObjectCodecsParam.trim().split("\\s*\\,\\s*")) {
        	if (className.length() == 0)
        		continue;
        	
        	log.info("Loading JMF extended object codec: " + className);
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
	
	protected List<String> loadDefaultStoredStrings(ServletContext servletContext) {
        List<String> defaultStoredStrings = new ArrayList<String>(JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS);
		
        String defaultStoredStringsParam = servletContext.getInitParameter(DEFAULT_STORED_STRINGS);
		
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
        
        servletContext.removeAttribute(ATTRIBUTE_KEY);
	}
}
