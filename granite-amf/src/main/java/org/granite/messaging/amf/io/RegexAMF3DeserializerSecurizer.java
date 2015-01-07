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

package org.granite.messaging.amf.io;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * A default implementation of the securizer interface that prevents arbitrary class
 * instantiation based on a regex pattern.
 * 
 * @author Franck WOLFF
 */
public class RegexAMF3DeserializerSecurizer implements AMF3DeserializerSecurizer {

	private Pattern allow = null;
	private ConcurrentMap<String, Boolean> cache = new ConcurrentHashMap<String, Boolean>();

	/**
	 * Checks if the given class name isn't matched by the configured pattern. Note
	 * that null or empty class names are allowed.
	 * 
	 * @param className the class to check.
	 * @return <code>true</code> if the given class name is allowed to be
	 * 		instantiated, <code>false</code> otherwise.
	 */
	public boolean allowInstantiation(String className) {
		if (allow == null || className == null || className.length() == 0)
			return true;
		if (cache.containsKey(className))
			return true;
		boolean allowed = allow.matcher(className).matches();
		if (allowed)
			cache.putIfAbsent(className, Boolean.TRUE);
		return allowed;
	}

	/**
	 * Set this securizer pattern. Note that you may use whitespaces in your pattern in
	 * order to improve readability: theses extra characters will be ignored.
	 * 
	 * @param param a regex containing <strong>allowed</strong> class name patterns.
	 * @throws java.util.regex.PatternSyntaxException if the given value isn't a valid
	 * 		regex pattern.
	 */
	public void setParam(String param) {
		if (param == null || param.length() == 0)
			allow = null;
		else {
			StringBuilder sb = new StringBuilder(param.length());
			for (String s : param.split("\\s", -1)) {
				if (s.length() > 0)
					sb.append(s);
			}
			allow = Pattern.compile(sb.toString());
		}
		cache = new ConcurrentHashMap<String, Boolean>();
	}
	

	/**
	 * Return this securizer pattern.
	 * 
	 * @return this securizer pattern.
	 */
	public String getParam() {
		return (allow != null ? allow.pattern() : null);
	}
}
