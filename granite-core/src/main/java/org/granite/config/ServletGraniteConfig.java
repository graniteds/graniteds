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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.granite.config.api.Configuration;
import org.granite.jmx.MBeanUtil;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.util.ServletParams;
import org.granite.util.StreamUtil;

/**
 * @author Franck WOLFF
 */
public class ServletGraniteConfig implements ServletGraniteConfigMBean {

    ///////////////////////////////////////////////////////////////////////////
    // Static fields.

    private static final Logger log = Logger.getLogger(ServletGraniteConfig.class);

    private static final String GRANITE_CONFIG_DEFAULT = "/WEB-INF/granite/granite-config.xml";
    
    public static final String GRANITE_CONFIG_KEY = GraniteConfig.class.getName() + "_CACHE";
    public static final String GRANITE_CONFIG_DEFAULT_KEY = GraniteConfig.class.getName() + "_DEFAULT";
    public static final String GRANITE_CONFIG_CONFIGURATION_KEY = GraniteConfig.class.getName() + "_CONFIG";

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.

    private GraniteConfig config = null;
    
    // Context where this GraniteConfig instance is registered (required for reload operation).
    private ServletContext context = null;
    
    // Should Granite MBeans be registered at startup.
    private boolean registerMBeans = false;
    
    // Reload listeners.
    private final List<GraniteConfigReloadListener> reloadListeners = new ArrayList<GraniteConfigReloadListener>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    private ServletGraniteConfig(ServletContext context, GraniteConfig config) {
    	this.context = context;
    	this.config = config;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static GraniteConfig loaders.

    public static synchronized GraniteConfig getConfig(ServletContext context) {
    	return ((ServletGraniteConfig)context.getAttribute(GRANITE_CONFIG_KEY)).config;
    }

    public static synchronized ServletGraniteConfig getServletConfig(ServletContext context) {
    	return (ServletGraniteConfig)context.getAttribute(GRANITE_CONFIG_KEY);
    }

    public static synchronized GraniteConfig loadConfig(ServletContext context) throws ServletException {
        ServletGraniteConfig servletGraniteConfig = (ServletGraniteConfig)context.getAttribute(GRANITE_CONFIG_KEY);

        if (servletGraniteConfig == null) {
        	String path = getCustomConfigPath(context);

            InputStream is = context.getResourceAsStream(path);
            if (is == null) {
                log.warn("Could not load custom granite-config.xml: %s (file does not exists)", path);
                path = null;
            }
        	
        	Configuration configuration = (Configuration)context.getAttribute(GRANITE_CONFIG_CONFIGURATION_KEY);
        	
        	String stdConfigPath = (String)context.getAttribute(GRANITE_CONFIG_DEFAULT_KEY);

            boolean registerMBeans = ServletParams.get(context, GraniteConfigListener.GRANITE_MBEANS_ATTRIBUTE, Boolean.TYPE, false);
            
            try {
            	String mbeanContextName = null;
            	// Use reflection for JDK 1.4 compatibility. 
            	if (registerMBeans)
            		mbeanContextName = (String)ServletContext.class.getMethod("getContextPath").invoke(context);

                GraniteConfig graniteConfig = new GraniteConfig(stdConfigPath, is, configuration, mbeanContextName);
                
                servletGraniteConfig = loadConfig(context, graniteConfig);
            } 
            catch (Exception e) {
                log.error(e, "Could not load granite-config.xml");
                throw new ServletException("Could not load custom granite-config.xml", e);
            }
            finally {
            	if (is != null) try {
            		is.close();
            	} catch (IOException e) {
            		// Ignore...
            	}
            }
        }

        return servletGraniteConfig.config;
    }

    public static synchronized ServletGraniteConfig loadConfig(ServletContext context, GraniteConfig graniteConfig) {
        ServletGraniteConfig servletGraniteConfig = new ServletGraniteConfig(context, graniteConfig);
        
        context.setAttribute(GRANITE_CONFIG_KEY, servletGraniteConfig);
        
        return servletGraniteConfig;
    }

    private static String getCustomConfigPath(ServletContext context) {
    	String path = null;
    	
    	Configuration configuration = (Configuration)context.getAttribute(GRANITE_CONFIG_CONFIGURATION_KEY);
    	if (configuration != null)
    		path = configuration.getGraniteConfig();
    	
        if (path == null)
        	path = ServletParams.get(context, "graniteConfigPath", String.class, GRANITE_CONFIG_DEFAULT);
        
        return path;
	}
    
    public String getCustomConfigPath() {
    	return getCustomConfigPath(context);
    }
    

    ///////////////////////////////////////////////////////////////////////////
    // GraniteConfigMBean implementation: attributes.
    
	public boolean isRegisterMBeans() {
		return registerMBeans;
	}
	public void setRegisterMBeans(boolean registerMBeans) {
		this.registerMBeans = registerMBeans;
	}

	
	public synchronized void reload() {

		if (context == null)
    		throw new IllegalStateException("GraniteConfig was not registered in ServletContext");

    	ServletGraniteConfig oldConfig = (ServletGraniteConfig)context.getAttribute(GRANITE_CONFIG_KEY);
		
		try {
	    	context.removeAttribute(GRANITE_CONFIG_KEY);
	    	GraniteConfig config = loadConfig(context);
	    	for (GraniteConfigReloadListener listener : reloadListeners) {
	    		try {
	    			listener.onReload(context, config);
	    		}
	    		catch (Exception e) {
	    			log.error(e, "Error while calling reload listener: %s", listener);
	    		}
	    	}
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		finally {
			if (context.getAttribute(GRANITE_CONFIG_KEY) == null)
				context.setAttribute(GRANITE_CONFIG_KEY, oldConfig);
		}
	}


    ///////////////////////////////////////////////////////////////////////////
    // GraniteConfigMBean implementation
	
	public void addReloadListener(GraniteConfigReloadListener listener) {
		synchronized (reloadListeners) {
			if (!reloadListeners.contains(listener))
				reloadListeners.add(listener);
		}
	}
	
	public boolean removeReloadListener(GraniteConfigReloadListener listener) {
		synchronized (reloadListeners) {
			return reloadListeners.remove(listener);
		}
	}
    
    public boolean getScan() {
        return config.getScan();
    }

	public String getAmf3DeserializerClass() {
		return MBeanUtil.format(
			config.getAmf3DeserializerConstructor() != null ?
			config.getAmf3DeserializerConstructor().getDeclaringClass().getName():
			null
		);
	}

	public String getAmf3SerializerClass() {
		return MBeanUtil.format(
			config.getAmf3SerializerConstructor() != null ?
			config.getAmf3SerializerConstructor().getDeclaringClass().getName():
			null
		);
	}

	public String getAmf3MessageInterceptorClass() {
		return MBeanUtil.format(
			config.getAmf3MessageInterceptor() != null ?
			config.getAmf3MessageInterceptor().getClass().getName() :
			null
		);
	}

	public String getClassGetterClass() {
		return MBeanUtil.format(
			config.getClassGetter() != null ?
			config.getClassGetter().getClass().getName() :
			null
		);
	}

	public String getMessageSelectorClass() {
		return MBeanUtil.format(
			config.getMessageSelectorConstructor() != null ?
			config.getMessageSelectorConstructor().getDeclaringClass().getName() :
			null
		);
	}

	public String getMethodMatcherClass() {
		return MBeanUtil.format(
			config.getMethodMatcher() != null ?
			config.getMethodMatcher().getClass().getName() :
			null
		);
	}

	public String getSecurityServiceClass() {
		return MBeanUtil.format(
			config.getSecurityService() != null ?
			config.getSecurityService().getClass().getName() :
			null
		);
	}

	public String getServiceInvocationListenerClass() {
		return MBeanUtil.format(
			config.getInvocationListener() != null ?
			config.getInvocationListener().getClass().getName() :
			null
		);
	}

	public String showStandardConfig() throws IOException {
		String s = StreamUtil.getResourceAsString("org/granite/config/granite-config.xml", getClass().getClassLoader());
		return MBeanUtil.format(s);
	}
	
    public String showCustomConfig() throws IOException {
    	String path = getCustomConfigPath();

        InputStream is = context.getResourceAsStream(path);
        try {
        	String s = StreamUtil.getStreamAsString(is);
    		return MBeanUtil.format(s);
        }
        finally {
        	if (is != null)
        		is.close();
        }
	}

	public String showConverters() {
		Converter[] cs = config.getConverters().getConverters();
		String[] names = new String[cs.length];
		for (int i = 0; i < cs.length; i++)
			names[i] = cs[i].getClass().getName();
		return MBeanUtil.format(names);
	}

	public String showExceptionConverters() {
		String[] names = new String[config.getExceptionConverters().size()];
		for (int i = 0; i < config.getExceptionConverters().size(); i++)
			names[i] = config.getExceptionConverters().get(i).getClass().getName();
		return MBeanUtil.format(names);
	}

	public String showInstantiators() {
		String[] names = new String[config.getInstantiators().size()];
		int i = 0;
		for (Map.Entry<String, String> e : config.getInstantiators().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue();
		return MBeanUtil.format(names, true);
	}

	public String showAs3DescriptorsByInstanceOf() {
		String[] names = new String[config.getAs3DescriptorsByInstanceOf().size()];
		int i = 0;
		for (Map.Entry<String, String> e : config.getAs3DescriptorsByInstanceOf().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue();
		return MBeanUtil.format(names, true);
	}

	public String showAs3DescriptorsByType() {
		String[] names = new String[config.getAs3DescriptorsByType().size()];
		int i = 0;
		for (Map.Entry<String, Class<? extends ActionScriptClassDescriptor>> e : config.getAs3DescriptorsByType().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue().getName();
		return MBeanUtil.format(names, true);
	}

	public String showDisabledTideComponentsByName() {
		String[] names = new String[config.getDisabledTideComponentsByName().size()];
		int i = 0;
		for (Map.Entry<String, Object[]> e : config.getDisabledTideComponentsByName().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue()[0];
		return MBeanUtil.format(names, true);
	}

	public String showEnabledTideComponentsByName() {
		String[] names = new String[config.getEnabledTideComponentsByName().size()];
		int i = 0;
		for (Map.Entry<String, Object[]> e : config.getEnabledTideComponentsByName().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue()[0];
		return MBeanUtil.format(names, true);
	}

	public String showExternalizersByAnnotatedWith() {
		String[] names = new String[config.getExternalizersByAnnotatedWith().size()];
		int i = 0;
		for (Map.Entry<String, String> e : config.getExternalizersByAnnotatedWith().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue();
		return MBeanUtil.format(names, true);
	}

	public String showExternalizersByInstanceOf() {
		String[] names = new String[config.getExternalizersByInstanceOf().size()];
		int i = 0;
		for (Map.Entry<String, String> e : config.getExternalizersByInstanceOf().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue();
		return MBeanUtil.format(names, true);
	}

	public String showExternalizersByType() {
		List<String> names = new ArrayList<String>(config.getExternalizersByType().size());
		for (Map.Entry<String, Externalizer> e : config.getExternalizersByType().entrySet()) {
			if (config.EXTERNALIZER_FACTORY.getNullInstance() != e.getValue())
				names.add(e.getKey() + "=" + e.getValue().getClass().getName());
		}
		return MBeanUtil.format(names.toArray(new String[names.size()]), true);
	}

	public String showJavaDescriptorsByInstanceOf() {
		String[] names = new String[config.getJavaDescriptorsByInstanceOf().size()];
		int i = 0;
		for (Map.Entry<String, String> e : config.getJavaDescriptorsByInstanceOf().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue();
		return MBeanUtil.format(names, true);
	}

	public String showJavaDescriptorsByType() {
		String[] names = new String[config.getJavaDescriptorsByType().size()];
		int i = 0;
		for (Map.Entry<String, Class<? extends JavaClassDescriptor>> e : config.getJavaDescriptorsByType().entrySet())
			names[i++] = e.getKey() + "=" + e.getValue().getName();
		return MBeanUtil.format(names, true);
	}

	public String showScannedExternalizers() {
		String[] names = new String[config.getScannedExternalizers().size()];
		for (int i = 0; i < config.getScannedExternalizers().size(); i++)
			names[i] = config.getScannedExternalizers().get(i).getClass().getName();
		return MBeanUtil.format(names);
	}

	public String showTideComponentMatchers() {
		String[] names = new String[config.getTideComponentMatchers().size()];
		for (int i = 0; i < config.getTideComponentMatchers().size(); i++)
			names[i] = config.getTideComponentMatchers().get(i).toString();
		return MBeanUtil.format(names);
	}
}
