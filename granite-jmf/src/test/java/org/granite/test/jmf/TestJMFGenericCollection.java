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
package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.Vector;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.test.jmf.Util.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFGenericCollection implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}
	
	static enum TestEnum {
		ONE,
		TWO,
		THREE,
		FOUR,
		FIVE,
		SIX,
		SEVEN,
		EIGHT,
		NINE,
		TEN
	};

	@SuppressWarnings("unchecked")
	@Test
	public void testGenericCollection() throws ClassNotFoundException, IOException {
		
		Collection<?> collection = new LinkedHashSet<Object>();
		Collection<?> clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new LinkedHashSet<Object>(Arrays.asList(new Object[]{Boolean.TRUE}));
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new LinkedHashSet<Object>(Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.TRUE, Boolean.FALSE}));
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);

		collection = new LinkedHashSet<Object>(Arrays.asList(new Object[]{
			Boolean.TRUE,
			Byte.valueOf(Byte.MAX_VALUE),
			Byte.valueOf((byte)0),
			null,
			Double.NaN,
			"Bla bla"
		}));
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new Stack<Object>();
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new Stack<Object>();
		((Stack<Object>)collection).push(Boolean.TRUE);
		((Stack<Object>)collection).push(Boolean.TRUE);
		((Stack<Object>)collection).push(Boolean.FALSE);
		((Stack<Object>)collection).push("bla");
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new Vector<Object>();
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = new Vector<Object>(Arrays.asList(new Object[]{
			Boolean.TRUE,
			Byte.valueOf(Byte.MAX_VALUE),
			Byte.valueOf((byte)0),
			null,
			Double.NaN,
			"Bla bla"
		}));
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		// Use standard object codec with writeReplace / readResolve...
		collection = EnumSet.noneOf(TestEnum.class);
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
		
		collection = EnumSet.allOf(TestEnum.class);
		clone = serializeDeserialize(collection);
		Assert.assertEquals(collection.getClass(), clone.getClass());
		Assert.assertEquals(collection, clone);
	}
	
	private Collection<?> serializeDeserialize(Collection<?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private Collection<?> serializeDeserialize(Collection<?> v, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = Util.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Collection<?> clone = (Collection<?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
