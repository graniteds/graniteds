/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.hibernate4.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.externalizers.AbstractTestJPAExternalizer;
import org.granite.test.externalizers.Entity7;
import org.granite.test.externalizers.Entity8;
import org.junit.Assert;
import org.junit.Test;


public class TestHibernate4Externalizer extends AbstractTestJPAExternalizer {
	
	@Override
	protected String setProperties(Properties props) {
		return "hibernate4";
	}
    
    @Test
    public void testSerializationProxy3() throws Exception {
        GraniteContext gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction et = entityManager.getTransaction();
        et.begin();
        
        Entity8 e8 = new Entity8();
        e8.setName("Test");
        Entity7 e7 = new Entity7();
        e7.setName("Test");
        e7.setEntity8(e8);
        entityManager.persist(e7);
        
        entityManager.flush();
        et.commit();
        entityManager.close();
        
        entityManager = entityManagerFactory.createEntityManager();     
        et = entityManager.getTransaction();
        et.begin();
        
        e7 = entityManager.find(Entity7.class, e7.getId());
        
        entityManager.flush();
        et.commit();
        entityManager.close();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
        ObjectOutput out = gc.getGraniteConfig().newAMF3Serializer(baos);
        out.writeObject(e7);        
        GraniteContext.release();
        
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        gc = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
        ObjectInput in = gc.getGraniteConfig().newAMF3Deserializer(is);
        Object obj = in.readObject();
        GraniteContext.release();
        
        Assert.assertTrue("Entity7", obj instanceof Entity7);
        e7 = (Entity7)obj;
        Long e8id = e7.getEntity8().getId();
        Assert.assertEquals("Entity8 id", e8.getId(), e8id);
    }
}
