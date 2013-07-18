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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Franck WOLFF
 */
public class ServiceLoader<S> implements Iterable<S> {
	
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
			
			e = loader.getResources("META_INF/services/" + service.getName() + ".xml");
			if (e.hasMoreElements()) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setCoalescing(true);
				factory.setIgnoringComments(true);

				while (e.hasMoreElements())
				    serviceClassNames.addAll(getServiceClassNames(factory, e.nextElement()));
			}
			
			return new ServiceLoader<S>(loader, serviceClassNames);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static List<String> getServiceClassNames(DocumentBuilderFactory factory, URL xmlServices)
		throws IOException, SAXException, ParserConfigurationException {
		
		List<String> serviceClassNames = new ArrayList<String>();
		InputStream is = xmlServices.openStream();
		try {
			Document document = factory.newDocumentBuilder().parse(is);
			Node root = document.getFirstChild();
			if (!"services".equals(root.getNodeName()))
				throw new RuntimeException("Illegal root node in: " + xmlServices + " (should be <services>)");
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if ("service".equals(child.getNodeName())) {
					String serviceClassName = child.getTextContent();
					if (serviceClassName != null) {
						serviceClassName = serviceClassName.trim();
						if (serviceClassName.length() > 0)
							serviceClassNames.add(serviceClassName.trim());
					}
				}
			}
		}
		finally {
	    	is.close();
	    }
		return serviceClassNames;
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
