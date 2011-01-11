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
package org.granite.osgi.constants;

/**
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public interface OSGIConstants {
	/**
	 * topic of the add service to GDS
	 */
	public static final String TOPIC_GDS_ADD_SERVICE =OSGIConstants.class.getName().replace('.','/')+"/add";
	public static final String TOPIC_GDS_REMOVE_SERVICE =OSGIConstants.class.getName().replace('.','/')+"/remove";
	public static final String SERVICE_CLASS = "serviceClass"; //one class
	public static final String SERVICE_CLASS_SET = "serviceClassSet";//a set of classes
	
	public static final String DEFAULT_FLEX_CONFIG = "META-INF/flex/services-config.xml";
	public static final String DEFAULT_GRANITEDS_CONFIG = "META-INF/granite/granite-config.xml";
	/**
	 * contextPath for AMFServlet which is defined in the MANIFEST.MF
	 * i.e. GraniteDS-Context: /MyWebContext
	 */
	public static final String GDS_CONTEXT="GraniteDS-Context";
}
