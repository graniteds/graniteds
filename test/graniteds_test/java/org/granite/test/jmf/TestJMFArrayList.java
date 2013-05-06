package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

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

public class TestJMFArrayList implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testArrayList() throws ClassNotFoundException, IOException {
		
		ArrayList<?> arrayList = new ArrayList<Object>();
		Assert.assertEquals(arrayList, serializeDeserialize(arrayList));
		
		arrayList = new ArrayList<Object>(Arrays.asList(new Object[]{Boolean.TRUE}));
		Assert.assertEquals(arrayList, serializeDeserialize(arrayList));
		
		arrayList = new ArrayList<Boolean>(Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.TRUE, Boolean.FALSE}));
		Assert.assertEquals(arrayList, serializeDeserialize(arrayList));
		
		arrayList = new ArrayList<Object>(Arrays.asList(new Object[]{Boolean.TRUE, Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0), Double.NaN}));
		Assert.assertEquals(arrayList, serializeDeserialize(arrayList));
		
		arrayList = new ArrayList<Object>(Arrays.asList(new Object[]{Boolean.TRUE, Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0), null, Double.NaN}));
		((ArrayList<Object>)arrayList).add(arrayList);
		((ArrayList<Object>)arrayList).add(new int[] {1,2,3,4,5,6});
		((ArrayList<Object>)arrayList).add(arrayList);
		ArrayList<?> clone = serializeDeserialize(arrayList);
		Assert.assertTrue(arrayList.get(0).equals(clone.get(0)));
		Assert.assertTrue(arrayList.get(1).equals(clone.get(1)));
		Assert.assertTrue(arrayList.get(2).equals(clone.get(2)));
		Assert.assertTrue(arrayList.get(3) == null && clone.get(3) == null);
		Assert.assertTrue(arrayList.get(4).equals(clone.get(4)));
		Assert.assertTrue(clone.get(5) == clone);
		Assert.assertTrue(Arrays.equals((int[])arrayList.get(6), (int[])clone.get(6)));
		Assert.assertTrue(clone.get(7) == clone);
	}
	
	private ArrayList<?> serializeDeserialize(ArrayList<?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private ArrayList<?> serializeDeserialize(ArrayList<?> v, boolean dump) throws ClassNotFoundException, IOException {
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
		ArrayList<?> clone = (ArrayList<?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
