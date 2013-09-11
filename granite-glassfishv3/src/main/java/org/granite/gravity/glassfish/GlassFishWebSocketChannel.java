/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.gravity.glassfish;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.LinkedList;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.logging.Logger;

import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketListener;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;


public class GlassFishWebSocketChannel extends AbstractChannel implements WebSocketListener {
	
	private static final Logger log = Logger.getLogger(GlassFishWebSocketChannel.class);
	
	private WebSocket websocket;
	private Message connectAckMessage;

	
	public GlassFishWebSocketChannel(Gravity gravity, String id, GlassFishWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
    }
	
	public void setConnectAckMessage(Message ackMessage) {
		this.connectAckMessage = ackMessage;
	}
	
	public void setWebSocket(WebSocket websocket) {
		this.websocket = websocket;
		this.websocket.add(this);
		
		if (connectAckMessage == null)
			return;
		
		try {
			// Return an acknowledge message with the server-generated clientId
	        byte[] resultData = serialize(getGravity(), new Message[] { connectAckMessage });
			websocket.send(resultData);
		}
		catch (IOException e) {
			throw new RuntimeException("Could not send connect acknowledge", e);
		}
		
		connectAckMessage = null;
	}
	
	public void onConnect(WebSocket websocket) {
	}

	public void onClose(WebSocket websocket, DataFrame frame) {
	}
	
	public void onMessage(WebSocket websocket, byte[] data) {
		try {
			initializeRequest(getGravity());
			
			Message[] messages = deserialize(getGravity(), data);

            log.debug(">> [AMF3 REQUESTS] %s", (Object)messages);

            Message[] responses = null;
            
            boolean accessed = false;
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                
                // Ask gravity to create a specific response (will be null with a connect request from tunnel).
                Message response = getGravity().handleMessage(getFactory(), message);
                String channelId = (String)message.getClientId();
                
                // Mark current channel (if any) as accessed.
                if (!accessed)
                	accessed = getGravity().access(channelId);                

                if (responses == null)
                	responses = new Message[messages.length];
                responses[i] = response;
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)responses);

            byte[] resultData = serialize(getGravity(), responses);
            
            websocket.send(resultData);
		}
		catch (ClassNotFoundException e) {
			log.error(e, "Could not handle incoming message data");
		}
		catch (IOException e) {
			log.error(e, "Could not handle incoming message data");
		}
		finally {
			cleanupRequest();
		}
	}
	
	private Gravity initializeRequest(Gravity gravity) {
		SimpleGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType);
		return gravity;
	}

	private static Message[] deserialize(Gravity gravity, byte[] data) throws ClassNotFoundException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		try {
			ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
	        Object[] objects = (Object[])amf3Deserializer.readObject();
	        Message[] messages = new Message[objects.length];
	        System.arraycopy(objects, 0, messages, 0, objects.length);
	        
	        return messages;
		}
		finally {
			is.close();
		}
	}
	
	private static byte[] serialize(Gravity gravity, Message[] messages) throws IOException {
		ByteArrayOutputStream os = null;
		try {
	        os = new ByteArrayOutputStream(200*messages.length);
	        ObjectOutput amf3Serializer = gravity.getGraniteConfig().newAMF3Serializer(os);
	        amf3Serializer.writeObject(messages);	        
	        os.flush();
	        return os.toByteArray();
		}
		finally {
			if (os != null)
				os.close();
		}		
	}
	
	private static void cleanupRequest() {
		GraniteContext.release();
	}
	
	@Override
	public boolean runReceived(AsyncHttpContext asyncHttpContext) {
		
		LinkedList<AsyncMessage> messages = null;
		ByteArrayOutputStream os = null;

		try {
			receivedQueueLock.lock();
			try {
				// Do we have any pending messages? 
				if (receivedQueue.isEmpty())
					return false;
				
				// Both conditions are ok, get all pending messages.
				messages = receivedQueue;
				receivedQueue = new LinkedList<AsyncMessage>();
			}
			finally {
				receivedQueueLock.unlock();
			}
			
	        if (websocket == null || !websocket.isConnected())
	        	return false;
	        
			AsyncMessage[] messagesArray = new AsyncMessage[messages.size()];
			int i = 0;
			for (AsyncMessage message : messages)
				messagesArray[i++] = message;
			
			// Setup serialization context (thread local)
			Gravity gravity = getGravity();
	        GraniteContext context = SimpleGraniteContext.createThreadInstance(
	            gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType
	        );
	        
	        os = new ByteArrayOutputStream(500);
	        ObjectOutput amf3Serializer = context.getGraniteConfig().newAMF3Serializer(os);
	        
	        log.debug("<< [MESSAGES for channel=%s] %s", this, messagesArray);
	        
	        amf3Serializer.writeObject(messagesArray);
	        
        	websocket.send(os.toByteArray());
	        
	        return true; // Messages were delivered, http context isn't valid anymore.
		}
		catch (IOException e) {
			log.warn(e, "Could not send messages to channel: %s (retrying later)", this);
			
			GravityConfig gravityConfig = getGravity().getGravityConfig();
			if (gravityConfig.isRetryOnError()) {
				receivedQueueLock.lock();
				try {
					if (receivedQueue.size() + messages.size() > gravityConfig.getMaxMessagesQueuedPerChannel()) {
						log.warn(
							"Channel %s has reached its maximum queue capacity %s (throwing %s messages)",
							this,
							gravityConfig.getMaxMessagesQueuedPerChannel(),
							messages.size()
						);
					}
					else
						receivedQueue.addAll(0, messages);
				}
				finally {
					receivedQueueLock.unlock();
				}
			}
			
			return true; // Messages weren't delivered, but http context isn't valid anymore.
		}
		finally {
			if (os != null) {
				try {
					os.close();
				}
				catch (Exception e) {
					// Could not close bytearray ???
				}
			}
			
			// Cleanup serialization context (thread local)
			try {
				GraniteContext.release();
			}
			catch (Exception e) {
				// should never happen...
			}
		}
	}

	@Override
	public void destroy() {
		try {
			super.destroy();
		}
		finally {
			close();
		}
	}
	
	public void close() {
		if (websocket != null) {
			websocket.close(1000, "Channel closed");
			websocket = null;
		}
	}
	
	@Override
	protected boolean hasAsyncHttpContext() {
		return true;
	}

	@Override
	protected void releaseAsyncHttpContext(AsyncHttpContext context) {
	}

	@Override
	protected AsyncHttpContext acquireAsyncHttpContext() {
    	return null;
    }

	public void onFragment(WebSocket arg0, String arg1, boolean arg2) {
	}

	public void onFragment(WebSocket arg0, byte[] arg1, boolean arg2) {
	}

	public void onMessage(WebSocket arg0, String arg1) {
	}

	public void onPing(WebSocket arg0, byte[] arg1) {
	}

	public void onPong(WebSocket arg0, byte[] arg1) {
	}

}