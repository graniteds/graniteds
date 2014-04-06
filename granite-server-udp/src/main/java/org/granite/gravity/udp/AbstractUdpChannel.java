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

import org.granite.gravity.AbstractChannel;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractUdpChannel implements UdpChannel {
	
    protected UdpChannelFactory channelFactory = null;
    protected AbstractChannel gravityChannel = null;
	protected InetSocketAddress address = null;
	
	public AbstractUdpChannel(UdpChannelFactory channelFactory, AbstractChannel gravityChannel, InetSocketAddress address) {
		if (channelFactory == null || gravityChannel == null)
			throw new NullPointerException();
		
		this.channelFactory = channelFactory;
		this.gravityChannel = gravityChannel;
		this.address = address;
	}

	public AbstractChannel getGravityChannel() {
		return gravityChannel;
	}

	public int write(byte[] data) throws IOException {
		return write(data, 0, data.length);
	}

	public void close() {
		channelFactory = null;
		gravityChannel = null;
		address = null;
	}
}
