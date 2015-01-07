/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class ServiceLoader<S> implements Iterable<S> {
	
	// Can't use granite logger here, because service loader can be used to load a specific
	// logger implementation (stack overflow...)
	private static final Logger jdkLog = Logger.getLogger(ServiceLoader.class.getName());
	
	private final Class<S> service;
	private final ClassLoader loader;
	
	private List<String> serviceClassNames;
	
	private Class<?>[] constructorParameterTypes = new Class<?>[0];
	private Object[] constructorParameters = new Object[0];
	
	private ServiceLoader(Class<S> service, ClassLoader loader, List<String> servicesNames) {
		this.service = service;
		this.loader = loader;
		this.serviceClassNames = servicesNames;
	}
	
	public void setConstructorParameters(Class<?>[] constructorParameterTypes, Object[] constructorParameters) {		
		
		if (constructorParameterTypes == null)
			constructorParameterTypes = new Class<?>[0];
		if (constructorParameters == null)
			constructorParameters = new Object[0];
		
		if (constructorParameterTypes.length != constructorParameters.length)
			throw new IllegalArgumentException("constructor types and argurments must have the same size");

		this.constructorParameterTypes = constructorParameterTypes;
		this.constructorParameters = constructorParameters;
	}

	public ServicesIterator<S> iterator() {
		return new ServicesIterator<S>(loader, serviceClassNames.iterator(), constructorParameterTypes, constructorParameters);
	}
	
	public void reload() {
		ServiceLoader<S> serviceLoaderTmp = load(service, loader);
		this.serviceClassNames = serviceLoaderTmp.serviceClassNames;
	}
	
	public static <S> ServiceLoader<S> load(Class<S> service) {
		return load(service, Thread.currentThread().getContextClassLoader());
	}

	public static <S> ServiceLoader<S> load(final Class<S> service, final ClassLoader loader) {
		List<String> serviceClassNames = new ArrayList<String>();
		
		try {
			// Standard Java platforms.
			Enumeration<URL> en = loader.getResources("META-INF/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			// Android support (META-INF/** files are not included in APK files).
			en = loader.getResources("meta_inf/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			return new ServiceLoader<S>(service, loader, serviceClassNames);
		}
		catch (Exception e) {
			jdkLog.log(Level.SEVERE, "Could not load services of type " + service, e);
			throw new RuntimeException(e);
		}
	}
	
	private static void parse(URL serviceFile, List<String> serviceClassNames) {
		try {
		    InputStream is = serviceFile.openStream();
		    try {
		    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	
		    	String serviceClassName = null;
		    	while ((serviceClassName = reader.readLine()) != null) {
		    		int comment = serviceClassName.indexOf('#');
		    		if (comment >= 0)
		    			serviceClassName = serviceClassName.substring(0, comment);
			        serviceClassName = serviceClassName.trim();
			        if (serviceClassName.length() > 0) {
			        	jdkLog.log(Level.FINE, "Adding service " + serviceClassName + " from " + serviceFile);
			        	serviceClassNames.add(serviceClassName);
			        }
		    	}
		    }
		    finally {
		    	is.close();
		    }
		}
		catch (Exception e) {
			jdkLog.log(Level.SEVERE, "Could not parse service file " + serviceFile, e);
		}
	}
	
	public static class ServicesIterator<S> implements Iterator<S> {
		
		private final ClassLoader loader;
		private final Iterator<String> serviceClassNames;
		private final Class<?>[] constructorParameterTypes;
		private final Object[] constructorParameters;

		private ServicesIterator(ClassLoader loader, Iterator<String> servicesNames, Class<?>[] constructorParameterTypes, Object[] constructorParameters) {
			this.loader = loader;
			this.serviceClassNames = servicesNames;
			this.constructorParameterTypes = constructorParameterTypes;
			this.constructorParameters = constructorParameters;
		}

		public boolean hasNext() {
			return serviceClassNames.hasNext();
		}

		public S next() {
			String serviceClassName = serviceClassNames.next();
			jdkLog.log(Level.FINE, "Loading service " + serviceClassName);
			try {
				@SuppressWarnings("unchecked")
				Class<? extends S> serviceClass = (Class<? extends S>)loader.loadClass(serviceClassName);
				return serviceClass.getConstructor(constructorParameterTypes).newInstance(constructorParameters);
			}
			catch (Throwable t) {
				jdkLog.log(Level.SEVERE, "Could not load service " + serviceClassName, t);
				throw new RuntimeException(t);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
}
