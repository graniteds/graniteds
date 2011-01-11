package org.granite.test.externalizers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.granite.context.GraniteContext;
import org.junit.Assert;
import org.junit.Test;


public class TypedMapTest extends AbstractExternalizerTest {

	@Test
	public void testMap() throws Exception {
		GraniteContext gc = GraniteContext.getCurrentInstance();
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("toto", new String[] { "tutu", "titi" });
		map.put("tata", new String[] { "tata" });
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(map);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(bais);
		Object obj = in.readObject();
		Assert.assertEquals("Map size", 2, ((Map<?, ?>)obj).size());
		Assert.assertTrue("Map type", ((Map<?, ?>)obj).get("toto").getClass().getComponentType().equals(String.class));
	}
}
