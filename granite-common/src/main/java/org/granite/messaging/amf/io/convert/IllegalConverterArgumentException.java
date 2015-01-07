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
package org.granite.messaging.amf.io.convert;

import java.lang.reflect.Type;

/**
 * @author Franck WOLFF
 */
public class IllegalConverterArgumentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Converter converter;
    private final Object value;
    private final Type targetType;

    public IllegalConverterArgumentException(Converter converter, Object value, Type targetType) {
        this(converter, value, targetType, buildDefaultMessage(converter, value, targetType), null);
    }

    public IllegalConverterArgumentException(Converter converter, Object value, Type targetType, String message) {
        this(converter, value, targetType, message, null);
    }

    public IllegalConverterArgumentException(Converter converter, Object value, Type targetType, Throwable cause) {
        this(converter, value, targetType, buildDefaultMessage(converter, value, targetType), cause);
    }

    public IllegalConverterArgumentException(Converter converter, Object value, Type targetType, String message, Throwable cause) {
        super(message, cause);
        this.converter = converter;
        this.value = value;
        this.targetType = targetType;
    }

    public Converter getConverter() {
        return converter;
    }

    public Object getValue() {
        return value;
    }

    public Type getTargetType() {
        return targetType;
    }

    private static String buildDefaultMessage(Converter converter, Object value, Type targetType) {
        try {
            return "Illegal argument for converter: " + converter + " from: " + value + " to: " + targetType;
        } catch (Exception e) {
            return "Illegal argument for converter. Additionally, an error occured when trying to build default error message: " + e.toString();
        }
    }
}
