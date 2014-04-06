/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.generator.as3.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PropertyType;
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
	public Set<JavaImport> getJavaImports(ClientType clientType, boolean property);
	public JavaType getJavaTypeSuperclass(Class<?> clazz);
	public List<JavaInterface> getJavaTypeInterfaces(Class<?> clazz);
	public boolean isId(JavaFieldProperty fieldProperty);
	public boolean isUid(JavaProperty property);
	public boolean isVersion(JavaProperty property);
	public boolean isLazy(JavaProperty property);
	public ClientType getClientType(Type type, Class<?> declaringClass, ParameterizedType[] declaringTypes, PropertyType propertyType);
	public ClientType getAs3Type(Class<?> clazz);
}
