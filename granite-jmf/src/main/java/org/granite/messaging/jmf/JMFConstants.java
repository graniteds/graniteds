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
package org.granite.messaging.jmf;

import java.nio.charset.Charset;

/**
 * @author Franck WOLFF
 */
public interface JMFConstants {

	static final String CLIENT_PERSISTENCE_COLLECTION_PACKAGE = "org.granite.client.persistence.collection";
	
	static final Charset UTF8 = Charset.forName("UTF-8");

	// Types with 4 bits of parameters (bit 4 always 0)
	
	static final int JMF_DOUBLE				= 0x00; // JMF_XXXX_0000
	static final int JMF_DOUBLE_OBJECT		= 0x01; // JMF_XXXX_0001
	static final int JMF_LONG				= 0x02; // JMF_XXXX_0010
	static final int JMF_LONG_OBJECT		= 0x03; // JMF_XXXX_0011
	static final int JMF_STRING				= 0x04; // JMF_XXXX_0100
	static final int JMF_PRIMITIVE_ARRAY	= 0x05; // JMF_XXXX_0101
	static final int JMF_OBJECT_ARRAY		= 0x06; // JMF_XXXX_0110

	static final int JMF_XXXX_0111			= 0x07; // JMF_XXXX_0111
	
	// Types with 3 bits of parameters (bit 5 always 0, bit 4 always 1)

	static final int JMF_INTEGER			= 0x08; // JMF_XXX0_1000
	static final int JMF_INTEGER_OBJECT		= 0x09; // JMF_XXX0_1001
	static final int JMF_ARRAY_LIST			= 0x0A; // JMF_XXX0_1010
	static final int JMF_HASH_SET			= 0x0B; // JMF_XXX0_1011
	static final int JMF_HASH_MAP			= 0x0C; // JMF_XXX0_1100
	static final int JMF_OBJECT				= 0x0D; // JMF_XXX0_1101
	static final int JMF_GENERIC_COLLECTION	= 0x0E; // JMF_XXX0_1110
	static final int JMF_GENERIC_MAP		= 0x0F; // JMF_XXX0_1111
	
	// Types with 2 bits of parameters (bit 6 always 0, bit 5 and 4 always 1)

	static final int JMF_SHORT				= 0x18; // JMF_XX01_1000
	static final int JMF_SHORT_OBJECT		= 0x19; // JMF_XX01_1001
	static final int JMF_BIG_INTEGER		= 0x1A; // JMF_XX01_1010
	static final int JMF_BIG_DECIMAL		= 0x1B; // JMF_XX01_1011
	static final int JMF_ENUM				= 0x1C; // JMF_XX01_1100

	static final int JMF_XX01_1101			= 0x1D; // JMF_XX01_1101
	static final int JMF_XX01_1110			= 0x1E; // JMF_XX01_1110
	static final int JMF_XX01_1111			= 0x1F; // JMF_XX01_1111
	
	// Types with 1 bit of parameters (bit 7 always 0, bit 6, 5 and 4 always 1)

	static final int JMF_BOOLEAN			= 0x38; // JMF_X011_1000
	static final int JMF_BOOLEAN_OBJECT		= 0x39; // JMF_X011_1001
	static final int JMF_CHARACTER			= 0x3A; // JMF_X011_1010
	static final int JMF_CHARACTER_OBJECT	= 0x3B; // JMF_X011_1011

	static final int JMF_X011_1100			= 0x3C; // JMF_X011_1100
	static final int JMF_X011_1101			= 0x3D; // JMF_X011_1101
	static final int JMF_X011_1110			= 0x3E; // JMF_X011_1110
	static final int JMF_X011_1111			= 0x3F; // JMF_X011_1111
	
	// Types with 0 bit of parameters (bit 7, 6, 5 and 4 always 1)

	static final int JMF_NULL				= 0x78; // JMF_0111_1000
	static final int JMF_CLASS				= 0x79; // JMF_0111_1001
	static final int JMF_OBJECT_END			= 0x7A; // JMF_0111_1010
	static final int JMF_BYTE				= 0x7B; // JMF_0111_1011
	static final int JMF_BYTE_OBJECT		= 0x7C; // JMF_0111_1100
	static final int JMF_FLOAT				= 0x7D; // JMF_0111_1101
	static final int JMF_FLOAT_OBJECT		= 0x7E; // JMF_0111_1110
	static final int JMF_DATE				= 0x7F; // JMF_0111_1111

	static final int JMF_SQL_DATE			= 0xF8; // JMF_1111_1000
	static final int JMF_SQL_TIME			= 0xF9; // JMF_1111_1001
	static final int JMF_SQL_TIMESTAMP		= 0xFA; // JMF_1111_1010

	static final int JMF_LOCALDATE			= 0xFB; // JMF_1111_1011
	static final int JMF_LOCALTIME			= 0xFC; // JMF_1111_1100
	static final int JMF_LOCALDATETIME		= 0xFD; // JMF_1111_1101
	static final int JMF_1111_1110			= 0xFE; // JMF_1111_1110
	static final int JMF_1111_1111			= 0xFF; // JMF_1111_1111
}
