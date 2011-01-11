/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.logging.Logger;
import org.granite.osgi.adaptor.AMFServiceAdaptor;
import org.granite.osgi.constants.OSGIConstants;
import org.granite.osgi.metadata.ManifestMetadataParser;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public class Activator implements BundleActivator {

	private static final Logger log=Logger.getLogger(Activator.class);
	private static final String DEFAULT_CONTEXT_PATH="/WebContent";
	static ServiceTracker configurationTracker;

	String contextPath;
	ServiceRegistration configRegistration;
	ServiceTracker httpServiceTracker;
	ManifestMetadataParser metaParser;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		contextPath=(String) context.getBundle().getHeaders().get(OSGIConstants.GDS_CONTEXT);
		contextPath=(contextPath==null?DEFAULT_CONTEXT_PATH:contextPath);
		if(!contextPath.startsWith("/")){
			contextPath="/"+contextPath;
		}
		Configuration cfg=new ConfigurationImpl();
		//provide a service for other bundle to override the config files
		configRegistration=context.registerService(Configuration.class.getName(),cfg, null);
		configurationTracker=new ServiceTracker(context,Configuration.class.getName(),null);
		configurationTracker.open();
		//set default config files
		setupDefaultConfigurations(cfg);
		
		//track the AMFServiceAdaptor
		httpServiceTracker=new HttpServiceTracker(context);
		httpServiceTracker.open();
		//begin to parse Metadata for all bundle in the OSGi Container
		metaParser=new ManifestMetadataParser(context);
		metaParser.start();
	}
	/**
	 * 
	 * @param cfg
	 */
	private void setupDefaultConfigurations(Configuration cfg){
		cfg.setFlexServicesConfig(OSGIConstants.DEFAULT_FLEX_CONFIG);
		cfg.setGraniteConfig(OSGIConstants.DEFAULT_GRANITEDS_CONFIG);
	}
	/**
	 * @return Configuration
	 */
	public static Configuration getConfigurationService(){
		return (Configuration) configurationTracker.getService();
	}
	/**
	 * Register AMFServiceAdaptor
	 * HttpService Tracker
	 */
	private class HttpServiceTracker extends ServiceTracker {
		String amfServicServleteAlias=contextPath+"/graniteamf/amf";
		public HttpServiceTracker(BundleContext context) { 
			super(context, HttpService.class.getName(), null);
		}
		/*
		 * (non-Javadoc)
		 * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
		 */
		@Override
		public Object addingService(ServiceReference reference) {
			final HttpService httpService = (HttpService) context.getService(reference);
			try {
				Dictionary<String, Object> initparams = new Hashtable<String, Object>();
			    initparams.put("servlet-name", "AMFServiceServlet");
			    httpService.registerServlet(amfServicServleteAlias,new AMFServiceAdaptor(context), initparams, httpService.createDefaultHttpContext()); 
			} catch (Exception e) {
				log.error(e, "Could not add service");
			}
			return httpService;
		}
		/*
		 * (non-Javadoc)
		 * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
		 */
		@Override
		public void removedService(ServiceReference reference, Object service) {
			final HttpService httpService = (HttpService) service;
			httpService.unregister(amfServicServleteAlias);
			super.removedService(reference, service);
		}
	} 
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if(configRegistration!=null){
			configRegistration.unregister();
			configRegistration=null;
		}
		if(configurationTracker!=null){
			configurationTracker.close();
			configurationTracker=null;
		}
		if(httpServiceTracker!=null){
			httpServiceTracker.close();
			httpServiceTracker=null;
		}
		if(metaParser!=null){
			metaParser.stop();
			metaParser=null;
		}
	}
}
