/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.granite.gravity.GravityInternal;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class UdpSharedIOChannelFactory extends AbstractUdpChannelFactory {

	private static final Logger log = Logger.getLogger(UdpSharedIOChannelFactory.class);

	private DatagramSocket socket = null;
	
	public UdpSharedIOChannelFactory(UdpReceiverFactory factory) {
		super(factory); 
	}
	
	@Override
	public void start() {
		super.start();
		
		int port = factory.getPort();
		
		if (port == 0)
			log.info("Creating shared UDP socket on port * (any available port)...");
		else
			log.info("Creating shared UDP socket on port %d...", port);
			
		try {
			socket = new DatagramSocket(port);
			socket.setSendBufferSize(factory.getSendBufferSize());
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create data socket on port: " + port, e);
		}
		
		log.info("UDP shared socket bound to port: %d", socket.getLocalPort());
	}

	@Override
	public void stop() {
		super.stop();
		
		log.info("Closing UDP socket...");
		
		if (socket != null) {
			try {
				socket.close();
			}
			finally {
				socket = null;
			}
		}

		log.info("UDP socket closed.");
	}

	@Override
	public UdpChannel newUdpChannel(GravityInternal gravity, InetSocketAddress address) {
		return new UdpIOChannel(this, gravity, socket, address);
	}
}
