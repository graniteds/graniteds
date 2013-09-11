/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.gravity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AsyncHttpContext {

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final Message connect;
	private final Object object;

	public AsyncHttpContext(HttpServletRequest request, HttpServletResponse response, Message connect) {
		this(request, response, connect, null);
	}
	
	public AsyncHttpContext(HttpServletRequest request, HttpServletResponse response, Message connect, Object object) {
		this.request = request;
		this.response = response;
		this.connect = connect;
		this.object = object;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Message getConnectMessage() {
		return connect;
	}

	public Object getObject() {
		return object;
	}
}
