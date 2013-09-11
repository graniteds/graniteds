/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

	public Object createSpecialValue(Property property, Object o) {
    	if (o != null && !(o instanceof Externalizable)) {
    		if (o instanceof Collection || (o.getClass().isArray() && o.getClass().getComponentType() != Byte.TYPE)) {
	    		if (property.isAnnotationPresent(AMFVectorInt.class))
	    			return new AMFVectorIntValue(o, property.getAnnotation(AMFVectorInt.class).fixed());
	    		if (property.isAnnotationPresent(AMFVectorNumber.class))
	    			return new AMFVectorNumberValue(o, property.getAnnotation(AMFVectorNumber.class).fixed());
	    		if (property.isAnnotationPresent(AMFVectorUint.class))
	    			return new AMFVectorUintValue(o, property.getAnnotation(AMFVectorUint.class).fixed());
	    		if (property.isAnnotationPresent(AMFVectorObject.class)) {
	    			AMFVectorObject annotation = property.getAnnotation(AMFVectorObject.class);
	    			String type = annotation.type();
	    			if (type == null || type.length() == 0) {
		    			Type propertyType = property.getType();
		    			Class<?> componentClass = TypeUtil.componentClassOfType(propertyType);
		    			type = vectorObjectAlias.aliasFor(componentClass);
	    			}
	    			return new AMFVectorObjectValue(o, type, annotation.fixed());
	    		}
    		}
    		else if (o instanceof Map && property.isAnnotationPresent(AMFDictionary.class))
    			return new AMFDictionaryValue((Map<?, ?>)o, property.getAnnotation(AMFDictionary.class).weakKeys());
    	}
    	
    	return o;
	}
}
