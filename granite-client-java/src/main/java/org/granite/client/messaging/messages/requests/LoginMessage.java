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
package org.granite.client.messaging.messages.requests;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import org.granite.client.messaging.channel.Credentials;

/**
 * @author Franck WOLFF
 */
public final class LoginMessage extends AbstractRequestMessage {

    private static final long serialVersionUID = 1L;
	
	private Credentials credentials;
	
	public LoginMessage() {
	}

	public LoginMessage(String clientId, Credentials credentials) {
		super(clientId);
		
		this.credentials = credentials;
	}

	public LoginMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		Credentials credentials) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.credentials = credentials;
	}

	@Override
	public Type getType() {
		return Type.LOGIN;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	@Override
	public LoginMessage copy() {
		LoginMessage message = new LoginMessage();
		
		copy(message);
		
		message.credentials = credentials;
		
		return message;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		credentials = (Credentials)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeObject(credentials);
	}
	
	@Override
	public StringBuilder toString(StringBuilder sb) {
		return super.toString(sb).append("\n    credentials=").append(credentials);
	}
}
