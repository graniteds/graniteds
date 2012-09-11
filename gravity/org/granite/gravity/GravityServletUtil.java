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

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.webapp.HttpGraniteContext;
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
	
	public static Gravity initializeRequest(ServletConfig config, Gravity gravity, HttpServletRequest request, HttpServletResponse response) {
        HttpGraniteContext.createThreadIntance(
            gravity.getGraniteConfig(), gravity.getServicesConfig(),
            config.getServletContext(), request, response
        );
        return gravity;
	}

	public static Message[] deserialize(Gravity gravity, HttpServletRequest request) throws ClassNotFoundException, IOException {
		InputStream is = request.getInputStream();
		try {
			return deserialize(gravity, request, is);
		}
		finally {
			is.close();
		}
	}
	
	public static Message[] deserialize(Gravity gravity, HttpServletRequest request, InputStream is) throws ClassNotFoundException, IOException {
		ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
        Object[] objects = (Object[])amf3Deserializer.readObject();
        Message[] messages = new Message[objects.length];
        System.arraycopy(objects, 0, messages, 0, objects.length);
        
        return messages;
	}
	
	public static void serialize(Gravity gravity, HttpServletResponse response, Message[] messages) throws IOException {
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
	
	public static void cleanupRequest(HttpServletRequest request) {
		GraniteContext.release();
	}
}
