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


public class TypedMapPropertyTest extends AbstractExternalizerTest {

	@Test
	public void testMap() throws Exception {
		GraniteContext gc = GraniteContext.getCurrentInstance();
		
		Entity1 entity = new Entity1();
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("toto", new String[] { "tutu", "titi" });
		map.put("tata", new String[] { "tata" });
		entity.setMap(map);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
		ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
		out.writeObject(entity);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(bais);
		Object obj = in.readObject();
		Assert.assertTrue("Entity1", obj instanceof Entity1);
		Assert.assertEquals("Entity map", 2, ((Entity1)obj).getMap().size());
		Assert.assertTrue("Entity map type", ((Entity1)obj).getMap().get("toto").getClass().getComponentType().equals(String.class));
	}
}
