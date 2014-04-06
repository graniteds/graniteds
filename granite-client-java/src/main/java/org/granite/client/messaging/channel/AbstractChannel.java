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
package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.messaging.transport.Transport;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractChannel<T extends Transport> implements Channel {
	
	protected final T transport;
	protected final String id;
	protected final URI uri;
	
	protected volatile String clientId;

	protected volatile Credentials credentials = null;
	protected volatile Object transportData = null;
	
	public AbstractChannel(T transport, String id, URI uri) {
		if (transport == null || id == null || uri == null)
			throw new NullPointerException("Transport, id and uri must be not null");
		
		this.transport = transport;
		this.id = id;
		this.uri = uri;
	}

	@Override
	public T getTransport() {
		return transport;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}
	
	@Override
	public Credentials getCredentials() {
		return credentials;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <D> D getTransportData() {
		return (D)transportData;
	}

	@Override
	public void setTransportData(Object data) {
		this.transportData = data;
	}
}
