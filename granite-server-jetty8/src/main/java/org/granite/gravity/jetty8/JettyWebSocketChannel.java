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
package org.granite.gravity.jetty8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;


public class JettyWebSocketChannel extends AbstractChannel implements WebSocket, OnBinaryMessage {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketChannel.class);
	
	private HttpSession session;
	private ContentType contentType;
	private Connection connection;
	private Message connectAckMessage;

	
	public JettyWebSocketChannel(Gravity gravity, String id, JettyWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

	public void setConnectAckMessage(Message ackMessage) {
		this.connectAckMessage = ackMessage;
	}
	
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	public void onOpen(Connection connection) {
		this.connection = connection;
		this.connection.setMaxIdleTime((int)getGravity().getGravityConfig().getChannelIdleTimeoutMillis());

		log.debug("Channel %s websocket connection onOpen", getId());
		
		if (connectAckMessage == null)
			return;
		
		try {
			initializeRequest();
			
			// Return an acknowledge message with the server-generated clientId
	        byte[] resultData = serialize(getGravity(), new Message[] { connectAckMessage });
	        
			connection.sendMessage(resultData, 0, resultData.length);
			
			connectAckMessage = null;
		}
		catch (IOException e) {
			log.error(e, "Channel %s could not send connect acknowledge", getId());
		}
		finally {
			cleanupRequest();
		}
	}

	public void onClose(int closeCode, String message) {
		log.debug("Channel %s websocket connection onClose %d, %s", getId(), closeCode, message);
	}

	public void onMessage(byte[] data, int offset, int length) {
		log.debug("Channel %s websocket connection onMessage %d", getId(), data.length);
		
		try {
			initializeRequest();
			
			Message[] messages = deserialize(getGravity(), data, offset, length);

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

	            connection.sendMessage(resultData, 0, resultData.length);
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
	
	private Gravity initializeRequest() {
        if (session != null)
            ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), session.getServletContext(), session, clientType);
        else
            SimpleGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType);
		return gravity;
	}

	private Message[] deserialize(Gravity gravity, byte[] data, int offset, int length) throws ClassNotFoundException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(data, offset, length);
		
		try {
			Message[] messages = null;
			
			if (ContentType.JMF_AMF.equals(contentType)) {
		    	@SuppressWarnings("all") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
				JMFDeserializer deserializer = new JMFDeserializer(is, gravity.getSharedContext());
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
	            JMFSerializer serializer = new JMFSerializer(os, gravity.getSharedContext());
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
			
			if (connection == null || !connection.isOpen())
				return false;
			
			AsyncMessage[] messagesArray = new AsyncMessage[messages.size()];
			int i = 0;
			for (AsyncMessage message : messages)
				messagesArray[i++] = message;
			
			// Setup serialization context (thread local)
			Gravity gravity = getGravity();
            initializeRequest();

            log.debug("<< [MESSAGES for channel=%s] %s", this, messagesArray);

            byte[] msg = serialize(gravity, messagesArray);
            if (msg.length > 16000) {
                // Split in ~2000 bytes chunks
                int count = msg.length / 2000;
                int chunkSize = Math.max(1, messagesArray.length / count);
                int index = 0;
                while (index < messagesArray.length) {
                    AsyncMessage[] chunk = Arrays.copyOfRange(messagesArray, index, Math.min(messagesArray.length, index+chunkSize));
                    msg = serialize(gravity, chunk);
                    log.debug("Send binary message: %d msgs (%d bytes)", chunk.length, msg.length);
                    connection.sendMessage(msg, 0, msg.length);
                    index += chunkSize;
                }
            }
            else {
                connection.sendMessage(msg, 0, msg.length);
                log.debug("Send binary message: %d msgs (%d bytes)", messagesArray.length, msg.length);
            }

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
			connection.close(1000, "Channel closed");
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