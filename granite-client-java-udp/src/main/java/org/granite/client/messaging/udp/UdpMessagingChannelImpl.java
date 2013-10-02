/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.messaging.udp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.channel.amf.BaseAMFMessagingChannel;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.DefaultTransportMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.logging.Logger;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class UdpMessagingChannelImpl extends BaseAMFMessagingChannel implements UdpMessagingChannel, UpdMessageListener {

	private static final Logger log = Logger.getLogger(UdpMessagingChannelImpl.class);

	private static final String GDS_CLIENT_UPD_PORT = "GDS_CLIENT_UDP_PORT";
	private static final String GDS_SERVER_UDP_PORT = "GDS_SERVER_UDP_PORT";

	private final ConcurrentMap<UdpChannelListener, Boolean> listeners = new ConcurrentHashMap<UdpChannelListener, Boolean>();
	private final InetAddress remoteHost; 
	
	private SocketAddress defaultLocalAddress = null;

	private DatagramChannel channel = null;
	private Thread channelReader = null; 

	public UdpMessagingChannelImpl(MessagingCodec<Message[]> codec, Transport transport, String id, URI uri) {
		super(codec, transport, id, uri);
		
		try {
			remoteHost = InetAddress.getByName(uri.getHost());
		}
		catch (UnknownHostException e) {
			throw new RuntimeException("Could not get remote host address from: " + uri, e);
		}
	}
	
	public void addListener(UdpChannelListener listener) {
		listeners.put(listener, Boolean.TRUE);
	}
	
	public boolean removeListener(UdpChannelListener listener) {
		return listeners.remove(listener) != null;
	}

	@Override
	public void setDefaultLocalAddress(SocketAddress address) {
		defaultLocalAddress = address;
	}

	@Override
	public SocketAddress getDefaultLocalAddress() {
		return defaultLocalAddress;
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return (channel != null ? channel.socket().getLocalSocketAddress() : null);
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return (channel != null ? channel.socket().getRemoteSocketAddress() : null);
	}

	@Override
	protected boolean connect() {
		
		// No subscriptions...
		if (consumersMap.isEmpty())
			return false;
		
		// We are already waiting for a connection/answer.
		final String id = UUIDUtil.randomUUID();
		if (!connectMessageId.compareAndSet(null, id))
			return false;
		
		log.debug("Connecting UDP channel with clientId %s", clientId);

		try {
			channel = DatagramChannel.open();
			channel.socket().bind(defaultLocalAddress);
		}
		catch (Exception e) {
			channel = null;
			return false;
		}
		
		for (UdpChannelListener listener : listeners.keySet()) {
			try {
				listener.onBound(this);
			}
			catch (Exception e) {
				log.error(e, "Error while calling listener %s", listener);
			}
		}
		
		int port = channel.socket().getLocalPort();
		
		CommandMessage connectMessage = new CommandMessage();
		connectMessage.setOperation(CommandMessage.CONNECT_OPERATION);
		connectMessage.setMessageId(id);
		connectMessage.setTimestamp(System.currentTimeMillis());
		connectMessage.setClientId(clientId);
		connectMessage.setHeader(GDS_CLIENT_UPD_PORT, Integer.valueOf(port));

		try {
			transport.send(this, new DefaultTransportMessage<Message[]>(id, true, clientId, sessionId, new Message[]{connectMessage}, codec));
			
			return true;
		}
		catch (Exception e) {
			connectMessageId.set(null);
			
			return false;
		}
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		ResponseMessage response = super.decodeResponse(is);
		
		if (response instanceof ResultMessage && response.getHeader(GDS_SERVER_UDP_PORT) != null) {
			ResultMessage result = (ResultMessage)response;
			
			String id = connectMessageId.getAndSet(null);
			if (id ==  null || !id.equals(result.getCorrelationId()))
				log.warn("Bad correlation id: %s != %s", id, result.getCorrelationId());

			Number port = (Number)result.getHeader(GDS_SERVER_UDP_PORT);
			if (port == null)
				throw new RuntimeException("Server didn't return an UDP port");

			channel.connect(new InetSocketAddress(remoteHost, port.intValue()));
			channel.configureBlocking(true);
			
			channelReader = new Thread(new UpdMessageReader(channel, this));
			channelReader.start();

			
			for (UdpChannelListener listener : listeners.keySet()) {
				try {
					listener.onConnected(this);
				}
				catch (Exception e) {
					log.error(e, "Error while calling listener %s", listener);
				}
			}
			
			return null;
		}
		
		return response;
	}
	
	public void onUdpMessage(byte[] data, int off, int len) {
		try {
			Message[] messages = codec.decode(new ByteArrayInputStream(data, off, len));
			
			for (Message message : messages) {
				if (!(message instanceof AsyncMessage))
					throw new RuntimeException("Message should be an AsyncMessage: " + message);
				
				String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
				Consumer consumer = consumersMap.get(subscriptionId);
				if (consumer != null)
					consumer.onMessage(convertFromAmf((AsyncMessage)message));
				else
					log.warn("No consumer for subscriptionId: %s", subscriptionId);
			}
		}
		catch (Exception e) {
			log.error(e, "Error while reading UDP message");
		}
	}

	@Override
	public void onStop(Transport transport) {
		
		if (channelReader != null) {
			try {
				channelReader.interrupt();
			}
			catch (Exception e) {
				log.error(e, "Could not close UDP channel %s", channel);
			}
			finally {
				channelReader = null;
				channel = null;
			}
		}
		
		for (UdpChannelListener listener : listeners.keySet()) {
			try {
				listener.onClosed(this);
			}
			catch (Exception e) {
				log.error(e, "Error while calling listener %s", listener);
			}
		}
		
		super.onStop(transport);
	}
}
