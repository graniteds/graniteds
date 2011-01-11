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

package org.granite.gravity;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AbstractGravityServlet extends HttpServlet {

	///////////////////////////////////////////////////////////////////////////
	// Fields.
	
	private static final long serialVersionUID = 1L;

    private static final String CONNECT_MESSAGE_KEY = AbstractGravityServlet.class.getName() + ".CONNECT_MESSAGE";

	///////////////////////////////////////////////////////////////////////////
	// Initialization.

	public void init(ServletConfig config, ChannelFactory channelFactory) throws ServletException {
		super.init(config);
		
		GravityManager.start(config, channelFactory);
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
	
	protected long getLongPollingTimeout() {
		return GravityManager.getGravity(getServletContext()).getGravityConfig().getLongPollingTimeoutMillis();
	}

	///////////////////////////////////////////////////////////////////////////
	// AMF (de)serialization methods.
	
	protected Gravity initializeRequest(Gravity gravity, HttpServletRequest request, HttpServletResponse response) {
        HttpGraniteContext.createThreadIntance(
            gravity.getGraniteConfig(), gravity.getServicesConfig(),
            getServletConfig().getServletContext(), request, response
        );
        return gravity;
	}

	protected Message[] deserialize(Gravity gravity, HttpServletRequest request) throws ClassNotFoundException, IOException {
		InputStream is = request.getInputStream();
		try {
			return deserialize(gravity, request, is);
		}
		finally {
			is.close();
		}
	}
	
	protected Message[] deserialize(Gravity gravity, HttpServletRequest request, InputStream is) throws ClassNotFoundException, IOException {
		ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
        Object[] objects = (Object[])amf3Deserializer.readObject();
        Message[] messages = new Message[objects.length];
        System.arraycopy(objects, 0, messages, 0, objects.length);
        
        return messages;
	}
	
	protected void serialize(Gravity gravity, HttpServletResponse response, Message[] messages) throws IOException {
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
	        response.setContentType(AMF0Message.CONTENT_TYPE);
	        response.setDateHeader("Expire", 0L);
	        response.setHeader("Cache-Control", "no-store");
	        
	        os = response.getOutputStream();
	        ObjectOutput amf3Serializer = gravity.getGraniteConfig().newAMF3Serializer(os);
	        amf3Serializer.writeObject(messages);
	        
	        os.flush();
	        response.flushBuffer();
		}
		finally {
			if (os != null)
				os.close();
		}
	}
	
	protected void cleanupRequest(HttpServletRequest request) {
		GraniteContext.release();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Unsupported HTTP methods.

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		throw new ServletException("Unsupported operation: " + req.getMethod());
	}
}
