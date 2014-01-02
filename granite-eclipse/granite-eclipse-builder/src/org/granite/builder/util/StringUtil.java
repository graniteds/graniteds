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

package org.granite.builder.util;

import java.util.regex.Pattern;

/**
 * @author Franck WOLFF
 */
public class StringUtil {
	
	public static String unNull(String s) {
		return s == null ? "" : s;
	}
    
    public static String defaultText(String s, String def) {
    	return (s == null || s.length() == 0 ? def : s);
    }

	public static String tokenize(String s, int max, String delimiter) {
		
		if (max <= 0 || s == null || s.length() < max)
			return s;

		StringBuilder sb = new StringBuilder();
		
		int length = 0;
		for (String token : s.split("\\s", -1)) {
			if (length > 0) {
				if (length + token.length() >= max) {
					sb.append(delimiter);
					length = 0;
				}
				else {
					sb.append(' ');
					length++;
				}
			}
			sb.append(token);
			length += token.length();
		}
		
		return sb.toString();
	}
	
	public static String join(String[] items, char separator) {
		return join(items, String.valueOf(separator));
	}
	
	public static String join(String[] items, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String item : items) {
			if (sb.length() > 0)
				sb.append(separator);
			sb.append(item);
		}
		return sb.toString();
	}
	
	public static String[] split(String items, char separator) {
		return split(items, String.valueOf(separator));
	}
	
	public static String[] split(String items, String separator) {
		return items.split(Pattern.quote(separator), -1);
	}
	
	public static String regexifyPathPattern(String pattern) {
		StringBuilder sb = new StringBuilder();
		
		boolean quote = false;
		sb.append('^');
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			switch (c) {
			case '*':
				if (quote) {
					sb.append("\\E");
					quote = false;
				}
				// Double star (any character even the path separator '/').
				if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
					sb.append(".*");
					i++;
				}
				// Single star (any character except '/').
				else
					sb.append("[^/]*");
				break;
			case '?':
				if (quote) {
					sb.append("\\E");
					quote = false;
				}
				sb.append("[^/]");
				break;
			default:
				if (!quote) {
					sb.append("\\Q");
					quote = true;
				}
				sb.append(c);
				break;
			}
		}
		if (quote)
			sb.append("\\E");
		sb.append('$');
		
		return sb.toString();
	}
}
