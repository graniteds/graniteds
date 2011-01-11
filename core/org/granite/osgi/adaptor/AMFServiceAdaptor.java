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
package org.granite.osgi.adaptor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.logging.Logger;
import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;
import org.granite.messaging.amf.process.AMF0MessageProcessor;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.osgi.Activator;
import org.granite.osgi.constants.OSGIConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public class AMFServiceAdaptor extends HttpServlet {

	private static final long serialVersionUID = 4777538296260511097L;
	private static final Logger log=Logger.getLogger(AMFServiceAdaptor.class);
	
	private GraniteConfig graniteConfig = null;
	private ServicesConfig servicesConfig = null;
	BundleContext context;
	
	public AMFServiceAdaptor(BundleContext context){
		this.context=context;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) {
		try {
			super.init(config);
			Configuration configuration = Activator.getConfigurationService();
			getServletContext().setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, configuration);
			graniteConfig = ServletGraniteConfig.loadConfig(getServletContext());
			servicesConfig = ServletServicesConfig.loadConfig(getServletContext());
			
			//register EventHandler ServicesConfig handle Add or Remove dataservice
			Dictionary<String, Object> properties = new Hashtable<String, Object>();
			String[] topics = new String[] { OSGIConstants.TOPIC_GDS_ADD_SERVICE,OSGIConstants.TOPIC_GDS_REMOVE_SERVICE};
			properties.put(EventConstants.EVENT_TOPIC, topics);
			context.registerService(EventHandler.class.getName(), new ServiceEventHandler(servicesConfig), properties);
			
		} catch (ServletException e) {
			log.error(e, "Could initialize OSGi service adaptor");
		}
	}
	
	public ServicesConfig getServicesConfig(){
		 return servicesConfig;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if(log.isInfoEnabled())
		try {
			GraniteContext context = HttpGraniteContext.createThreadIntance(
					graniteConfig, servicesConfig, getServletContext(),request,response);
			if (context == null)
				throw new ServletException("GraniteContext not Initialized!!");
			 
			//AMFContextImpl amf = (AMFContextImpl) context.getAMFContext();
			//Phase1 Deserializing AMF0 request
			if(log.isInfoEnabled())	
				log.info(">>>>> Deserializing AMF0 request from..."+request.getRequestURI());
			AMF0Deserializer deserializer = new AMF0Deserializer(
					new DataInputStream(request.getInputStream()));
			AMF0Message amf0Request = deserializer.getAMFMessage();
			
			//Phase2 Processing AMF0 request 
			if(log.isInfoEnabled())
				log.info(">>>>> Processing AMF0 request: " + amf0Request);
			AMF0Message amf0Response = AMF0MessageProcessor.process(amf0Request);
			if(log.isInfoEnabled())
				log.info("<<<<< Returning AMF0 response: " + amf0Response);

			//Phase3 Send back response to the client
			response.setContentType(AMF0Message.CONTENT_TYPE);
			AMF0Serializer serializer = new AMF0Serializer(new DataOutputStream(response.getOutputStream()));
			serializer.serializeMessage(amf0Response);
			if(log.isInfoEnabled())
				log.info("...End of Processing AMF Request......");
		} 
		catch (Exception e) {
			log.error(e, "Could not handle AMF request");
			throw new ServletException(e);
		}
	}

}
