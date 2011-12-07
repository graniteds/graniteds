package org.granite.test.externalizers;

import java.util.Properties;


public class OpenJpaExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		// Does not work with H2 ???
		props.put("javax.persistence.jdbc.driver", "org.hsqldb.jdbcDriver");
		props.put("javax.persistence.jdbc.url", "jdbc:hsqldb:mem:testdb");
		return "openjpa";
	}
	
}
