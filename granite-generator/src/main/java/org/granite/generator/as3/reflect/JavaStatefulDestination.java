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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.granite.messaging.annotations.Include;

/**
 * @author Franck WOLFF
 */
public class JavaStatefulDestination extends JavaRemoteDestination {

	public JavaStatefulDestination(JavaTypeFactory provider, Class<?> type, URL url) {
		super(provider, type, url);
	}
	
	private boolean isPropertyAccessor(Method method) {
		return ((method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0) ||
				(method.getName().startsWith("set") && method.getParameterTypes().length == 1 && method.getReturnType() == void.class);
	}

	@Override
	protected boolean shouldGenerateMethod(Method method) {
		return super.shouldGenerateMethod(method) && !isPropertyAccessor(method);
	}

	@Override
	protected boolean shouldGenerateProperty(Method method) {
		return Modifier.isPublic(method.getModifiers())
			&& !Modifier.isStatic(method.getModifiers())
			&& !method.isAnnotationPresent(Include.class)
			&& isPropertyAccessor(method);
	}
}
