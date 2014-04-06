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
package org.granite.client.messaging.events;

import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.ResponseMessage;

/**
 * Message received from a remoting channel
 *
 * @author Franck WOLFF
 */
public abstract class AbstractResponseEvent<M extends ResponseMessage> implements IncomingMessageEvent<M> {

	protected final RequestMessage request;
	protected final M response;
	
	public AbstractResponseEvent(RequestMessage request, M response) {
		if (request == null || response == null)
			throw new NullPointerException("request and response cannot be null");

		this.request = request;
		this.response = response;
	}

    /**
     * Source request message
     * @return request message
     */
	public RequestMessage getRequest() {
		return request;
	}

    /**
     * Response message
     * @return response message
     */
	public M getResponse() {
		return response;
	}

    /**
     * Response message
     * @return response message
     */
	@Override
	public M getMessage() {
		return response;
	}
}