package org.granite.test.externalizers;

import java.util.Properties;

import org.hibernate.Hibernate;
import org.junit.Assert;


public class Hibernate4ExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		return "hibernate4";
	}
	
	@Override
	protected void providerSpecificAsserts(Entity2 obj) {
		Assert.assertFalse("Entity3 set lazy", Hibernate.isInitialized(((Entity2)obj).getEntity().getEntities()));
	}
	
}
