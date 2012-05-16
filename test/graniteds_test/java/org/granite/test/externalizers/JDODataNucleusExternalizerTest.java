package org.granite.test.externalizers;

import java.util.Properties;


public class JDODataNucleusExternalizerTest extends AbstractJDOExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		props.put("datanucleus.ConnectionDriverName", "org.h2.Driver");
		props.put("datanucleus.ConnectionURL", "jdbc:h2:mem:testdb");
		props.put("datanucleus.ConnectionUserName", "sa");
		props.put("datanucleus.ConnectionPassword", "");
		props.put("datanucleus.autoCreateTables", "true");
		props.put("datanucleus.autoCreateColumns", "true");
		props.put("datanucleus.storeManagerType", "rdbms");
		props.put("datanucleus.DetachAllOnCommit", "true");
		return "datanucleus-jdo";
	}
	
//	@Test
//	@Ignore("DataNucleus does not work with this case, embedded objects detachedState is not serialized")
//	@Override
//	public void testSerializationLazyEmbeddedGDS838() throws Exception {		
//	}
}
