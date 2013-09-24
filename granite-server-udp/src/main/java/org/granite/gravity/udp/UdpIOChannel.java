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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.granite.gravity.AbstractChannel;

/**
 * @author Franck WOLFF
 */
public class UdpIOChannel extends AbstractUdpChannel {

	private DatagramSocket socket = null;
	
	public UdpIOChannel(UdpChannelFactory channelFactory, AbstractChannel gravityChannel, DatagramSocket socket) {
		this(channelFactory, gravityChannel, socket, null);
	}
	
	public UdpIOChannel(UdpChannelFactory channelFactory, AbstractChannel gravityChannel, DatagramSocket socket, InetSocketAddress address) {
		super(channelFactory, gravityChannel, address);
		
		if ((address == null && !socket.isConnected()) || (address != null && socket.isConnected()))
			throw new IllegalArgumentException("Inconsistent arguments");

		this.socket = socket;
	}

	public SocketAddress getClientAddress() {
		return (socket.isConnected() ? socket.getRemoteSocketAddress() : address);
	}

	@Override
	public int getServerPort() {
		return (socket.isConnected() ? socket.getLocalPort() : address.getPort());
	}

	public int write(byte[] data, int offset, int length) throws IOException {
		int size = length - offset;
        
		if (size > MAX_PACKET_SIZE)
        	throw new UdpPacketTooLargeException("UDP packet size cannot exceed 64K: " + size);

		DatagramPacket packet = (
			socket.isConnected() ?
			new DatagramPacket(data, offset, length) :
			new DatagramPacket(data, offset, length, address)
		);
		
		socket.send(packet);
		return size;
	}

	public void close() {
		super.close();
		
		try {
			if (socket.isConnected())
				socket.close();
		}
		finally {
			socket = null;
		}
	}
}
