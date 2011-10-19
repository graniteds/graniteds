package org.granite.test.externalizers;

import java.util.Properties;

import org.eclipse.persistence.indirection.IndirectSet;
import org.junit.Assert;


public class EclipseLinkExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		props.put("eclipselink.ddl-generation", "create-tables");
		props.put("eclipselink.ddl-generation.output-mode", "database");
		return "eclipselink";
	}
	
	@Override
	protected void providerSpecificAsserts(Entity2 obj) {
		Assert.assertTrue("Entity3 set lazy", obj.getEntity().getEntities() instanceof IndirectSet);
	}
	
}
