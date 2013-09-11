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

package org.granite.messaging.amf;

import java.io.Serializable;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AMF3Object implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object value;

    public AMF3Object(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (value == null ? "null" : value.toString());
    }

    public String toString(String indent) {
        if (value instanceof Message)
            return ((Message)value).toString(indent);
        return new StringBuilder().append(indent).append(value).toString();
    }
}
