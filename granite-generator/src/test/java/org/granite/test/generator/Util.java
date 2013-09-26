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
package org.granite.test.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.granite.generator.as3.reflect.JavaImport;
import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;

public class Util {
	
	public static String readFile(File file) throws IOException {
		BufferedReader fr = new BufferedReader(new FileReader(file));
		try {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = fr.readLine()) != null)
				sb.append(line).append("\n");
			return sb.toString();
		}
		finally {
			fr.close();
		}
	}
	
	public static JavaMethod findMethod(JavaRemoteDestination jrd, String name, Class<?>... argTypes) {
		JavaMethod jm = null;
		Iterator<JavaMethod> i = jrd.getMethods().iterator();
		while (i.hasNext()) {
			JavaMethod m = i.next();
			if (m.getName().equals(name) && m.getParameterTypes().length == argTypes.length) {
				if (!Arrays.equals(argTypes, m.getParameterTypes()))
					continue;
				if (jm != null)
					throw new RuntimeException("Duplicate methods found");
				jm = m;
			}
		}
		return jm;
	}
	
	public static JavaImport findImport(JavaRemoteDestination jrd, String name) {
		Iterator<JavaImport> i = jrd.getImports().iterator();
		while (i.hasNext()) {
			JavaImport imp = i.next();
			if (imp.getImportQualifiedName().equals(name))
				return imp;
		}
		return null;
	}

}
