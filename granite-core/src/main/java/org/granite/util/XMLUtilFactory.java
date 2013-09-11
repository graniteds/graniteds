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

package org.granite.util;


/**
 * @author Franck WOLFF
 */
public class XMLUtilFactory {

	private static XMLUtil xmlUtil = null;
	private static Class<? extends XMLUtil> xmlUtilClass = null;
	
	public static XMLUtil getXMLUtil() {
		if (xmlUtil == null)
			xmlUtil = buildXMLUtil();
		return xmlUtil;
	}
	
	public static void setXMLUtilClass(Class<? extends XMLUtil> clazz) {
		xmlUtilClass = clazz;
	}
	
	@SuppressWarnings("unchecked")
	private static XMLUtil buildXMLUtil() {
		try {
			if (xmlUtilClass == null)
				xmlUtilClass = (Class<? extends XMLUtil>)Thread.currentThread().getContextClassLoader().loadClass("org.granite.util.StdXMLUtil");
			return xmlUtilClass.newInstance();
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not build XML util", e);
		}
	}
}
