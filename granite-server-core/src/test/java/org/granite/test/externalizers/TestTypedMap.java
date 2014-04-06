/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.externalizers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.granite.config.AMF3Config;
import org.granite.context.GraniteContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class TestTypedMap extends AbstractTestExternalizer {

	@Ignore("TODO check map")
	@Test
	public void testMap() throws Exception {
		GraniteContext gc = GraniteContext.getCurrentInstance();
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("toto", new String[] { "tutu", "titi" });
		map.put("tata", new String[] { "tata" });
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
		ObjectOutput out = ((AMF3Config)gc.getGraniteConfig()).newAMF3Serializer(baos);
		out.writeObject(map);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInput in = ((AMF3Config)gc.getGraniteConfig()).newAMF3Deserializer(bais);
		Object obj = in.readObject();
		Assert.assertEquals("Map size", 2, ((Map<?, ?>)obj).size());
		Assert.assertTrue("Map type", ((Map<?, ?>)obj).get("toto").getClass().getComponentType().equals(String.class));
	}
}
