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
package org.granite.gravity.udp;

import java.net.InetSocketAddress;

import javax.servlet.http.HttpServletRequest;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.udp.UdpReceiver;
import org.granite.gravity.udp.UdpReceiverFactory;
import org.granite.logging.Logger;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class UdpReceiverFactoryImpl implements UdpReceiverFactory {

	private static final Logger log = Logger.getLogger(UdpReceiverFactoryImpl.class);

    private int port = 0;
	private boolean nio = true;
	private boolean connected = false;
	private int sendBufferSize = GravityConfig.DEFAULT_UDP_SEND_BUFFER_SIZE;
	
	private UdpChannelFactory udpChannelFactory = null;

	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}

	public boolean isNio() {
		return nio;
	}
	public void setNio(boolean nio) {
		this.nio = nio;
	}

	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}
	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public void start() {
		log.info("Starting UDP Receiver Factory (port=%d, nio=%b, connected=%b, sendBufferSize=%d)", port, nio, connected, sendBufferSize);
		
		if (connected) {
			if (nio)
				udpChannelFactory = new UdpConnectedNIOChannelFactory(this);
			else
				udpChannelFactory = new UdpConnectedIOChannelFactory(this);
		}
		else {
			if (nio)
				udpChannelFactory = new UdpSharedNIOChannelFactory(this);
			else
				udpChannelFactory = new UdpSharedIOChannelFactory(this);
		}
		
		udpChannelFactory.start();
	}

	@Override
	public int getHeartBeatPort() {
		return 0;
	}
	
	public boolean isUdpConnectRequest(Message connect) {
		return connect.headerExists(UdpReceiverImpl.GDS_CLIENT_UPD_PORT);
	}

	public UdpReceiver newReceiver(AbstractChannel channel, HttpServletRequest request, Message connect) {
		Number port = (Number)connect.getHeader(UdpReceiverImpl.GDS_CLIENT_UPD_PORT);
		InetSocketAddress address = new InetSocketAddress(request.getRemoteAddr(), port.intValue());
		
		log.debug("Creating UDP receiver for channel id %s and address %s", channel.getId(), address);
		
		return new UdpReceiverImpl(udpChannelFactory.newUdpChannel(channel, address), connect);
	}

	public void stop() {
		log.info("Stopping UDP Receiver Factory");

		try {
			udpChannelFactory.stop();
		}
		finally {
			udpChannelFactory = null;
		}
	}
}
