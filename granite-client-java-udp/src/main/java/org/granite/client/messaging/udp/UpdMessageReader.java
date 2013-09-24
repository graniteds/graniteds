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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class UpdMessageReader implements Runnable {

	private static final Logger log = Logger.getLogger(UpdMessageReader.class);

	public static final int DEFAULT_UDP_RECEIVE_BUFFER_SIZE = 64 * 1024; // 64K.

	private final DatagramChannel channel;
	private final UpdMessageListener listener;
	private final ByteBuffer buffer;
	
	public UpdMessageReader(DatagramChannel channel, UpdMessageListener listener) {
		this.channel = channel;
		this.listener = listener;
		
		this.buffer = ByteBuffer.allocate(DEFAULT_UDP_RECEIVE_BUFFER_SIZE);
	}

	@Override
	public void run() {
		log.debug("Entering UDP channel read loop");
		
		while (!Thread.interrupted()) {
			buffer.clear();
			try {
				int read = channel.read(buffer);
				if (read == -1) {
					log.debug("UDP channel closed");
					break;
				}
				
				listener.onUdpMessage(buffer.array(), 0, buffer.position());
			}
			catch (ClosedByInterruptException e) {
				log.debug(e, "UDP read loop interrupted");
				break;
			}
			catch (IOException e) {
				log.error(e, "Error while reading UDP channel");
				break;
			}
		}
		
		log.debug("Exiting UDP channel read loop");
	}
}
