package org.granite.test.externalizers;

import java.util.HashMap;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.junit.After;
import org.junit.Before;


public class AbstractExternalizerTest {

	@Before
	public void before() throws Exception {
		GraniteConfig graniteConfig = new GraniteConfig(null, null, null, null);
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, new HashMap<String, Object>());
	}
	
	@After
	public void after() throws Exception {
		GraniteContext.release();
	}
}
