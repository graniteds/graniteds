package org.granite.test.builder;

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
