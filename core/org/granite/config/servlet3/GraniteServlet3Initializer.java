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

package org.granite.config.servlet3;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;

import org.granite.config.GraniteConfigListener;
import org.granite.messaging.webapp.AMFMessageFilter;
import org.granite.messaging.webapp.AMFMessageServlet;
import org.granite.util.ClassUtil;

/**
 * @author William DRAI
 */
@HandlesTypes({FlexFilter.class})
public class GraniteServlet3Initializer implements ServletContainerInitializer {

	public void onStartup(Set<Class<?>> scannedClasses, ServletContext servletContext) throws ServletException {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		if (scannedClasses != null) {
			classes.addAll(scannedClasses);
			classes.remove(FlexFilter.class);	// JBossWeb adds the annotation ???
		}
		if (classes.size() > 1)
			throw new ServletException("Application must have only one FlexFilter configuration class");
		
		if (!classes.isEmpty()) {
			// Configure GraniteDS only if we find a config class annotated with @FlexFilter
			Class<?> clazz = classes.iterator().next();
			FlexFilter flexFilter = clazz.getAnnotation(FlexFilter.class);
			
			servletContext.setAttribute(GraniteConfigListener.GRANITE_CONFIG_ATTRIBUTE, clazz);
			
		    servletContext.addListener(new GraniteConfigListener());
			
			if (servletContext.getFilterRegistration("AMFMessageFilter") == null) {
				FilterRegistration.Dynamic graniteFilter = servletContext.addFilter("AMFMessageFilter", AMFMessageFilter.class);
				graniteFilter.addMappingForUrlPatterns(null, true, flexFilter.graniteUrlMapping());
			}
			if (servletContext.getServletRegistration("AMFMessageServlet") == null) {
				ServletRegistration.Dynamic graniteServlet = servletContext.addServlet("AMFMessageServlet", AMFMessageServlet.class);
				graniteServlet.setLoadOnStartup(1);
				graniteServlet.addMapping(flexFilter.graniteUrlMapping());
			}
			
			try {
				if (servletContext.getServletRegistration("GravityServlet") == null) {
					Class<? extends Servlet> gravityAsyncServletClass = ClassUtil.forName("org.granite.gravity.servlet3.GravityAsyncServlet", Servlet.class);
					ServletRegistration.Dynamic gravityServlet = servletContext.addServlet("GravityServlet", gravityAsyncServletClass);
					gravityServlet.setLoadOnStartup(1);
					gravityServlet.setAsyncSupported(true);
					gravityServlet.addMapping(flexFilter.gravityUrlMapping());
				}
			}
			catch (ClassNotFoundException e) {
				servletContext.log("Could not setup GravityAsyncServlet", e);
			}
		}
	}
}
