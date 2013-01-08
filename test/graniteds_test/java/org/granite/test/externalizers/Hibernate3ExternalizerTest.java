package org.granite.test.externalizers;

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
import org.junit.Assert;
import org.junit.Test;


public class Hibernate3ExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		return "hibernate";
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
