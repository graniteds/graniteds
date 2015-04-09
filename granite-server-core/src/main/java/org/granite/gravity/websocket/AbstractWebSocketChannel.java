/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.websocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Channel;
import org.granite.gravity.ChannelFactory;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.MessageReceivingException;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractWebSocketChannel extends AbstractChannel {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

	private static final int DEFAULT_MAX_BINARY_MESSAGE_BUFFER_SIZE = 16384;
	
    private static final Logger log = Logger.getLogger(AbstractWebSocketChannel.class);
    private static final Logger logFine = Logger.getLogger(AbstractWebSocketChannel.class.getName() + "_fine");

    private HttpSession session;
    private ContentType contentType;
    private Object clientId;
    private byte[] connectAckMessage;
    private int maxBinaryMessageBufferSize = DEFAULT_MAX_BINARY_MESSAGE_BUFFER_SIZE;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    protected AbstractWebSocketChannel(GravityInternal gravity, String id, ChannelFactory<? extends Channel> factory, String clientType) {
        super(gravity, id, factory, clientType);
    }
    
    protected void setMaxBinaryMessageBufferSize(int maxBinaryMessageBufferSize) {
    	if (maxBinaryMessageBufferSize < 512)
    		log.warn("Trying to set WebSocket maxBinaryMessageBufferSize too low: %d (ignored)", maxBinaryMessageBufferSize);
    	else {
	    	log.debug("Setting MaxBinaryMessageBufferSize to: %d", maxBinaryMessageBufferSize);
	    	this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    	}
    }
    
    public int getMaxBinaryMessageBufferSize() {
    	return maxBinaryMessageBufferSize;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public void setConnectAckMessage(Message ackMessage) {
        try {
            // Return an acknowledge message with the server-generated clientId
            clientId = ackMessage.getClientId();
            connectAckMessage = serialize(getGravity(), new Message[] { ackMessage });
        }
        catch (IOException e) {
            throw new RuntimeException("Could not serialize connect acknowledge", e);
        }
    }

    protected void connect() {
        log.debug("Channel %s websocket connect clientId %s %s", getId(), clientId, connectAckMessage == null ? "(no ack)" : "");

        if (connectAckMessage == null)
            return;

        try {
            // Return an acknowledge message with the server-generated clientId
            sendBytes(connectAckMessage);

            connectAckMessage = null;
        }
        catch (IOException e) {
            log.error(e, "Channel %s could not send connect acknowledge", getId());
        }
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    protected GravityInternal initializeRequest() {
        if (session != null)
            ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), session.getServletContext(), session, clientType);
        else
            SimpleGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType);
        return gravity;
    }

    protected Message[] deserialize(GravityInternal gravity, byte[] data, int offset, int length) throws ClassNotFoundException, IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(data, offset, length);

        try {
            Message[] messages = null;

            if (ContentType.JMF_AMF.equals(contentType)) {
                @SuppressWarnings("all") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
                JMFDeserializer deserializer = new JMFDeserializer(is, gravity.getGraniteConfig().getSharedContext());
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

    protected byte[] serialize(GravityInternal gravity, Message[] messages) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(200*messages.length);

            if (ContentType.JMF_AMF.equals(contentType)) {
                @SuppressWarnings("all") // JDK7 warning (Resource leak: 'serializer' is never closed)...
                        JMFSerializer serializer = new JMFSerializer(os, gravity.getGraniteConfig().getSharedContext());
                serializer.writeObject(messages);
            }
            else {
                ObjectOutput amf3Serializer = gravity.getGraniteConfig().newAMF3Serializer(os);
                amf3Serializer.writeObject(messages);
                amf3Serializer.flush();
            }

            return os.toByteArray();
        }
        finally {
            if (os != null)
                os.close();
        }
    }

    protected static void cleanupRequest() {
        GraniteContext.release();
    }

    public abstract boolean isConnected();

    protected abstract void sendBytes(byte[] msg) throws IOException;

    protected void receiveBytes(byte[] data, int offset, int length) {
        log.debug("Channel %s websocket receive %d bytes", getId(), data.length);

        try {
            initializeRequest();

            Message[] messages = deserialize(getGravity(), data, offset, length);

            logFine.debug(">> [AMF3 REQUESTS] %s", (Object)messages);

            Message[] responses = null;

            boolean accessed = false;
            int responseIndex = 0;
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];

                // Ask gravity to create a specific response (will be null with a connect request from tunnel).
                Message response = getGravity().handleMessage(getFactory(), message);
                String clientId = (String)message.getClientId();

                log.debug("Channel %s received message %s for clientId %s", getId(), message.getMessageId(), clientId);

                // Mark current channel (if any) as accessed.
                if (!accessed)
                    accessed = getGravity().access(clientId);
                
                if (response != null) {
                    if (responses == null)
                        responses = new Message[1];
                    else
                        responses = Arrays.copyOf(responses, responses.length+1);
                    responses[responseIndex++] = response;
                }
            }
            
            if (isConnected()) {	// Check in case of disconnect message
	            logFine.debug("<< [AMF3 RESPONSES] %s", (Object)responses);
	            
	            for (Message response : responses) {
	            	if (response instanceof AsyncMessage)
	            		receive((AsyncMessage)response);
	            }
            }
        }
        catch (MessageReceivingException e) {
            log.error(e, "Could not handle incoming message data");
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

            if (!isConnected()) {
                log.debug("Channel %s is not connected", getId());
                return false;
            }

            // Setup serialization context (thread local)
            GravityInternal gravity = getGravity();
            SimpleGraniteContext.createThreadInstance(
                gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType
            );

            AsyncMessage[] messagesArray = messages.toArray(new AsyncMessage[messages.size()]);

            logFine.debug("<< [MESSAGES for channel=%s] %s", this, messagesArray);
            
            gravity.access(getId());	// Notify that the channel has been active
            
            byte[] msg = serialize(gravity, messagesArray);
            if (msg.length <= maxBinaryMessageBufferSize) {
                log.debug("Channel %s send binary message: %d msgs (%d bytes)", getId(), messagesArray.length, msg.length);
                sendBytes(msg);
            }
            else {
            	int index = 1;
            	for (AsyncMessage message : messagesArray) {
            		msg = serialize(gravity, new AsyncMessage[]{ message });
            		log.debug("Channel %s send chunked binary message: %d/%d msgs (%d bytes)", getId(), index++, messagesArray.length, msg.length);
                    sendBytes(msg);
            	}
            }
            log.debug("Channel %s binary messages sent", getId());
            
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
    public void destroy(boolean timeout) {
        try {
            super.destroy(timeout);
        }
        finally {
            close(timeout);
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