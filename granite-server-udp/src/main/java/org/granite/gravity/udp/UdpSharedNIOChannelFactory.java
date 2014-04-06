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
import java.nio.channels.DatagramChannel;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.udp.UdpReceiverFactory;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class UdpSharedNIOChannelFactory extends AbstractUdpChannelFactory {

	private static final Logger log = Logger.getLogger(UdpSharedNIOChannelFactory.class);

	private DatagramChannel channel = null;
	
	public UdpSharedNIOChannelFactory(UdpReceiverFactory factory) {
		super(factory); 
	}
	
	@Override
	public void start() {
		super.start();
		
		int port = factory.getPort();
		
		if (port == 0)
			log.info("Creating shared UDP channel on port * (any available port)...");
		else
			log.info("Creating shared UDP channel on port %d...", port);
			
		try {
			channel = DatagramChannel.open();
			channel.socket().setSendBufferSize(factory.getSendBufferSize());
			channel.socket().bind(new InetSocketAddress(port));
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create data channel on port: " + port, e);
		}
		
		log.info("UDP shared channel bound to port: %d", channel.socket().getLocalPort());
	}

	@Override
	public void stop() {
		super.stop();
		
		log.info("Closing UDP channel...");
		
		if (channel != null) {
			try {
				channel.close();
			}
			catch (IOException e) {
				log.error(e, "Could not close UDP channel");
			}
			finally {
				channel = null;
			}
		}

		log.info("UDP channel closed.");
	}

	@Override
	public UdpChannel newUdpChannel(AbstractChannel gravityChannel, InetSocketAddress address) {
		return new UdpNIOChannel(this, gravityChannel, channel, address);
	}
}
