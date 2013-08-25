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
import java.util.Collection;
import java.util.Map;

import org.granite.messaging.amf.AMF3Constants;
import org.granite.messaging.amf.io.util.Property;

/**
 * @author Franck WOLFF
 */
public abstract class AMFSpecialValue<T> implements AMF3Constants {
	
	public final byte type;
	public final T value;
	
	protected AMFSpecialValue(byte type, T value) {
		if (value == null)
			throw new NullPointerException();
		
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " {type=" + type + ", value=" + value + "}";
	}

	public static Object getSpecialValue(Property property, Object o) {
    	if (o != null && !(o instanceof Externalizable)) {
    		if (o instanceof Collection || (o.getClass().isArray() && o.getClass().getComponentType() != Byte.TYPE)) {
	    		if (property.isAnnotationPresent(AMFVectorObject.class)) {
	    			AMFVectorObject annotation = property.getAnnotation(AMFVectorObject.class);
	    			return new AMFVectorObjectValue(o, annotation.type(), annotation.fixed());
	    		}
	    		if (property.isAnnotationPresent(AMFVectorInt.class))
	    			return new AMFVectorIntValue(o, property.getAnnotation(AMFVectorInt.class).fixed());
	    		if (property.isAnnotationPresent(AMFVectorNumber.class))
	    			return new AMFVectorNumberValue(o, property.getAnnotation(AMFVectorNumber.class).fixed());
	    		if (property.isAnnotationPresent(AMFVectorUint.class))
	    			return new AMFVectorUintValue(o, property.getAnnotation(AMFVectorUint.class).fixed());
    		}
    		else if (o instanceof Map && property.isAnnotationPresent(AMFDictionary.class))
    			return new AMFDictionaryValue((Map<?, ?>)o, property.getAnnotation(AMFDictionary.class).weakKeys());
    	}
    	
    	return o;
	}
}