package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDeserializer;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFHashMap implements JMFConstants {
	
	private CodecRegistry codecRegistry;
	
	@Before
	public void before() {
		codecRegistry = new DefaultCodecRegistry();
	}
	
	@After
	public void after() {
		codecRegistry = null;
	}

	@Test
	public void testHashMap() throws ClassNotFoundException, IOException {
		
		HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put(null, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put(Boolean.TRUE, Boolean.FALSE);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		Assert.assertEquals(hashMap, serializeDeserialize(hashMap));
		
		hashMap = new HashMap<Object, Object>();
		hashMap.put("John", "Doe");
		hashMap.put(Boolean.FALSE, null);
		hashMap.put(Byte.valueOf(Byte.MAX_VALUE), Byte.valueOf((byte)0));
		hashMap.put(null, hashMap);
		int[] ints = new int[] {1,2,3,4,5,6};
		short[] shorts = new short[] {7,8,9,10};
		hashMap.put(ints, shorts);
		
//		byte[] bytes = Util.serializeJava(hashMap);
//		System.out.println("Serialization Java: " + bytes.length + "B.");
		
		HashMap<?, ?> clone = serializeDeserialize(hashMap);
		Assert.assertEquals(hashMap.size(), clone.size());
		Assert.assertTrue(clone.containsKey("John"));
		Assert.assertTrue("Doe".equals(clone.get("John")));
		Assert.assertTrue(clone.containsKey(Byte.valueOf(Byte.MAX_VALUE)));
		Assert.assertTrue(Byte.valueOf((byte)0).equals(clone.get(Byte.valueOf(Byte.MAX_VALUE))));
		
		boolean foundArray = false;
		boolean foundMap = false;
		for (Map.Entry<?, ?> o : clone.entrySet()) {
			if (o.getKey() == null) {
				foundMap = true;
				Assert.assertTrue(o.getValue() == clone);
			}
			else if (o.getKey().getClass().isArray() && o.getKey() instanceof int[]) {
				foundArray = true;
				Assert.assertTrue(Arrays.equals(ints, (int[])o.getKey()));
				Assert.assertTrue(Arrays.equals(shorts, (short[])o.getValue()));
			}
		}
		Assert.assertTrue(foundArray && foundMap);
	}
	
	private HashMap<?, ?> serializeDeserialize(HashMap<?, ?> v) throws ClassNotFoundException, IOException {
		return serializeDeserialize(v, false);
	}
	
	private HashMap<?, ?> serializeDeserialize(HashMap<?, ?> v, boolean dump) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		PrintStream ps = TestUtil.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + TestUtil.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		HashMap<?, ?> clone = (HashMap<?, ?>)deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
