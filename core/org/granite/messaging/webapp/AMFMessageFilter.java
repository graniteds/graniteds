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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfig;
import org.granite.config.GraniteConfigListener;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.AMFContextImpl;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.io.AMF0Deserializer;
import org.granite.messaging.amf.io.AMF0Serializer;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.util.ContentType;
import org.granite.util.ServletParams;

/**
 * @author Franck WOLFF
 */
public class AMFMessageFilter implements Filter {

    private static final Logger log = Logger.getLogger(AMFMessageFilter.class);
    
    protected FilterConfig config = null;
    protected GraniteConfig graniteConfig = null;
    protected ServicesConfig servicesConfig = null;
    
    protected Integer inputBufferSize = null;
    protected Integer outputBufferSize = null;
    protected boolean closeStreams = true;
    
    protected SharedContext jmfSharedContext = null;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.graniteConfig = ServletGraniteConfig.loadConfig(config.getServletContext());
        this.servicesConfig = ServletServicesConfig.loadConfig(config.getServletContext());
        
        closeStreams = ServletParams.get(config, "closeStreams", Boolean.TYPE, true);
        inputBufferSize = ServletParams.get(config, "inputBufferSize", Integer.TYPE, null);
        outputBufferSize = ServletParams.get(config, "outputBufferSize", Integer.TYPE, null);
        
        if (inputBufferSize != null && inputBufferSize <= 0)
        	throw new ServletException("Illegal value for inputBufferSize=" + inputBufferSize + " (should be > 0, fix your web.xml)");
        if (outputBufferSize != null && outputBufferSize <= 0)
        	throw new ServletException("Illegal value for outputBufferSize=" + outputBufferSize + " (should be > 0, fix your web.xml)");
        
        log.info("Using configuration: {closeStreams=%s, inputBufferSize=%s, outputBufferSize=%s}", closeStreams, inputBufferSize, outputBufferSize);
        
        jmfSharedContext = GraniteConfigListener.getSharedContext(config.getServletContext());
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        if (!(req instanceof HttpServletRequest) || !(resp instanceof HttpServletResponse))
            throw new ServletException("Not in HTTP context: " + req + ", " + resp);

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
        
        if (ContentType.JMF_AMF.mimeType().equals(request.getContentType()))
        	doJMFAMFFilter(request, response, chain);
        else
        	doAMFFilter(request, response, chain);
    }
    
    protected void doAMFFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        
        log.debug(">> Incoming AMF0 request from: %s", request.getRequestURL());

        InputStream is = null;
        OutputStream os = null;
        
        try {
        	if (inputBufferSize != null)
        		is = new BufferedInputStream(request.getInputStream(), inputBufferSize);
        	else
        		is = request.getInputStream();
        	
            GraniteContext context = HttpGraniteContext.createThreadIntance(
                graniteConfig, servicesConfig, config.getServletContext(),
                request, response
            );

            AMFContextImpl amf = (AMFContextImpl)context.getAMFContext();

            log.debug(">> Deserializing AMF0 request...");

            AMF0Deserializer deserializer = new AMF0Deserializer(is);
            AMF0Message amf0Request = deserializer.getAMFMessage();

            amf.setAmf0Request(amf0Request);

            log.debug(">> Chaining AMF0 request: %s", amf0Request);

            chain.doFilter(request, response);

            AMF0Message amf0Response = amf.getAmf0Response();

            log.debug("<< Serializing AMF0 response: %s", amf0Response);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(ContentType.AMF.mimeType());
	        response.setDateHeader("Expire", 0L);
	        response.setHeader("Cache-Control", "no-store");
            
	        if (outputBufferSize != null)
	        	response.setBufferSize(outputBufferSize);
            
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
        	
        	if (closeStreams) {
	        	if (is != null) {
	        		try {
	        			is.close();
	        		} catch (IOException e) {
	        			log.error(e, "Error while closing request input stream");
	        		}
	        	}
	        	
	        	if (os != null) {
	        		try {
	        			os.close();
	        		} catch (IOException e) {
	        			log.error(e, "Error while closing response output stream");
	        		}
	        	}
        	}
        	
            GraniteContext.release();
        }
    }
    
    protected void doJMFAMFFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        
    	log.debug(">> Incoming JMF+AMF request from: %s", request.getRequestURL());
    	
    	if (jmfSharedContext == null)
    		throw GraniteConfigListener.newSharedContextNotInitializedException();

        InputStream is = null;
        OutputStream os = null;
        
        try {
        	is = request.getInputStream();
        	
            GraniteContext context = HttpGraniteContext.createThreadIntance(
                graniteConfig, servicesConfig, config.getServletContext(),
                request, response
            );

            AMFContextImpl amf = (AMFContextImpl)context.getAMFContext();

            log.debug(">> Deserializing JMF+AMF request...");

            @SuppressWarnings("all") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
			JMFDeserializer deserializer = new JMFDeserializer(is, jmfSharedContext);
            AMF0Message amf0Request = (AMF0Message)deserializer.readObject();

            amf.setAmf0Request(amf0Request);

            log.debug(">> Chaining AMF0 request: %s", amf0Request);

            chain.doFilter(request, response);

            AMF0Message amf0Response = amf.getAmf0Response();

            log.debug("<< Serializing JMF+AMF response: %s", amf0Response);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(ContentType.JMF_AMF.mimeType());
	        response.setDateHeader("Expire", 0L);
	        response.setHeader("Cache-Control", "no-store");
            
	        os = response.getOutputStream();

            @SuppressWarnings("all") // JDK7 warning (Resource leak: 'serializer' is never closed)...
	        JMFSerializer serializer = new JMFSerializer(os, jmfSharedContext);
            serializer.writeObject(amf0Response);
            
            response.flushBuffer();
        }
        catch (IOException e) {
        	if ("org.apache.catalina.connector.ClientAbortException".equals(e.getClass().getName()))
        		log.debug(e, "Connection closed by client");
        	else
        		log.error(e, "JMF+AMF message error");
            throw e;
        }
        catch (Exception e) {
            log.error(e, "JMF+AMF message error");
            throw new ServletException(e);
        }
        finally {
        	if (is != null) {
        		try {
        			is.close();
        		} catch (IOException e) {
        			log.error(e, "Error while closing request input stream");
        		}
        	}
        	
        	if (os != null) {
        		try {
        			os.close();
        		} catch (IOException e) {
        			log.error(e, "Error while closing response output stream");
        		}
        	}
        	
            GraniteContext.release();
        }
    }

    public void destroy() {
        this.config = null;
        this.graniteConfig = null;
        this.servicesConfig = null;
        
        this.jmfSharedContext = null;
    }
}
