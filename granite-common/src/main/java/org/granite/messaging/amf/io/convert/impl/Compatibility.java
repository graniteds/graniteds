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
package org.granite.messaging.amf.io.convert.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class Compatibility extends Converter {

    public Compatibility(Converters converters) {
        super(converters);
    }

    @Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        if (value == null)
            return !TypeUtil.isPrimitive(targetType);

        if (targetType instanceof Class<?>)
            return ((Class<?>)targetType).isInstance(value);

        if (targetType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType)targetType;
            if (((Class<?>)pType.getRawType()).isInstance(value)) {
                Type[] pTypeArgs = pType.getActualTypeArguments();
                if (pTypeArgs == null || pTypeArgs.length == 0)
                    return true;
                for (Type pTypeArg : pTypeArgs) {
                    if (!(pTypeArg instanceof WildcardType || pTypeArg instanceof TypeVariable || pTypeArg.equals(Object.class))) 
                        return false;
                }
                return true;
            }
        }

        return false;
    }

    @Override
	protected Object internalConvert(Object value, Type targetType) {
        return value;
    }
}
