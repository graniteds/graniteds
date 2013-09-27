/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.granite.config.GraniteConfigListener;
import org.granite.context.GraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;


public class TomcatWebSocketChannel extends AbstractChannel {
	
	private static final Logger log = Logger.getLogger(TomcatWebSocketChannel.class);
	
	private StreamInbound streamInbound = new MessageInboundImpl();
	private ServletContext servletContext;
	private SharedContext jmfSharedContext;
	private ContentType contentType;
	
	private WsOutbound connection;
	private byte[] connectAckMessage;

	
	public TomcatWebSocketChannel(Gravity gravity, String id, TomcatWebSocketChannelFactory factory, ServletContext servletContext, String clientType) {
    	super(gravity, id, factory, clientType);

    	this.servletContext = servletContext;
        this.jmfSharedContext = GraniteConfigListener.getSharedContext(servletContext);
	}
	
	public void setConnectAckMessage(Message ackMessage) {
		try {
			// Return an acknowledge message with the server-generated clientId
			connectAckMessage = serialize(getGravity(), new Message[] { ackMessage });		        
		}
		catch (IOException e) {
			throw new RuntimeException("Could not send connect acknowledge", e);
		}
	}
	
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public StreamInbound getStreamInbound() {
		return streamInbound;
	}
	
	public class MessageInboundImpl extends MessageInbound {
		
		public MessageInboundImpl() {
		}

		@Override
		protected void onOpen(WsOutbound outbound) {			
			connection = outbound;
			
			log.debug("WebSocket connection onOpen");
			
			if (connectAckMessage == null)
				return;
			
			try {
		        ByteBuffer buf = ByteBuffer.wrap(connectAckMessage);
				connection.writeBinaryMessage(buf);
			}
			catch (IOException e) {
				throw new RuntimeException("Could not send connect acknowledge", e);
			}
			
			connectAckMessage = null;		
		}

		@Override
		public void onClose(int closeCode) {
			log.debug("WebSocket connection onClose %d", closeCode);
			
			connection = null;
		}
		
		@Override
		public void onBinaryMessage(ByteBuffer buf) {
			byte[] data = buf.array();
			
			log.debug("WebSocket connection onBinaryMessage %d", data.length);
			
			try {
				initializeRequest();
				
				Message[] messages = deserialize(getGravity(), data);

	            log.debug(">> [AMF3 REQUESTS] %s", (Object)messages);

	            Message[] responses = null;
	            
	            boolean accessed = false;
	            int responseIndex = 0;
	            for (int i = 0; i < messages.length; i++) {
	                Message message = messages[i];
	                
	                // Ask gravity to create a specific response (will be null with a connect request from tunnel).
	                Message response = getGravity().handleMessage(getFactory(), message);
	                String channelId = (String)message.getClientId();
	                
	                // Mark current channel (if any) as accessed.
	                if (!accessed)
	                	accessed = getGravity().access(channelId);
	                
	                if (response != null) {
		                if (responses == null)
		                	responses = new Message[1];
		                else
		                	responses = Arrays.copyOf(responses, responses.length+1);
		                responses[responseIndex++] = response;
	                }
	            }
	            
	            if (responses != null && responses.length > 0) {
		            log.debug("<< [AMF3 RESPONSES] %s", (Object)responses);
		
		            byte[] resultData = serialize(getGravity(), responses);
		            
		            connection.writeBinaryMessage(ByteBuffer.wrap(resultData));
	            }
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

		@Override
		protected void onTextMessage(CharBuffer buf) throws IOException {
		}
		
		public int getAckLength() {
			return connectAckMessage != null ? connectAckMessage.length : 0;
		}
	}
	
	private Gravity initializeRequest() {
		ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), servletContext, sessionId, clientType);
		return gravity;
	}

	private Message[] deserialize(Gravity gravity, byte[] data) throws ClassNotFoundException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		
		try {
			Message[] messages = null;
			
			if (ContentType.JMF_AMF.equals(contentType)) {
		    	@SuppressWarnings("all") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
				JMFDeserializer deserializer = new JMFDeserializer(is, jmfSharedContext);
				messages = (Message[])deserializer.readObject();
			}
			else {
				ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
		        Object[] objects = (Object[])amf3Deserializer.readObject();
		        messages = new Message[objects.length];
		        System.arraycopy(objects, 0, messages, 0, objects.length);
			}
	        
	        return messages;
		}
		finally {
			is.close();
		}
	}
	
	private byte[] serialize(Gravity gravity, Message[] messages) throws IOException {
		ByteArrayOutputStream os = null;
		try {
	        os = new ByteArrayOutputStream(200*messages.length);
	        
	        if (ContentType.JMF_AMF.equals(contentType)) {
		        @SuppressWarnings("all") // JDK7 warning (Resource leak: 'serializer' is never closed)...
	            JMFSerializer serializer = new JMFSerializer(os, jmfSharedContext);
	            serializer.writeObject(messages);
	        }
	        else {
		        ObjectOutput amf3Serializer = gravity.getGraniteConfig().newAMF3Serializer(os);
		        amf3Serializer.writeObject(messages);	        
		        os.flush();
	        }

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
			
			if (connection == null)
				return false;
			
			AsyncMessage[] messagesArray = new AsyncMessage[messages.size()];
			int i = 0;
			for (AsyncMessage message : messages)
				messagesArray[i++] = message;
			
			// Setup serialization context (thread local)
			Gravity gravity = getGravity();
	        GraniteContext context = ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), servletContext, sessionId, clientType);
	        
	        os = new ByteArrayOutputStream(500);
	        ObjectOutput amf3Serializer = context.getGraniteConfig().newAMF3Serializer(os);
	        
	        log.debug("<< [MESSAGES for channel=%s] %s", this, messagesArray);
	        
	        amf3Serializer.writeObject(messagesArray);
	        
	        connection.writeBinaryMessage(ByteBuffer.wrap(os.toByteArray()));
	        
	        return true; // Messages were delivered
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
		if (connection != null) {
			try {
				connection.close(1000, ByteBuffer.wrap("Channel closed".getBytes()));
			}
			catch (IOException e) {
				log.error("Could not close WebSocket connection", e);
			}
			connection = null;
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
}