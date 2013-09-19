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
package org.granite.util;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class ServletParams {

	private static final Logger log = Logger.getLogger(ServletParams.class);

	public static <T> T get(final ServletContext context, final String name, Class<T> clazz, T defaultValue) {
		return get(context, name, clazz, defaultValue, false, true);
	}
	public static <T> T get(final ServletConfig config, final String name, Class<T> clazz, T defaultValue) {
		return get(config, name, clazz, defaultValue, false, true);
	}
	public static <T> T get(final FilterConfig config, final String name, Class<T> clazz, T defaultValue) {
		return get(config, name, clazz, defaultValue, false, true);
	}

	public static <T> T get(final ServletContext context, final String name, Class<T> clazz, T defaultValue, boolean required, boolean warn) {
		ParamGetter getter = new ParamGetter() {
			
			public Enumeration<String> getNames() {
				return context.getInitParameterNames();
			}
			
			public String getName() {
				return name;
			}
			
			public String getValue() {
				return context.getInitParameter(name);
			}
		};
		return getInitParameter(getter, clazz, defaultValue, required, warn);
	}

	public static <T> T get(final ServletConfig config, final String name, Class<T> clazz, T defaultValue, boolean required, boolean warn) {
		ParamGetter getter = new ParamGetter() {
			
			public Enumeration<String> getNames() {
				return config.getInitParameterNames();
			}
			
			public String getName() {
				return name;
			}
			
			public String getValue() {
				return config.getInitParameter(name);
			}
		};
		return getInitParameter(getter, clazz, defaultValue, required, warn);
	}

	public static <T> T get(final FilterConfig config, final String name, Class<T> clazz, T defaultValue, boolean required, boolean warn) {
		ParamGetter getter = new ParamGetter() {
			
			public Enumeration<String> getNames() {
				return config.getInitParameterNames();
			}
			
			public String getName() {
				return name;
			}
			
			public String getValue() {
				return config.getInitParameter(name);
			}
		};
		return getInitParameter(getter, clazz, defaultValue, required, warn);
	}
	
	public static boolean contains(FilterConfig config, String name) {
		boolean found = false;
		Enumeration<String> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String n = e.nextElement();
			if (name.equals(n)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public static boolean contains(ServletConfig config, String name) {
		boolean found = false;
		Enumeration<String> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String n = e.nextElement();
			if (name.equals(n)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public static boolean contains(ServletContext context, String name) {
		boolean found = false;
		Enumeration<String> e = context.getInitParameterNames();
		while (e.hasMoreElements()) {
			String n = e.nextElement();
			if (name.equals(n)) {
				found = true;
				break;
			}
		}
		return found;
	}

	private static <T> T getInitParameter(ParamGetter getter, Class<T> clazz, T defaultValue, boolean required, boolean warn) {

    	if (required) {
    		boolean found = false;
    		Enumeration<String> e = getter.getNames();
    		while (e.hasMoreElements()) {
    			String name = e.nextElement();
    			if (name.equals(getter.getName())) {
    				found = true;
    				break;
    			}
    		}
    		if (!found)
    			throw new RuntimeException("Init parameter " + getter.getName() + " is required in web.xml");
    	}
    		
        String sValue = getter.getValue();
        Object oValue = defaultValue;
        
        boolean unsupported = false;
        if (sValue != null) {
	        try {
	        	if (clazz == String.class)
	        		oValue = sValue;
	        	else if (clazz == Integer.class || clazz == Integer.TYPE)
	        		oValue = Integer.valueOf(sValue);
	        	else if (clazz == Long.class || clazz == Long.TYPE)
	        		oValue = Long.valueOf(sValue);
	        	else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
	        		if (!Boolean.TRUE.toString().equalsIgnoreCase(sValue) && !Boolean.FALSE.toString().equalsIgnoreCase(sValue))
	        			throw new NumberFormatException(sValue);
	        		oValue = Boolean.valueOf(sValue);
	        	}
	        	else if (clazz == Double.class || clazz == Double.TYPE)
	        		oValue = Double.valueOf(sValue);
	        	else if (clazz == Float.class || clazz == Float.TYPE)
	        		oValue = Float.valueOf(sValue);
	        	else if (clazz == Short.class || clazz == Short.TYPE)
	        		oValue = Short.valueOf(sValue);
	        	else if (clazz == Byte.class || clazz == Byte.TYPE)
	        		oValue = Byte.valueOf(sValue);
	        	else
	        		unsupported = true; 
	        }
	        catch (Exception e) {
	        	if (warn)
	        		log.warn(e, "Illegal %s value for %s: %s (using default: %s)", clazz.getSimpleName(), getter.getName(), sValue, defaultValue);
	        }
        }
        
        if (unsupported)
        	throw new UnsupportedOperationException("Unsupported value type: " + clazz.getName());
        
        @SuppressWarnings("unchecked")
        T tValue = (T)oValue;
        
    	return tValue;
    }
	
	private static interface ParamGetter {
		
		public Enumeration<String> getNames();
		public String getName();
		public String getValue();
	}
}
