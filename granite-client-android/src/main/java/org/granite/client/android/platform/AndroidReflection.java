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
package org.granite.client.android.platform;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.granite.client.android.messaging.reflect.AndroidBypassConstructorAllocator;
import org.granite.messaging.reflect.FieldProperty;
import org.granite.messaging.reflect.MethodProperty;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class AndroidReflection extends Reflection {

	public AndroidReflection(ClassLoader classLoader) {
		super(classLoader, new AndroidBypassConstructorAllocator());
	}

	@Override
	protected FieldProperty newFieldProperty(Field field) {
		return new AndroidFieldProperty(field);
	}

	@Override
	protected MethodProperty newMethodProperty(Method getter, Method setter, String name) {
		return new AndroidMethodProperty(getter, setter, name);
	}
}
