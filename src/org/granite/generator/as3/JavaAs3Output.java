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

package org.granite.generator.as3;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.granite.generator.Output;
import org.granite.generator.Template;
import org.granite.generator.as3.reflect.JavaType;

/**
 * @author Franck WOLFF
 */
public class JavaAs3Output implements Output<ClientType> {

	private final JavaType javaType;
	private final ClientType targetType;
	private final Template template;
	private final File dir;
	private final File file;
	private final boolean outdated;
	private final String message;
	
	public JavaAs3Output(JavaType javaType, Template template, File dir, File file, boolean outdated, String message) {
		this.javaType = javaType;
		this.targetType = (javaType != null ? javaType.getClientType() : null);
		this.template = template;
		this.dir = dir;
		this.file = file;
		this.outdated = outdated;
		this.message = message;
	}

	public JavaType getJavaType() {
		return javaType;
	}

	public ClientType getTargetType() {
		return targetType;
	}

	public String getDescription() {
		return file.toString();
	}

	public Template getTemplate() {
		return template;
	}

	public File getDir() {
		return dir;
	}

	public File getFile() {
		return file;
	}

	public boolean isOutdated() {
		return outdated;
	}

	public String getMessage() {
		return message;
	}

	public OutputStream openStream() throws IOException {
		File parent = file.getParentFile();
		if (parent != null)
			parent.mkdirs();
		return new BufferedOutputStream(new FileOutputStream(file));
	}

	public PrintWriter openWriter() throws IOException {
		File parent = file.getParentFile();
		if (parent != null)
			parent.mkdirs();
		return new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}
}
