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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class ServiceLoader<S> implements Iterable<S> {
	
	private static final Logger log = Logger.getLogger(ServiceLoader.class);
	
	private final ClassLoader loader;
	private final List<String> serviceClassNames;
	
	private ServiceLoader(ClassLoader loader, List<String> servicesNames) {
		this.loader = loader;
		this.serviceClassNames = servicesNames;
	}

	public Iterator<S> iterator() {
		return new ServicesIterator<S>(loader, serviceClassNames.iterator());
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public static <S> ServiceLoader<S> load(Class<S> service) {
		return load(service, Thread.currentThread().getContextClassLoader());
	}

	public static <S> ServiceLoader<S> load(final Class<S> service, final ClassLoader loader) {
		List<String> serviceClassNames = new ArrayList<String>();
		
		try {
			// Standard.
			Enumeration<URL> en = loader.getResources("META-INF/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			// Android support (META-INF/** are not included in APK files).
			en = loader.getResources("meta_inf/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			return new ServiceLoader<S>(loader, serviceClassNames);
		}
		catch (Exception e) {
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
			        	log.debug("Adding service %s from %s", serviceClassName, serviceFile);
			        	serviceClassNames.add(serviceClassName);
			        }
		    	}
		    }
		    finally {
		    	is.close();
		    }
		}
		catch (Exception e) {
			log.error(e, "Could not parse service file %s", serviceFile);
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
			log.debug("Loading service %s", serviceClassName);
			try {
				@SuppressWarnings("unchecked")
				Class<? extends S> serviceClass = (Class<? extends S>)loader.loadClass(serviceClassName);
				return serviceClass.getConstructor().newInstance();
			}
			catch (Exception e) {
	        	log.error(e, "Could not load service %s", serviceClassName);
				throw new RuntimeException(e);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
}
