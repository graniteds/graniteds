/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.messaging.amf.AMF0Message;

public class MockAMFRemotingChannel extends AMFRemotingChannel {

    public MockAMFRemotingChannel(Transport transport, MessagingCodec<AMF0Message> codec, String id, URI uri, int maxConcurrentRequests) {
        super(transport, codec, id, uri, maxConcurrentRequests);
    }

	public TransportMessage createMessage(AsyncToken token) throws UnsupportedEncodingException {
		return createTransportMessage(token);
	}

}
