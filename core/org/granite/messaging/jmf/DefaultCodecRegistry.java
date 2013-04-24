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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.jmf.codec.BijectiveCodec;
import org.granite.messaging.jmf.codec.ConditionalObjectCodec;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.PrimitiveCodec;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.BooleanCodec;
import org.granite.messaging.jmf.codec.std.ByteCodec;
import org.granite.messaging.jmf.codec.std.CharacterCodec;
import org.granite.messaging.jmf.codec.std.DoubleCodec;
import org.granite.messaging.jmf.codec.std.FloatCodec;
import org.granite.messaging.jmf.codec.std.IntegerCodec;
import org.granite.messaging.jmf.codec.std.LongCodec;
import org.granite.messaging.jmf.codec.std.ShortCodec;
import org.granite.messaging.jmf.codec.std.StringCodec;
import org.granite.messaging.jmf.codec.std.impl.ArrayCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ArrayListCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BigDecimalCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BigIntegerCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BooleanCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ByteCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.CharacterCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.DateCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.DoubleCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.EnumCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.FloatCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.HashMapCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.HashSetCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.IntegerCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.LongCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.NullCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ObjectCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ShortCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.StringCodecImpl;

/**
 * @author Franck WOLFF
 */
public class DefaultCodecRegistry implements CodecRegistry {

	private BooleanCodec booleanCodec;
	private CharacterCodec characterCodec;
	private ByteCodec byteCodec;
	private ShortCodec shortCodec;
	private IntegerCodec integerCodec;
	private LongCodec longCodec;
	private FloatCodec floatCodec;
	private DoubleCodec doubleCodec;
	private StringCodec stringCodec;
	
	private final Map<Integer, StandardCodec<?>> typeToCodec = new HashMap<Integer, StandardCodec<?>>();
	private final Map<Class<?>, StandardCodec<?>> classToCodec = new HashMap<Class<?>, StandardCodec<?>>();
	private final List<ConditionalObjectCodec> conditionalObjectCodecs = new ArrayList<ConditionalObjectCodec>();
	private final Map<Class<?>, PrimitiveFieldCodec> primitiveFieldCodecs = new HashMap<Class<?>, PrimitiveFieldCodec>();

	private final List<ExtendedObjectCodec> extendedCodecs;
	
	public DefaultCodecRegistry() {
		this(new ArrayList<ExtendedObjectCodec>());
	}
		
	public DefaultCodecRegistry(List<ExtendedObjectCodec> extendedCodecs) {
		this.extendedCodecs = extendedCodecs;

		List<StandardCodec<?>> standardCodecs = getStandardCodecs();
		for (StandardCodec<?> codec : standardCodecs) {
			
			if (codec instanceof BijectiveCodec) {
				if (codec instanceof PrimitiveCodec) {
					assertNull(classToCodec.put(((PrimitiveCodec<?>)codec).getPrimitiveClass(), codec));
					assertNull(typeToCodec.put(((PrimitiveCodec<?>)codec).getPrimitiveType(), codec));
					
					switch (((PrimitiveCodec<?>)codec).getPrimitiveType()) {
						case JMF_BOOLEAN: initBooleanCodec((BooleanCodec)codec); break;
						case JMF_CHARACTER: initCharacterCodec((CharacterCodec)codec); break;
						case JMF_BYTE: initByteCodec((ByteCodec)codec); break;
						case JMF_SHORT: initShortCodec((ShortCodec)codec); break;
						case JMF_INTEGER: initIntegerCodec((IntegerCodec)codec); break;
						case JMF_LONG: initLongCodec((LongCodec)codec); break;
						case JMF_FLOAT: initFloatCodec((FloatCodec)codec); break;
						case JMF_DOUBLE: initDoubleCodec((DoubleCodec)codec); break;
					}
				}
				
				assertNull(classToCodec.put(((BijectiveCodec<?>)codec).getObjectClass(), codec));
				assertNull(typeToCodec.put(codec.getObjectType(), codec));
				
				if (codec.getObjectType() == JMF_STRING)
					initStringCodec((StringCodec)codec);
			}
			else if (codec instanceof ConditionalObjectCodec) {
				assertNull(typeToCodec.put(codec.getObjectType(), codec));
				conditionalObjectCodecs.add((ConditionalObjectCodec)codec);
			}
			else
				throw new JMFConfigurationException("Codec must implement BijectiveCodec or ConditionalObjectCodec: " + codec);
		}
		
		checkPrimitiveCodecs();
	}

	public BooleanCodec getBooleanCodec() {
		return booleanCodec;
	}

	public CharacterCodec getCharacterCodec() {
		return characterCodec;
	}

	public ByteCodec getByteCodec() {
		return byteCodec;
	}

	public ShortCodec getShortCodec() {
		return shortCodec;
	}

	public IntegerCodec getIntegerCodec() {
		return integerCodec;
	}

	public LongCodec getLongCodec() {
		return longCodec;
	}

	public FloatCodec getFloatCodec() {
		return floatCodec;
	}

	public DoubleCodec getDoubleCodec() {
		return doubleCodec;
	}

	public StringCodec getStringCodec() {
		return stringCodec;
	}

	@SuppressWarnings("unchecked")
	public <T> StandardCodec<T> getCodec(int jmfType) {
		return (StandardCodec<T>)typeToCodec.get(jmfType);
	}

	@SuppressWarnings("unchecked")
	public <T> StandardCodec<T> getCodec(Class<?> cls) {
		StandardCodec<T> codec = (StandardCodec<T>)classToCodec.get(cls);
		if (codec == null) {
			for (ConditionalObjectCodec condCodec : conditionalObjectCodecs) {
				if (condCodec.accept(cls)) {
					codec = (StandardCodec<T>)condCodec;
					break;
				}
			}
		}
		return codec;
	}

	public ExtendedObjectCodec findExtendedEncoder(Class<?> cls) {
		for (ExtendedObjectCodec c : extendedCodecs) {
			if (c.canEncode(cls))
				return c;
		}
		return null;
	}

	public ExtendedObjectCodec findExtendedDecoder(Class<?> cls) {
		for (ExtendedObjectCodec c : extendedCodecs) {
			if (c.canDecode(cls))
				return c;
		}
		return null;
	}

	public PrimitiveFieldCodec getPrimitiveFieldCodec(Class<?> fieldCls) {
		return primitiveFieldCodecs.get(fieldCls);
	}

	public int extractJmfType(int parameterizedJmfType) {
		if ((parameterizedJmfType & 0x08) == 0x00)
			return (parameterizedJmfType & 0x07);
		if ((parameterizedJmfType & 0x18) == 0x08)
			return (parameterizedJmfType & 0x0F);
		if ((parameterizedJmfType & 0x38) == 0x18)
			return (parameterizedJmfType & 0x1F);
		if ((parameterizedJmfType & 0x78) == 0x38)
			return (parameterizedJmfType & 0x3F);
		return parameterizedJmfType;
	}

	public int jmfTypeOfPrimitiveClass(Class<?> cls) {
		if (!cls.isPrimitive())
			return -1;
		StandardCodec<?> codec = classToCodec.get(cls);
		return (codec instanceof PrimitiveCodec ? ((PrimitiveCodec<?>)codec).getPrimitiveType() : -1);
	}

	public Class<?> primitiveClassOfJmfType(int jmfType) {
		StandardCodec<?> codec = typeToCodec.get(Integer.valueOf(jmfType));
		return (codec instanceof PrimitiveCodec && ((PrimitiveCodec<?>)codec).getPrimitiveType() == jmfType ? ((PrimitiveCodec<?>)codec).getPrimitiveClass() : null);
	}
	
	protected List<StandardCodec<?>> getStandardCodecs() {
		return Arrays.asList((StandardCodec<?>)
			new NullCodecImpl(),
				
			new BooleanCodecImpl(),
			new CharacterCodecImpl(),
			new ByteCodecImpl(),
			new ShortCodecImpl(),
			new IntegerCodecImpl(),
			new LongCodecImpl(),
			new FloatCodecImpl(),
			new DoubleCodecImpl(),

			new BigIntegerCodecImpl(),
			new BigDecimalCodecImpl(),

			new StringCodecImpl(),

			new DateCodecImpl(),

			new ArrayListCodecImpl(),
			new HashSetCodecImpl(),
			new HashMapCodecImpl(),

			new EnumCodecImpl(),
			new ArrayCodecImpl(),
			new ObjectCodecImpl()
		);
	}
	
	private void assertNull(StandardCodec<?> codec) {
		if (codec != null)
			throw new JMFConfigurationException("Codec conflict with: " + codec);
	}
	
	private void checkPrimitiveCodecs() {
		if (booleanCodec == null)
			throw new JMFConfigurationException("No Boolean codec");
		if (characterCodec == null)
			throw new JMFConfigurationException("No Character codec");
		if (byteCodec == null)
			throw new JMFConfigurationException("No Byte codec");
		if (shortCodec == null)
			throw new JMFConfigurationException("No Short codec");
		if (integerCodec == null)
			throw new JMFConfigurationException("No Integer codec");
		if (longCodec == null)
			throw new JMFConfigurationException("No Long codec");
		if (floatCodec == null)
			throw new JMFConfigurationException("No Float codec");
		if (doubleCodec == null)
			throw new JMFConfigurationException("No Double codec");
		if (stringCodec == null)
			throw new JMFConfigurationException("No String codec");
	}

	private void initBooleanCodec(BooleanCodec codec) {
		booleanCodec = codec;
		primitiveFieldCodecs.put(booleanCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				booleanCodec.encodePrimitive(ctx, field.getBoolean(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setBoolean(v, booleanCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initCharacterCodec(CharacterCodec codec) {
		characterCodec = codec;
		primitiveFieldCodecs.put(characterCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				characterCodec.encodePrimitive(ctx, field.getChar(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setChar(v, characterCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initByteCodec(ByteCodec codec) {
		byteCodec = codec;
		primitiveFieldCodecs.put(byteCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				byteCodec.encodePrimitive(ctx, field.getByte(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setByte(v, byteCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initShortCodec(ShortCodec codec) {
		shortCodec = codec;
		primitiveFieldCodecs.put(shortCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				shortCodec.encodePrimitive(ctx, field.getShort(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setShort(v, shortCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initIntegerCodec(IntegerCodec codec) {
		integerCodec = codec;
		primitiveFieldCodecs.put(integerCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				integerCodec.encodePrimitive(ctx, field.getInt(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setInt(v, integerCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initLongCodec(LongCodec codec) {
		longCodec = codec;
		primitiveFieldCodecs.put(longCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				longCodec.encodePrimitive(ctx, field.getLong(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setLong(v, longCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initFloatCodec(FloatCodec codec) {
		floatCodec = codec;
		primitiveFieldCodecs.put(floatCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				floatCodec.encodePrimitive(ctx, field.getFloat(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setFloat(v, floatCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initDoubleCodec(DoubleCodec codec) {
		doubleCodec = codec;
		primitiveFieldCodecs.put(doubleCodec.getPrimitiveClass(), new PrimitiveFieldCodec() {
			public void encodePrimitive(OutputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				doubleCodec.encodePrimitive(ctx, field.getDouble(v));
			}
			public void decodePrimitive(InputContext ctx, Object v, Field field) throws IllegalAccessException, IOException {
				field.setDouble(v, doubleCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initStringCodec(StringCodec codec) {
		stringCodec = codec;
	}
}
