/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.granite.gravity.AbstractChannel;

/**
 * @author Franck WOLFF
 */
public class UdpNIOChannel extends AbstractUdpChannel {

	private DatagramChannel channel = null;
	
	public UdpNIOChannel(UdpChannelFactory channelFactory, AbstractChannel gravityChannel, DatagramChannel channel) {
		this(channelFactory, gravityChannel, channel, null);
	}

	public UdpNIOChannel(UdpChannelFactory channelFactory, AbstractChannel gravityChannel, DatagramChannel channel, InetSocketAddress address) {
		super(channelFactory, gravityChannel, address);
		
		if ((address == null && !channel.isConnected()) || (address != null && channel.isConnected()))
			throw new IllegalArgumentException("Inconsistent arguments");

		this.channel = channel;
	}
	
	public SocketAddress getClientAddress() {
		return (channel.isConnected() ? channel.socket().getRemoteSocketAddress() : address);
	}
	
	@Override
	public int getServerPort() {
		return (channel.isConnected() ? channel.socket().getLocalPort() : address.getPort());
	}

	public int write(byte[] data, int offset, int length) throws IOException {
		int size = length - offset;
        
		if (size > MAX_PACKET_SIZE)
        	throw new UdpPacketTooLargeException("UDP packet size cannot exceed 64K: " + size);

        ByteBuffer buf = ByteBuffer.wrap(data, offset, length);
        return (channel.isConnected() ? channel.write(buf) : channel.send(buf, address));
	}

	public void close() {
		super.close();

		try {
			if (channel.isConnected()) {
				try {
					channel.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		finally {
			channel = null;
		}
	}
}
