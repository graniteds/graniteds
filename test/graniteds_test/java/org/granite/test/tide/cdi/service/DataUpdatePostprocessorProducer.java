package org.granite.test.tide.cdi.service;

import javax.enterprise.inject.Produces;
import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.test.tide.TestDataUpdatePostprocessor;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataUpdatePostprocessor;


@DataEnabled(topic="testTopic")
public class DataUpdatePostprocessorProducer {
		
	@Produces
	public DataUpdatePostprocessor initDupp() {
        ServletContext sc = ((HttpGraniteContext)GraniteContext.getCurrentInstance()).getServletContext();
        if (sc.getAttribute("dupp") != null)
        	return new TestDataUpdatePostprocessor();
        return null;
	}
}
