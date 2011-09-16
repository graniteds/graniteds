package org.granite.test.externalizers;

import java.util.Properties;

import org.junit.Assert;


public class OpenJpaExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		// Does not work with H2 ???
		props.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
		props.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
		return "openjpa";
	}
	
	@Override
	protected void providerSpecificAsserts(Entity2 obj) {
		Assert.assertNull("Entity2 contacts not loaded", ((Entity2)obj).getEntity().getEntities());
	}
	
}
