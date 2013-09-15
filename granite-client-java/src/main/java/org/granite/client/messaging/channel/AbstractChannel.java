/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
