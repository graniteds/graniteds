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

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.udp.UdpReceiverFactory;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class UdpConnectedNIOChannelFactory extends AbstractUdpChannelFactory {

	private static final Logger log = Logger.getLogger(UdpConnectedNIOChannelFactory.class);

	public UdpConnectedNIOChannelFactory(UdpReceiverFactory factory) {
		super(factory); 
	}
	
	@Override
	public void start() {
		super.start();
		
		log.info("UDP channel factory started.");
	}

	@Override
	public void stop() {
		super.stop();
		
		log.info("UDP channel factory stopped.");
	}

	@Override
	public UdpChannel newUdpChannel(AbstractChannel gravityChannel, InetSocketAddress address) {
		int port = factory.getPort();
		
		if (port == 0)
			log.info("Creating UDP channel on port * (any available port) and connected to %s...", address);
		else
			log.info("Creating UDP channel on port %d and connected to %s...", port, address);

		try {
			DatagramChannel channel = DatagramChannel.open();
			channel.socket().setSendBufferSize(factory.getSendBufferSize());
			if (port != 0)
				channel.socket().setReuseAddress(true);
			channel.socket().bind(new InetSocketAddress(port));
			channel.connect(address);

			log.info("UDP channel bound to port %d and connected to %s", channel.socket().getLocalPort(), channel.socket().getRemoteSocketAddress());
			
			return new UdpNIOChannel(this, gravityChannel, channel);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create data channel on port: " + port, e);
		}
	}
}
