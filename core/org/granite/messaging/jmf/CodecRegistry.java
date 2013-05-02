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

package org.granite.messaging.jmf;

import java.io.IOException;
import java.lang.reflect.Field;

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
	<T> StandardCodec<T> getCodec(Object v);
	
	ExtendedObjectCodec findExtendedEncoder(ExtendedObjectOutput out, Object v);
	ExtendedObjectCodec findExtendedDecoder(ExtendedObjectInput in, String className)
		throws ClassNotFoundException;
	
	static interface PrimitiveFieldCodec {
		public void encodePrimitive(OutputContext ctx, Object v, Field field)
			throws IllegalAccessException, IOException;
		public void decodePrimitive(InputContext ctx, Object v, Field field)
			throws IllegalAccessException, IOException;
	}
	
	PrimitiveFieldCodec getPrimitiveFieldCodec(Class<?> fieldCls);

	int extractJmfType(int parameterizedJmfType);
	
	int jmfTypeOfPrimitiveClass(Class<?> cls);
	Class<?> primitiveClassOfJmfType(int jmfType);
}
