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

package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.IllegalConverterArgumentException;
import org.granite.messaging.amf.io.convert.Reverter;

/**
 * @author Franck WOLFF
 */
public class String2URI extends Converter implements Reverter {

    public String2URI(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        return (targetType.equals(URI.class) || targetType.equals(URL.class)) && (value == null || value instanceof String);
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;
        
        try {
        	return (targetType.equals(URI.class) ? new URI((String)value) : new URL((String)value));
        } catch (Exception e) {
        	throw new IllegalConverterArgumentException(this, value, targetType, "Illegal URI: " + value);
        }
    }

	public boolean canRevert(Object value) {
		return (value == null) || (value instanceof URI) || (value instanceof URL);
	}

	public Object revert(Object value) {
		if (value != null)
			value = value.toString();
		return value;
	}
}
