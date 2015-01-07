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
package org.granite.messaging.jmf.codec.std.impl.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.jmf.InputContext;
import org.granite.messaging.jmf.OutputContext;

/**
 * @author Franck WOLFF
 */
public class ClassNameUtil {
	
	private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_$.".toCharArray();
	
	private static final byte[] ALPHABET_INDICES = new byte[128]; 
	static {
		for (int i = 0; i < ALPHABET_INDICES.length; i++)
			ALPHABET_INDICES[i] = -1;
		for (int i = 0; i < ALPHABET.length; i++)
			ALPHABET_INDICES[ALPHABET[i]] = (byte)i;
	}

	private static final int CLASS_NAME_END = ALPHABET.length;
	private static final int INDEX_0 = CLASS_NAME_END + 1;
	private static final int INDEX_OVERFLOW = 0xFF - INDEX_0;
	
	private ClassNameUtil() {
		throw new UnsupportedOperationException("Not instantiable");
	}
	
	public static void initClassNameDictionary(List<String> dictionary, String className) {
		if (!dictionary.contains(className)) {
			List<String> lookups = new ArrayList<String>();
			
			String lookup = className;
			do {
				lookups.add(lookup);
				int lastIndexOfDot = lookup.lastIndexOf('.');
				if (lastIndexOfDot == -1) {
					lookup = "";
					break;
				}
				lookup = lookup.substring(0, lastIndexOfDot);
			}
			while (!dictionary.contains(lookup));
			
			for (int i = lookups.size() - 1; i >= 0; i--)
				dictionary.add(lookups.get(i));
		}
	}
	
	public static void encodeClassName(OutputContext ctx, String className) throws IOException {
		final OutputStream os = ctx.getOutputStream();
		
		String lookup = className;
		
		int indexOfStoredClassName = ctx.indexOfClassName(lookup);
		if (indexOfStoredClassName == -1) {
			List<String> lookups = new ArrayList<String>();
			
			do {
				lookups.add(lookup);
				int lastIndexOfDot = lookup.lastIndexOf('.');
				if (lastIndexOfDot == -1) {
					lookup = "";
					break;
				}
				lookup = lookup.substring(0, lastIndexOfDot);
				indexOfStoredClassName = ctx.indexOfClassName(lookup);
			}
			while (indexOfStoredClassName == -1);
			
			for (int i = lookups.size() - 1; i >= 0; i--)
				ctx.addToClassNames(lookups.get(i));
		}
		
		if (indexOfStoredClassName != -1) {
			if (indexOfStoredClassName < INDEX_OVERFLOW)
				os.write(indexOfStoredClassName + INDEX_0);
			else {
				os.write(0xFF);
				IntegerUtil.encodeVariableUnsignedInteger(ctx, indexOfStoredClassName - INDEX_OVERFLOW);
			}
		}
		
		if (lookup.length() != className.length()) {
			int start = (lookup.length() == 0 ? 0 : lookup.length() + 1);
			for (int i = start; i < className.length(); i++) {
				int code = ALPHABET_INDICES[className.charAt(i)];
				if (code == -1)
					throw new RuntimeException();
				os.write(code);
			}
		}

		os.write(CLASS_NAME_END);
	}
	
	public static String decodeClassName(InputContext ctx) throws IOException {
		String className = "";
		
		int code = ctx.safeRead();
		if (code > CLASS_NAME_END) {
			if (code != 0xFF)
				className = ctx.getClassName(code - INDEX_0);
			else {
				int index = IntegerUtil.decodeVariableUnsignedInteger(ctx) + INDEX_OVERFLOW;
				className = ctx.getClassName(index);
			}
			
			code = ctx.safeRead();
		}
		
		if (code != CLASS_NAME_END) {
			StringBuilder sb = (className.length() > 0 ? new StringBuilder(className).append('.') : new StringBuilder(64));
			do {
				char c = ALPHABET[code];
				if (c == '.')
					ctx.addToClassNames(sb.toString());
				sb.append(c);
				code = ctx.safeRead();
			}
			while (code != CLASS_NAME_END);
			
			className = sb.toString();
			
			ctx.addToClassNames(className);
		}
		
		return className;
	}
}
