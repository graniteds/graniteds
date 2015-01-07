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
package org.granite.messaging.amf.io;

import java.nio.charset.Charset;

/**
 * @author Franck WOLFF
 */
public interface AMF3Constants {
	
	public static final Charset UTF8 = Charset.forName("UTF-8");
	
	/*
	Adobe Systems Incorporated AMF 3 Specification (January 2013) 
	
	undefined-marker = 0x00 
	null-marker = 0x01 
	false-marker = 0x02 
	true-marker = 0x03 
	integer-marker = 0x04 
	double-marker = 0x05 
	string-marker = 0x06 
	xml-doc-marker = 0x07 
	date-marker = 0x08 
	array-marker = 0x09 
	object-marker = 0x0A 
	xml-marker = 0x0B 
	byte-array-marker = 0x0C
	vector-int-marker = 0x0D
	vector-uint-marker = 0x0E
	vector-double-marker = 0x0F
	vector-object-marker = 0x10
	dictionary-marker = 0x11
	 */

    public static final byte AMF3_UNDEFINED = 0x00;
    public static final byte AMF3_NULL = 0x01;
    public static final byte AMF3_BOOLEAN_FALSE = 0x02;
    public static final byte AMF3_BOOLEAN_TRUE = 0x03;
    public static final byte AMF3_INTEGER = 0x04;
    public static final byte AMF3_NUMBER = 0x05;
    public static final byte AMF3_STRING = 0x06;
    public static final byte AMF3_XML = 0x07;
    public static final byte AMF3_DATE = 0x08;
    public static final byte AMF3_ARRAY = 0x09;
    public static final byte AMF3_OBJECT = 0x0A;
    public static final byte AMF3_XMLSTRING = 0x0B;
    public static final byte AMF3_BYTEARRAY = 0x0C;
    public static final byte AMF3_VECTOR_INT = 0x0D;
    public static final byte AMF3_VECTOR_UINT = 0x0E;
    public static final byte AMF3_VECTOR_NUMBER = 0x0F;
    public static final byte AMF3_VECTOR_OBJECT = 0x10;
    public static final byte AMF3_DICTIONARY = 0x11;
    
    public static final int AMF3_SIGNED_INTEGER_MASK = 0x1FFFFFFF;
    public static final int AMF3_INTEGER_MAX = 0x0FFFFFFF;
    public static final int AMF3_INTEGER_MIN = 0xF0000000;
}
