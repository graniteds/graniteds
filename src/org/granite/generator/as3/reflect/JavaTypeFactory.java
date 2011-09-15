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

package org.granite.generator.as3.reflect;

import java.util.List;

import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.reflect.JavaAbstractType.GenerationType;
import org.granite.generator.as3.reflect.JavaType.Kind;

/**
 * @author Franck WOLFF
 */
public interface JavaTypeFactory {

	public JavaType getJavaType(Class<?> clazz);
	public Kind getKind(Class<?> clazz);
	public GenerationType getGenerationType(Kind kind, Class<?> clazz);
	public JavaImport getJavaImport(Class<?> clazz);
	public JavaType getJavaTypeSuperclass(Class<?> clazz);
	public List<JavaInterface> getJavaTypeInterfaces(Class<?> clazz);
	public boolean isId(JavaFieldProperty fieldProperty);
	public boolean isUid(JavaProperty property);
	public boolean isVersion(JavaProperty property);
	public boolean isLazy(JavaProperty property);
	public As3Type getAs3Type(Class<?> clazz);
}
