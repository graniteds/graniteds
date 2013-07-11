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

package org.granite.scan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public class ServiceLoader {

	private static final Method serviceLoaderLoad;
	static {
		Method serviceLoaderLoadTmp;
		try {
			Class<?> serviceLoaderClass = Thread.currentThread().getContextClassLoader().loadClass("java.util.ServiceLoader");
			serviceLoaderLoadTmp = serviceLoaderClass.getDeclaredMethod("load", Class.class, ClassLoader.class);
		}
		catch (Throwable t) {
			serviceLoaderLoadTmp = null;
		}
		serviceLoaderLoad = serviceLoaderLoadTmp;
	}
	
	public static <S> Iterable<S> load(Class<S> service) {
		return load(service, Thread.currentThread().getContextClassLoader());
	}

	@SuppressWarnings("unchecked")
	public static <S> Iterable<S> load(final Class<S> service, final ClassLoader loader) {
		
		if (serviceLoaderLoad != null) {
			try {
				return (Iterable<S>)serviceLoaderLoad.invoke(null, service, loader);
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		try {
			Enumeration<URL> e = loader.getResources("META-INF/services/" + service.getName());
			
			final List<String> serviceClassNames = new ArrayList<String>();
			
			while (e.hasMoreElements()) {
			    URL url = e.nextElement();
			    
			    InputStream is = url.openStream();
			    try {
			    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			    	String name = null;
			    	while ((name = reader.readLine()) != null) {
			    		int comment = name.indexOf('#');
			    		if (comment >= 0)
			    			name = name.substring(0, comment);
				        name = name.trim();
				        if (name.length() > 0)
				        	serviceClassNames.add(name);
			    	}
			    }
			    finally {
			    	is.close();
			    }
			}
			
			return new Iterable<S>() {
				public Iterator<S> iterator() {
					return new ServicesIterator<S>(loader, serviceClassNames.iterator());
				}
			};
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static class ServicesIterator<S> implements Iterator<S> {
		
		private final ClassLoader loader;
		private final Iterator<String> serviceClassNames;

		public ServicesIterator(ClassLoader loader, Iterator<String> servicesNames) {
			this.loader = loader;
			this.serviceClassNames = servicesNames;
		}

		public boolean hasNext() {
			return serviceClassNames.hasNext();
		}

		public S next() {
			String serviceClassName = serviceClassNames.next();
			try {
				@SuppressWarnings("unchecked")
				Class<? extends S> serviceClass = (Class<? extends S>)loader.loadClass(serviceClassName);
				return serviceClass.getConstructor().newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
}
