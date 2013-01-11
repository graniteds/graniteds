package org.granite.test.config;

import java.util.Date;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.test.tide.MockServletContext;
import org.junit.Assert;
import org.junit.Test;

public class GraniteConfigTest {
	
    @Test
    public void testOverrideDefaultConverters() throws Exception {
    	ServletContext servletContext = new MockServletContext();
        Configuration cfg = new ConfigurationImpl();
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-override-converters.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        
        Date dt = new Date();
        Object converted = graniteConfig.getConverters().convert(dt, Date.class);
        Assert.assertTrue("Converted to Long", converted instanceof Long);
    }

}
