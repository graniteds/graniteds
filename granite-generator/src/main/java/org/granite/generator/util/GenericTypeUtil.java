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
package org.granite.generator.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public abstract class GenericTypeUtil {
    
    public static ParameterizedType[] getDeclaringTypes(Class<?> type) {
		List<ParameterizedType> supertypes = new ArrayList<ParameterizedType>();
		
		Type stype = type.getGenericSuperclass();
		Class<?> sclass = type.getSuperclass();
		while (sclass != null && sclass != Object.class) {
			if (stype instanceof ParameterizedType)
				supertypes.add((ParameterizedType)stype);
			stype = sclass.getGenericSuperclass();
			sclass = sclass.getSuperclass();
		}
		
		collectGenericInterfaces(type.getGenericInterfaces(), supertypes);
		
		return supertypes.isEmpty() ? null : supertypes.toArray(new ParameterizedType[supertypes.size()]);
    }
    
    private static void collectGenericInterfaces(Type[] types, List<ParameterizedType> supertypes) {
    	if (types == null)
    		return;
		for (Type t : types) {
			if (t instanceof ParameterizedType)
				supertypes.add((ParameterizedType)t);
			else
				collectGenericInterfaces(((Class<?>)t).getGenericInterfaces(), supertypes);
		}
    }

    public static Type primitiveToWrapperType(Type type) {
		if (type.equals(short.class))
			return Short.class;
		else if (type.equals(byte.class))
			return Byte.class;
		else if (type.equals(boolean.class))
			return Boolean.class;
		else if (type == int.class)
			return Integer.class;
		else if (type == long.class)
			return Long.class;
		else if (type == float.class)
			return Float.class;
		else if (type == double.class)
			return Double.class;
		return type;
    }
    
    public static Type resolveTypeVariable(Type genericType, Class<?> declaringClass, ParameterizedType[] declaringTypes) {
    	if (genericType instanceof TypeVariable && declaringTypes != null) {
    		int index = -1;
    		TypeVariable<?> typeVariable = (TypeVariable<?>)genericType;
    		ParameterizedType declaringType = null;
    		for (int j = 0; j < declaringClass.getTypeParameters().length; j++) {
    			Type typeParameter = declaringClass.getTypeParameters()[j];
    			if (typeParameter == typeVariable)
    				index = j;
    			else if (typeVariable.getBounds() != null) {
    				for (Type t : typeVariable.getBounds()) {
    					if (typeParameter == t) {
    						index = j;
    						break;
    					}
    				}
    			}
    			if (index >= 0) {
					for (ParameterizedType t : declaringTypes) {
						if (declaringClass.isAssignableFrom(ClassUtil.classOfType(t))) {
							declaringType = t;
							break;
						}
					}
					break;
    			}
    		}
    		if (declaringType != null && index >= 0 && index < declaringType.getActualTypeArguments().length)
    			return declaringType.getActualTypeArguments()[index];
    	}
    	return genericType;
    }
}
