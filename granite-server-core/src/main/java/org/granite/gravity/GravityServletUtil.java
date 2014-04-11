/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.gravity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfigListener;
import org.granite.context.GraniteContext;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.ContentType;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class GravityServletUtil {

    public static final String CONNECT_MESSAGE_KEY = AbstractGravityServlet.class.getName() + ".CONNECT_MESSAGE";

    public static void init(ServletConfig config) throws ServletException {
    	GravityManager.start(config);
    }

    ///////////////////////////////////////////////////////////////////////////
	// Connect messages management (request attribute).
	
	public static void setConnectMessage(HttpServletRequest request, Message connect) {
		if (!(connect instanceof CommandMessage) && ((CommandMessage)connect).getOperation() != CommandMessage.CONNECT_OPERATION)
			throw new IllegalArgumentException("Not a connect message: " + connect);
		request.setAttribute(CONNECT_MESSAGE_KEY, connect);
	}
	
	public static CommandMessage getConnectMessage(HttpServletRequest request) {
		return (CommandMessage)request.getAttribute(CONNECT_MESSAGE_KEY);
	}
	
	public static void removeConnectMessage(HttpServletRequest request) {
		request.removeAttribute(CONNECT_MESSAGE_KEY);
	}

	///////////////////////////////////////////////////////////////////////////
	// Long polling timeout.
	
	public static long getLongPollingTimeout(ServletContext context) {
		return GravityManager.getGravity(context).getGravityConfig().getLongPollingTimeoutMillis();
	}

	///////////////////////////////////////////////////////////////////////////
	// AMF (de)serialization methods.
	
	public static GravityInternal initializeRequest(ServletConfig config, GravityInternal gravity, HttpServletRequest request, HttpServletResponse response) {
        HttpGraniteContext.createThreadIntance(
            gravity.getGraniteConfig(), gravity.getServicesConfig(),
            config.getServletContext(), request, response
        );
        return gravity;
	}

	public static Message[] deserialize(GravityInternal gravity, HttpServletRequest request) throws ClassNotFoundException, IOException {
		InputStream is = request.getInputStream();
		try {
			return deserialize(gravity, request, request.getInputStream());
		}
		finally {
			is.close();
		}
	}
	
	@SuppressWarnings("resource") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
	public static Message[] deserialize(GravityInternal gravity, HttpServletRequest request, InputStream is) throws ClassNotFoundException, IOException {
        if (ContentType.JMF_AMF.mimeType().equals(request.getContentType())) {
            JMFDeserializer deserializer = new JMFDeserializer(is, gravity.getGraniteConfig().getSharedContext());
            return (Message[])deserializer.readObject();
        }
        else {
    		ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
            Object[] objects = (Object[])amf3Deserializer.readObject();
            Message[] messages = new Message[objects.length];
            System.arraycopy(objects, 0, messages, 0, objects.length);            
            return messages;
        }
	}
	
	public static ObjectOutput newSerializer(GravityInternal gravity, OutputStream os, ContentType contentType) throws ServletException, IOException {
        if (contentType == ContentType.JMF_AMF) {
            return new JMFSerializer(os, gravity.getGraniteConfig().getSharedContext());
        }
    	return gravity.getGraniteConfig().newAMF3Serializer(os);
	}
	
	public static void serialize(GravityInternal gravity, HttpServletResponse response, Message[] messages, ContentType contentType) throws ServletException, IOException {
    	if (contentType == ContentType.JMF_AMF && gravity.getGraniteConfig().getSharedContext() == null)
    		throw GraniteConfigListener.newSharedContextNotInitializedException();
    	
		OutputStream os = null;
		try {
            // For SDK 2.0.1_Hotfix2+ (LCDS 2.5+).
			String dsId = null;
            for (Message message : messages) {
	            if ("nil".equals(message.getHeader(Message.DS_ID_HEADER))) {
	            	if (dsId == null)
	            		dsId = UUIDUtil.randomUUID();
	                message.getHeaders().put(Message.DS_ID_HEADER, dsId);
	            }
            }
			
	        response.setStatus(HttpServletResponse.SC_OK);
	        response.setContentType(contentType.mimeType());
	        response.setDateHeader("Expire", 0L);
	        response.setHeader("Cache-Control", "no-store");

	        os = response.getOutputStream();
	        
	        ObjectOutput serializer = newSerializer(gravity, os, contentType);
            serializer.writeObject(messages);
            serializer.flush();	        
            
            os.flush();
	        response.flushBuffer();
		}
		finally {
			if (os != null)
				os.close();
		}
	}
	
	public static void cleanupRequest(HttpServletRequest request) {
		GraniteContext.release();
	}
}
