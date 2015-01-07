/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationUtils {
	
	public static boolean isAnnotatedWith(Object entity, String propertyName, Class<? extends Annotation> annotationClass) {
		Field field = null;
		Class<?> c = entity.getClass();
		while (c != null && !c.equals(Object.class)) {
			for (Field f : c.getDeclaredFields()) {
				if (f.getName().equals(propertyName)) {
					field = f;
					break;
				}
			}
			if (field != null)
				break;
			c = c.getSuperclass();
		}
		if (field.isAnnotationPresent(annotationClass))
			return true;
		
		String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		try {
			Method getter = entity.getClass().getMethod(getterName);
			return getter.isAnnotationPresent(annotationClass);
		} 
		catch (NoSuchMethodException e) {
			return false;
		}
	}

}
