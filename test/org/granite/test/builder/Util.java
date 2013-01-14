package org.granite.test.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Util {
	
	public static String readFile(File file) throws IOException {
		BufferedReader fr = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = fr.readLine()) != null)
			sb.append(line).append("\n");
		return sb.toString();
	}

}
