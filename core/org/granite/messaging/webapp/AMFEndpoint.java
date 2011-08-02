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

package org.granite.messaging.webapp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;
import org.granite.messaging.amf.process.AMF0MessageProcessor;

/**
 * @author Franck WOLFF
 */
public class AMFEndpoint {

    private static final Logger log = Logger.getLogger(AMFEndpoint.class);
	
    public static void service(GraniteConfig graniteConfig, ServicesConfig servicesConfig, ServletContext context,
    		HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {	    
	    log.debug(">> Incoming AMF0 request from: %s", request.getRequestURL());

	    InputStream is = null;
	    OutputStream os = null;
	    
	    try {
    		is = new BufferedInputStream(request.getInputStream());
	    	
	        HttpGraniteContext.createThreadIntance(
	            graniteConfig, servicesConfig, context,
	            request, response
	        );
	
	        log.debug(">> Deserializing AMF0 request...");
	
	        AMF0Deserializer deserializer = new AMF0Deserializer(is);
	        AMF0Message amf0Request = deserializer.getAMFMessage();

            log.debug(">> Processing AMF0 request: %s", amf0Request);

            AMF0Message amf0Response = AMF0MessageProcessor.process(amf0Request);
	
	        log.debug("<< Serializing AMF0 response: %s", amf0Response);
	
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.setContentType(AMF0Message.CONTENT_TYPE);
	        response.setDateHeader("Expire", 0L);
	        response.setHeader("Cache-Control", "no-store");
	        
	        os = response.getOutputStream();
	        AMF0Serializer serializer = new AMF0Serializer(os);
	        
	        serializer.serializeMessage(amf0Response);
	        
	        response.flushBuffer();
	    }
	    catch (IOException e) {
	    	if ("org.apache.catalina.connector.ClientAbortException".equals(e.getClass().getName()))
	    		log.debug(e, "Connection closed by client");
	    	else
	    		log.error(e, "AMF message error");
	        throw e;
	    }
	    catch (Exception e) {
	        log.error(e, "AMF message error");
	        throw new ServletException(e);
	    }
	    finally {
	        GraniteContext.release();
	    }
	}
}
