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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.BooleanCodec;
import org.granite.messaging.jmf.codec.std.ByteCodec;
import org.granite.messaging.jmf.codec.std.CharacterCodec;
import org.granite.messaging.jmf.codec.std.DoubleCodec;
import org.granite.messaging.jmf.codec.std.FloatCodec;
import org.granite.messaging.jmf.codec.std.IntegerCodec;
import org.granite.messaging.jmf.codec.std.LongCodec;
import org.granite.messaging.jmf.codec.std.NullCodec;
import org.granite.messaging.jmf.codec.std.ShortCodec;
import org.granite.messaging.jmf.codec.std.StringCodec;
import org.granite.messaging.reflect.Property;

/**
 * @author Franck WOLFF
 */
public interface CodecRegistry extends JMFConstants {

	NullCodec getNullCodec();
	
	BooleanCodec getBooleanCodec();
	CharacterCodec getCharacterCodec();
	ByteCodec getByteCodec();
	ShortCodec getShortCodec();
	IntegerCodec getIntegerCodec();
	LongCodec getLongCodec();
	FloatCodec getFloatCodec();
	DoubleCodec getDoubleCodec();
	StringCodec getStringCodec();

	<T> StandardCodec<T> getCodec(int jmfType);
	<T> StandardCodec<T> getCodec(ExtendedObjectOutput out, Object v);
	
	ExtendedObjectCodec findExtendedEncoder(ExtendedObjectOutput out, Object v);
	ExtendedObjectCodec findExtendedDecoder(ExtendedObjectInput in, String className);
	
	static interface PrimitivePropertyCodec {
		public void encodePrimitive(OutputContext ctx, Object holder, Property property)
			throws IllegalAccessException, IOException, InvocationTargetException;
		public void decodePrimitive(InputContext ctx, Object holder, Property property)
			throws IllegalAccessException, IOException, InvocationTargetException;
	}
	
	PrimitivePropertyCodec getPrimitivePropertyCodec(Class<?> propertyCls);

	int extractJmfType(int parameterizedJmfType);
}
