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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.granite.generator.Input;
import org.granite.generator.as3.reflect.JavaType;

/**
 * @author Franck WOLFF
 */
public class JavaAs3Input implements Input<Class<?>> {

	private final Class<?> type;
	private final File file;
	private final Map<String, String> attributes;
	private JavaType javaType = null;
	
	public JavaAs3Input(Class<?> type, File file) {
		this(type, file, null);
	}
	
	public JavaAs3Input(Class<?> type, File file, Map<String, String> attributes) {
		this.type = type;
		this.file = file;
		this.attributes = (attributes != null ? attributes : new HashMap<String, String>());
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return type.getName();
	}

	public File getFile() {
		return file;
	}

	public JavaType getJavaType() {
		return javaType;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setJavaType(JavaType javaType) {
		this.javaType = javaType;
	}
}
