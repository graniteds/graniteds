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
package org.granite.client.messaging.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;

/**
 * @author Franck WOLFF
 */
public class DefaultTransportMessage<M> implements TransportMessage {

	private final String id;
	private final boolean connect;
	private final String clientId;
	private final String sessionId;
	private final M message;
	private final MessagingCodec<M> codec;

	public DefaultTransportMessage(String id, boolean connect, String clientId, String sessionId, M message, MessagingCodec<M> codec) {
		this.id = id;
		this.connect = connect;
		this.clientId = clientId;
		this.sessionId = sessionId;
		this.message = message;
		this.codec = codec;
	}

	public ClientType getClientType() {
		return codec.getClientType();
	}

	public String getId() {
		return id;
	}
	
	public boolean isConnect() {
		return connect;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public String getContentType() {
		return codec.getContentType();
	}

	@Override
	public void encode(OutputStream os) throws IOException {
		codec.encode(message, os);
	}
}
