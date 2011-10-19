package org.granite.test.externalizers;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;


public class DataNucleusExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		return "datanucleus";
	}
	
	@Test
	@Ignore("DataNucleus does not work with this case")
	@Override
	public void testSerializationLazyEmbeddedGDS838() throws Exception {		
	}
}
