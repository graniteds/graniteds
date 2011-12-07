package org.granite.test.externalizers;

import java.util.Properties;


public class Hibernate3ExternalizerTest extends AbstractJPAExternalizerTest {
	
	@Override
	protected String setProperties(Properties props) {
		return "hibernate";
	}
	
}
