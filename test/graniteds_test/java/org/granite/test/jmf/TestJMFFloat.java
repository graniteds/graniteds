package org.granite.test.jmf;

import static org.granite.test.jmf.TestUtil.bytes;
import static org.granite.test.jmf.TestUtil.toHexString;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.JMFConstants;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDeserializer;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJMFFloat implements JMFConstants {
	
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
	public void testSomeFloat() throws IOException {

		checkFloat(Float.NaN, 						bytes( JMF_FLOAT, 0x00, 0x00, 0xC0, 0x7F ));
		checkFloat(Float.MAX_VALUE, 				bytes( JMF_FLOAT, 0xFF, 0xFF, 0x7F, 0x7F ));
		checkFloat(Float.MIN_VALUE, 				bytes( JMF_FLOAT, 0x01, 0x00, 0x00, 0x00 ));
		checkFloat(Float.MIN_NORMAL, 				bytes( JMF_FLOAT, 0x00, 0x00, 0x80, 0x00 ));
		checkFloat(Float.NEGATIVE_INFINITY, 		bytes( JMF_FLOAT, 0x00, 0x00, 0x80, 0xFF ));
		checkFloat(Float.POSITIVE_INFINITY, 		bytes( JMF_FLOAT, 0x00, 0x00, 0x80, 0x7F ));

		checkFloat((float)Math.PI, 					bytes( JMF_FLOAT, 0xDB, 0x0F, 0x49, 0x40 ));
		checkFloat((float)-Math.PI, 				bytes( JMF_FLOAT, 0xDB, 0x0F, 0x49, 0xC0 ));
		checkFloat((float)Math.E, 					bytes( JMF_FLOAT, 0x54, 0xF8, 0x2D, 0x40 ));
		checkFloat((float)-Math.E, 					bytes( JMF_FLOAT, 0x54, 0xF8, 0x2D, 0xC0 ));

		checkFloat((float)-1.0000004, 				bytes( JMF_FLOAT, 0x03, 0x00, 0x80, 0xBF ));
		checkFloat((float)-1.0000003, 				bytes( JMF_FLOAT, 0x03, 0x00, 0x80, 0xBF ));
		checkFloat((float)-1.0000002, 				bytes( JMF_FLOAT, 0x02, 0x00, 0x80, 0xBF ));
		checkFloat((float)-1.0000001, 				bytes( JMF_FLOAT, 0x01, 0x00, 0x80, 0xBF ));
		checkFloat((float)-1.0, 					bytes( JMF_FLOAT, 0x00, 0x00, 0x80, 0xBF ));
		
		checkFloat((float)-0.0,						bytes( JMF_FLOAT, 0x00, 0x00, 0x00, 0x80 ));
		checkFloat((float)0.0,						bytes( JMF_FLOAT, 0x00, 0x00, 0x00, 0x00 ));
		
		checkFloat((float)1.0, 						bytes( JMF_FLOAT, 0x00, 0x00, 0x80, 0x3F ));
		checkFloat((float)1.0000001, 				bytes( JMF_FLOAT, 0x01, 0x00, 0x80, 0x3F ));
		checkFloat((float)1.0000002, 				bytes( JMF_FLOAT, 0x02, 0x00, 0x80, 0x3F ));
		checkFloat((float)1.0000003, 				bytes( JMF_FLOAT, 0x03, 0x00, 0x80, 0x3F ));
		checkFloat((float)1.0000004, 				bytes( JMF_FLOAT, 0x03, 0x00, 0x80, 0x3F ));
		
		for (float d = -1000.0F; d < 1000.0F; d++)
			checkFloat(d);
		
		for (float d = -10.0002F; d < 11.0F; d += 0.01)
			checkFloat(d);
	}
	
	@Test
	public void testSomeFloatObject() throws ClassNotFoundException, IOException {

		checkFloatObject(null,							bytes( JMF_NULL ));
		
		checkFloatObject(Float.NaN, 					bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0xC0, 0x7F ));
		checkFloatObject(Float.MAX_VALUE, 				bytes( JMF_FLOAT_OBJECT, 0xFF, 0xFF, 0x7F, 0x7F ));
		checkFloatObject(Float.MIN_VALUE, 				bytes( JMF_FLOAT_OBJECT, 0x01, 0x00, 0x00, 0x00 ));
		checkFloatObject(Float.MIN_NORMAL, 				bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x80, 0x00 ));
		checkFloatObject(Float.NEGATIVE_INFINITY, 		bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x80, 0xFF ));
		checkFloatObject(Float.POSITIVE_INFINITY, 		bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x80, 0x7F ));

		checkFloatObject((float)Math.PI, 				bytes( JMF_FLOAT_OBJECT, 0xDB, 0x0F, 0x49, 0x40 ));
		checkFloatObject((float)-Math.PI, 				bytes( JMF_FLOAT_OBJECT, 0xDB, 0x0F, 0x49, 0xC0 ));
		checkFloatObject((float)Math.E, 				bytes( JMF_FLOAT_OBJECT, 0x54, 0xF8, 0x2D, 0x40 ));
		checkFloatObject((float)-Math.E, 				bytes( JMF_FLOAT_OBJECT, 0x54, 0xF8, 0x2D, 0xC0 ));

		checkFloatObject((float)-1.0000004, 			bytes( JMF_FLOAT_OBJECT, 0x03, 0x00, 0x80, 0xBF ));
		checkFloatObject((float)-1.0000003, 			bytes( JMF_FLOAT_OBJECT, 0x03, 0x00, 0x80, 0xBF ));
		checkFloatObject((float)-1.0000002, 			bytes( JMF_FLOAT_OBJECT, 0x02, 0x00, 0x80, 0xBF ));
		checkFloatObject((float)-1.0000001, 			bytes( JMF_FLOAT_OBJECT, 0x01, 0x00, 0x80, 0xBF ));
		checkFloatObject((float)-1.0, 					bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x80, 0xBF ));
		
		checkFloatObject((float)-0.0,					bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x00, 0x80 ));
		checkFloatObject((float)0.0,					bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x00, 0x00 ));
		
		checkFloatObject((float)1.0, 					bytes( JMF_FLOAT_OBJECT, 0x00, 0x00, 0x80, 0x3F ));
		checkFloatObject((float)1.0000001, 				bytes( JMF_FLOAT_OBJECT, 0x01, 0x00, 0x80, 0x3F ));
		checkFloatObject((float)1.0000002, 				bytes( JMF_FLOAT_OBJECT, 0x02, 0x00, 0x80, 0x3F ));
		checkFloatObject((float)1.0000003, 				bytes( JMF_FLOAT_OBJECT, 0x03, 0x00, 0x80, 0x3F ));
		checkFloatObject((float)1.0000004, 				bytes( JMF_FLOAT_OBJECT, 0x03, 0x00, 0x80, 0x3F ));
		
		for (float d = -1000.0F; d < 1000.0F; d++)
			checkFloatObject(d);
		
		for (float d = -10.0002F; d < 11.0F; d += 0.01)
			checkFloatObject(d);
	}

	private void checkFloat(float v) throws IOException {
		checkFloat(v, null);
	}
	
	private void checkFloat(float v, byte[] expected) throws IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeFloat(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(String.format(" for 0x%08X (%a)", Float.floatToIntBits(v), v));
			
			fail(sb.toString());
		}
		
		PrintStream ps = TestUtil.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		float u = deserializer.readFloat();
		deserializer.close();
		
		if (Float.compare(v, u) != 0) {
			StringBuilder sb = new StringBuilder(
					String.format("0x%08X (%a) != 0x%08X (%a) ",
					Float.floatToIntBits(v), v, Float.floatToIntBits(u), u)
				)
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}

	private void checkFloatObject(Float v) throws ClassNotFoundException, IOException {
		checkFloatObject(v, null);
	}
	
	private void checkFloatObject(Float v, byte[] expected) throws ClassNotFoundException, IOException {
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(codecRegistry);
		serializer.writeObject(v);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		if (expected != null && !Arrays.equals(bytes, expected)) {
			StringBuilder sb = new StringBuilder("Expected ")
				.append(toHexString(expected))
				.append(" != ")
				.append(toHexString(bytes))
				.append(" for ")
				.append(v);
			
			fail(sb.toString());
		}
		
		PrintStream ps = TestUtil.newNullPrintStream();
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, codecRegistry, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, codecRegistry);
		Object u = deserializer.readObject();
		deserializer.close();
		
		if (!(u instanceof Float || u == null))
			fail("u isn't a Float or null: " + u);
		
		if ((v != null && !v.equals(u)) || (v == null && u != null)) {
			StringBuilder sb = new StringBuilder(String.format("%s != %s", v, u))
				.append(toHexString(bytes));
			
			fail(sb.toString());
		}
	}
}
