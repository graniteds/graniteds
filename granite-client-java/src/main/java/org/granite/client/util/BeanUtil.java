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
package org.granite.client.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Franck WOLFF
 */
public abstract class BeanUtil {

    public static PropertyDescriptor[] getProperties(Class<?> clazz) {
        try {
        	PropertyDescriptor[] properties = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        	Field[] fields = clazz.getDeclaredFields();
        	for (Field field : fields) {
        		if (Boolean.class.equals(field.getType())) {
        			boolean found = false;
        			for (PropertyDescriptor property : properties) {
        				if (property.getName().equals(field.getName())) {
        					found = true;
        					if (property.getReadMethod() == null) {
        						try {
        							Method readMethod = clazz.getDeclaredMethod(getIsMethodName(field.getName()));
        							if (Modifier.isPublic(readMethod.getModifiers()) && !Modifier.isStatic(readMethod.getModifiers()))
        								property.setReadMethod(readMethod);
        						}
        						catch (NoSuchMethodException e) {
        						}
        					}
        					break;
        				}
        			}
        			if (!found) {
						try {
							Method readMethod = clazz.getDeclaredMethod(getIsMethodName(field.getName()));
							if (Modifier.isPublic(readMethod.getModifiers()) && !Modifier.isStatic(readMethod.getModifiers())) {
								PropertyDescriptor[] propertiesTmp = new PropertyDescriptor[properties.length + 1];
								System.arraycopy(properties, 0, propertiesTmp, 0, properties.length);
								propertiesTmp[properties.length] = new PropertyDescriptor(field.getName(), readMethod, null);
								properties = propertiesTmp;
							}
						}
						catch (NoSuchMethodException e) {
						}
        			}
        		}
        	}
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Could not introspect properties of class: " + clazz, e);
        }
    }
    
    private static String getIsMethodName(String name) {
    	return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
