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

package org.granite.builder.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.granite.builder.BuilderConfiguration;
import org.granite.builder.BuilderListener;
import org.granite.builder.properties.Gas3Source;
import org.granite.builder.properties.GraniteProperties;
import org.granite.generator.Listener;

/**
 * @author Franck WOLFF
 */
public class FlexConfigGenerator {

	private static final String AS3_METADATA_RES = "org/granite/tide/as3-metadata.properties";
	
	public static final String FILE_NAME="granite-flex-config.xml";
	
	private static final String PREFIX =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<flex-config>\n" +
		"   <compiler>\n" +
		"      <keep-as3-metadata>\n";
	
	private static final String INFIX =
		"      </keep-as3-metadata>\n" +
		"   </compiler>\n" +
		"   <includes>\n";

	private static final String SUFFIX =
		"   </includes>\n" +
		"</flex-config>";
	
	private static final FileFilter AS_FILE_FILTER = new FileFilter() {
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().endsWith(".as");
		}
	};
	
	public static boolean generateFlexConfig(BuilderConfiguration config, BuilderListener listener, IProject project) throws Exception {
		GraniteProperties properties = config.getProperties();
		
		if (properties.getGas3().getSources() == null || properties.getGas3().getSources().isEmpty())
			return false;
		if (!ProjectUtil.isFlexBuilderProject(project))
			return false;
		
		List<String> asClasses = new ArrayList<String>();
		for (Gas3Source source : properties.getGas3().getSources()) {
			File output = new File(ProjectUtil.getProjectFile(project), source.getOutputDir());
			asClasses.addAll(listAsFiles(output));
		}
		Collections.sort(asClasses);
		
		File flexConfigFile = FileUtil.getLocationFile(project.getFile(FILE_NAME));
		
		StringBuilder sb = new StringBuilder(1024);
		sb.append(PREFIX);
		try {
			Properties as3Metadata = new Properties();
			InputStream is = config.getClassLoader().getResourceAsStream(AS3_METADATA_RES);
			//  If null -> granite.jar isn't in the classpath or is outdated...
			if (is != null) {
				try {
					as3Metadata.load(is);
				}
				finally {
					is.close();
				}
				String[] names = as3Metadata.keySet().toArray(new String[as3Metadata.size()]);
				Arrays.sort(names);
				for (Object name : names)
					sb.append("        <name>").append(name).append("</name>\n");
			}
		}
		catch (IOException e) {
			// ignore...
		}
		sb.append(INFIX);
		for (String asClass : asClasses)
			sb.append("      <symbol>").append(asClass).append("</symbol>\n");
		sb.append(SUFFIX);
		
		byte[] bs = sb.toString().getBytes("UTF-8");
		
		boolean writeFile = true;
		
		String message = Listener.MSG_FILE_UPTODATE;
		if (flexConfigFile.exists()) {
			if (flexConfigFile.length() == bs.length) {
				InputStream is = new FileInputStream(flexConfigFile);
				try {
					byte[] fc = new byte[bs.length];
					is.read(fc);
					writeFile = !Arrays.equals(bs, fc);
				}
				finally {
					is.close();
				}
			}
			if (writeFile)
				message = Listener.MSG_FILE_OUTDATED;
		}
		else
			message = Listener.MSG_FILE_NOT_EXISTS;
		
		if (writeFile) {
			listener.generating(flexConfigFile.toString(), message);
			OutputStream os = new FileOutputStream(flexConfigFile);
			try {
				os.write(bs);
			}
			finally {
				os.close();
			}
		}
		else
			listener.skipping(flexConfigFile.toString(), message);

		return writeFile;
	}
	
	private static List<String> listAsFiles(File root) throws Exception {
		List<String> files = new ArrayList<String>();
		listAsFiles(root, root, files);
		return files;
	}
	
	private static void listAsFiles(File root, File dir, List<String> files) throws Exception {
		if (dir.exists() && dir.isDirectory()) {
			for (File file : dir.listFiles(AS_FILE_FILTER)) {
				if (file.isDirectory())
					listAsFiles(root, file, files);
				else {
					StringBuilder sb = new StringBuilder();
					sb.append(file.getName().substring(0, file.getName().length() - 3));
					for (File parent = file.getParentFile(); parent != null && !root.equals(parent); parent = parent.getParentFile())
						sb.insert(0, '.').insert(0, parent.getName());
					files.add(sb.toString());
				}
			}
		}
	}
}
