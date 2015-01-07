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
package org.granite.messaging.amf.types;

import java.io.Externalizable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.granite.messaging.amf.io.util.Property;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class AMFSpecialValueFactory {
	
	protected final AMFVectorObjectAliaser vectorObjectAlias;
	
	public AMFSpecialValueFactory() {
		this(null);
	}

	public AMFSpecialValueFactory(AMFVectorObjectAliaser vectorObjectAlias) {
		this.vectorObjectAlias = (vectorObjectAlias != null ? vectorObjectAlias : new AMFBasicVectorObjectAliaser());
	}
	
	public SpecialValueFactory<?> getValueFactory(Property property) {
		Type propertyType = property.getType();
		Class<?> type = TypeUtil.classOfType(propertyType);
		
        if (!Externalizable.class.isAssignableFrom(type)) {
        	if ((type.isArray() && type.getComponentType() != byte.class) || Collection.class.isAssignableFrom(type)) {
	    		if (property.isAnnotationPresent(AMFVectorInt.class))
	    			return new AMFVectorIntValueFactory(property.getAnnotation(AMFVectorInt.class));
	    		if (property.isAnnotationPresent(AMFVectorNumber.class))
	    			return new AMFVectorNumberValueFactory(property.getAnnotation(AMFVectorNumber.class));
	    		if (property.isAnnotationPresent(AMFVectorUint.class))
	    			return new AMFVectorUintValueFactory(property.getAnnotation(AMFVectorUint.class));
	    		if (property.isAnnotationPresent(AMFVectorObject.class)) {
	    			AMFVectorObject annotation = property.getAnnotation(AMFVectorObject.class);
	    			String vectorComponentType = annotation.type();
	    			if (vectorComponentType == null || vectorComponentType.length() == 0) {
		    			Class<?> componentClass = TypeUtil.componentClassOfType(propertyType);
		    			vectorComponentType = vectorObjectAlias.aliasFor(componentClass);
	    			}
	    			return new AMFVectorObjectValueFactory(annotation, vectorComponentType);
	    		}
        	}
        	else if (Map.class.isAssignableFrom(type) && property.isAnnotationPresent(AMFDictionary.class)) {
        		return new AMFDictionaryValueFactory(property.getAnnotation(AMFDictionary.class));
        	}
        }

        return null;
	}
	
	public interface SpecialValueFactory<T extends AMFSpecialValue<?>> {
		public T create(Object o);
	}

	public class AMFVectorIntValueFactory implements SpecialValueFactory<AMFVectorIntValue> {
		
		private final AMFVectorInt annotation;
		
		public AMFVectorIntValueFactory(AMFVectorInt annotation) {
			this.annotation = annotation;
		}

		@Override
		public AMFVectorIntValue create(Object o) {
			return (o != null ? new AMFVectorIntValue(o, annotation.fixed()) : null);
		}
	}

	public class AMFVectorUintValueFactory implements SpecialValueFactory<AMFVectorUintValue> {
		
		private final AMFVectorUint annotation;
		
		public AMFVectorUintValueFactory(AMFVectorUint annotation) {
			this.annotation = annotation;
		}

		@Override
		public AMFVectorUintValue create(Object o) {
			return (o != null ? new AMFVectorUintValue(o, annotation.fixed()) : null);
		}
	}

	public class AMFVectorNumberValueFactory implements SpecialValueFactory<AMFVectorNumberValue> {
		
		private final AMFVectorNumber annotation;
		
		public AMFVectorNumberValueFactory(AMFVectorNumber annotation) {
			this.annotation = annotation;
		}

		@Override
		public AMFVectorNumberValue create(Object o) {
			return (o != null ? new AMFVectorNumberValue(o, annotation.fixed()) : null);
		}
	}

	public class AMFVectorObjectValueFactory implements SpecialValueFactory<AMFVectorObjectValue> {
		
		private final AMFVectorObject annotation;
		private final String type;
		
		public AMFVectorObjectValueFactory(AMFVectorObject annotation, String type) {
			this.annotation = annotation;
			this.type = type;
		}

		@Override
		public AMFVectorObjectValue create(Object o) {
			return (o != null ? new AMFVectorObjectValue(o, type, annotation.fixed()) : null);
		}
	}

	public class AMFDictionaryValueFactory implements SpecialValueFactory<AMFDictionaryValue> {
		
		private final AMFDictionary annotation;
		
		public AMFDictionaryValueFactory(AMFDictionary annotation) {
			this.annotation = annotation;
		}

		@Override
		public AMFDictionaryValue create(Object o) {
			return (o != null ? new AMFDictionaryValue((Map<?, ?>)o, annotation.weakKeys()) : null);
		}
	}
}
