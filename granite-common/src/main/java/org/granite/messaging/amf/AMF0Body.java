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
/*
 * www.openamf.org
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.granite.messaging.amf;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import flex.messaging.messages.Message;

/**
 * AMF Body
 *
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @author Pat Maddox <pergesu@users.sourceforge.net>
 * @see AMF0Header
 * @see AMF0Message
 * @version $Revision: 1.19 $, $Date: 2003/09/20 01:05:24 $
 */
public class AMF0Body implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String target;
    protected String serviceName;
    protected String serviceMethodName;
    protected String response;
    protected Object value;
    protected byte type;
    /**
     * Unknow object type
     */
    public static final byte DATA_TYPE_UNKNOWN = -1;
    /**
     * Number object type
     */
    public static final byte DATA_TYPE_NUMBER = 0;
    /**
     * Boolean object type
     */
    public static final byte DATA_TYPE_BOOLEAN = 1;
    /**
     * String object type
     */
    public static final byte DATA_TYPE_STRING = 2;
    /**
     * Object object type
     */
    public static final byte DATA_TYPE_OBJECT = 3;
    /**
     * Movie clip object type
     */
    public static final byte DATA_TYPE_MOVIE_CLIP = 4;
    /**
     * NULL object type
     */
    public static final byte DATA_TYPE_NULL = 5;
    /**
     * Undefined object type
     */
    public static final byte DATA_TYPE_UNDEFINED = 6;
    /**
     * Reference object type
     */
    public static final byte DATA_TYPE_REFERENCE_OBJECT = 7;
    /**
     * Mixed Array Object type
     */
    public static final byte DATA_TYPE_MIXED_ARRAY = 8;
    /**
     * Object end type
     */
    public static final byte DATA_TYPE_OBJECT_END = 9;
    /**
     * Array Object type
     */
    public static final byte DATA_TYPE_ARRAY = 10;
    /**
     * Date object type
     */
    public static final byte DATA_TYPE_DATE = 11;
    /**
     * Long String object type
     */
    public static final byte DATA_TYPE_LONG_STRING = 12;
    /**
     * General Object type
     */
    public static final byte DATA_TYPE_AS_OBJECT = 13;
    /**
     * RecordSet object type
     */
    public static final byte DATA_TYPE_RECORDSET = 14;
    /**
     * XML Document object type
     */
    public static final byte DATA_TYPE_XML = 15;
    /**
     * Custom class object type
     */
    public static final byte DATA_TYPE_CUSTOM_CLASS = 16;
    /**
     * AMF3 data
     */
    public static final byte DATA_TYPE_AMF3_OBJECT = 17;

    /**
     * AMF body with unknown type
     *
     * @param target
     * @param response
     * @param value
     */
    public AMF0Body(String target, String response, Object value) {
        this(target, response, value, DATA_TYPE_UNKNOWN);
    }

    /**
     * AMF Body constructor
     *
     * @param target
     * @param response
     * @param value
     * @param type
     */
    public AMF0Body(String target, String response, Object value, byte type) {
        this.response = response;
        this.value = value;
        this.type = type;
        setTarget(target);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
        int dotIndex = target.lastIndexOf('.');
        if (dotIndex > 0) {
            this.serviceName = target.substring(0, dotIndex);
            this.serviceMethodName = target.substring(dotIndex + 1);
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceMethodName() {
        return serviceMethodName;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isFirstBody() {
        return "/1".equals(response);
    }

    public int getBodyIndex() {
        if (response != null && response.length() > 1) {
            try {
                return Integer.parseInt(response.substring(1));
            } catch (Exception e) {
            }
        }
        return 0; // response starts with 1.
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    /**
     * Returns object type
     *
     * @return the object type.
     */
    public byte getType() {
        return type;
    }
    /**
     * Sets object type
     *
     * @param type
     */
    public void setType(byte type) {
        this.type = type;
    }
    /**
     * Returns String description of object type
     *
     * @param type object type
     * @return the object type description
     */
    public static String getObjectTypeDescription(byte type) {
        switch (type) {
            case DATA_TYPE_UNKNOWN:
                return "UNKNOWN";
            case DATA_TYPE_NUMBER:
                return "NUMBER";
            case DATA_TYPE_BOOLEAN:
                return "BOOLEAN";
            case DATA_TYPE_STRING:
                return "STRING";
            case DATA_TYPE_OBJECT:
                return "OBJECT";
            case DATA_TYPE_MOVIE_CLIP:
                return "MOVIECLIP";
            case DATA_TYPE_NULL:
                return "NULL";
            case DATA_TYPE_UNDEFINED:
                return "UNDEFINED";
            case DATA_TYPE_REFERENCE_OBJECT:
                return "REFERENCE";
            case DATA_TYPE_MIXED_ARRAY:
                return "MIXED_ARRAY";
            case DATA_TYPE_OBJECT_END:
                return "OBJECT_END";
            case DATA_TYPE_ARRAY:
                return "ARRAY";
            case DATA_TYPE_DATE:
                return "DATE";
            case DATA_TYPE_LONG_STRING:
                return "LONG_STRING";
            case DATA_TYPE_AS_OBJECT:
                return "AS_OBJECT";
            case DATA_TYPE_RECORDSET:
                return "RECORDSET";
            case DATA_TYPE_XML:
                return "XML";
            case DATA_TYPE_CUSTOM_CLASS:
                return "CUSTOM_CLASS";
            case DATA_TYPE_AMF3_OBJECT:
                return "AMF3_OBJECT";
            default:
                return "UNKNOWN: 0x" + Integer.toBinaryString(type);
        }
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        return (new StringBuffer(1024)
            .append('\n').append(indent).append(AMF0Body.class.getName()).append(" {")
            .append('\n').append(indent).append("  target = ").append(getTarget())
            .append('\n').append(indent).append("  serviceName = ").append(getServiceName())
            .append('\n').append(indent).append("  serviceMethodName = ").append(getServiceMethodName())
            .append('\n').append(indent).append("  response = ").append(getResponse())
            .append('\n').append(indent).append("  type = ").append(getObjectTypeDescription(type))
            .append('\n').append(indent).append("  value = ").append(printValue(value, indent + "  "))
            .append('\n').append(indent).append('}')
            .toString()
        );
    }

    private static String printValue(Object value, String indent) {

        if (value == null)
            return "null";

        if (value instanceof AMF3Object)
            return ((AMF3Object)value).toString(indent);
        if (value instanceof Message)
            return ((Message)value).toString(indent);

        if (value.getClass().isArray()) {
            final int length = Array.getLength(value);
            List<Object> list = new ArrayList<Object>(length);
            for (int i = 0; i < length; i++)
                list.add(Array.get(value, i));
            value = list;
        }

        if (value instanceof List<?>) {
            List<?> list = (List<?>)value;

            StringBuilder sb = new StringBuilder(512);

            final String innerIndent = indent + "  ";
            sb.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0)
                    sb.append(',');
                sb.append('\n').append(indent).append("  ").append(printValue(list.get(i), innerIndent));
            }
            if (list.size() > 0)
                sb.append('\n').append(indent);
            sb.append(']');

            return sb.toString();
        }

        return value.toString();
    }
}
