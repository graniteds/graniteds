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

package org.granite.jmx;

/**
 * Utility class for String formatting.
 * 
 * @author Franck WOLFF
 */
public class MBeanUtil {

	public static String format(String s) {
		if (s == null)
			return null;
		if (shouldEscape())
			return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		return s;
	}
	public static String format(String[] sArr) {
		return format(sArr, false);
	}

	public static String format(String[] sArr, boolean asSet) {
		if (sArr == null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		if (asSet)
			sb.append("{\n");
		else
			sb.append("[\n");
		
		boolean shouldEscape = shouldEscape();
		for (String s : sArr) {
			if (shouldEscape)
				s = format(s);
			sb.append("  ").append(s).append(",\n");
		}
		
		if (asSet)
			sb.append("}");
		else
			sb.append("]");
		
		return sb.toString();
	}
	
	private static boolean shouldEscape() {
		return "jboss".equals(MBeanServerLocator.getInstance().getMBeanServer().getDefaultDomain());
	}
}
