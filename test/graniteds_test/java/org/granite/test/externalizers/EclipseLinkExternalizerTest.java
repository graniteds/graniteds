package org.granite.test.externalizers;

import java.util.Properties;


public class EclipseLinkExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		props.put("eclipselink.ddl-generation", "create-tables");
		props.put("eclipselink.ddl-generation.output-mode", "database");
		return "eclipselink";
	}
	
}
