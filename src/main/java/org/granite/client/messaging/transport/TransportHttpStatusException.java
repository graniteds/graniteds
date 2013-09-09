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
package org.granite.client.messaging.transport;

/**
 * @author Franck WOLFF
 */
public class TransportHttpStatusException extends TransportException {

	private static final long serialVersionUID = 1L;

	private final int status;
	
	public TransportHttpStatusException(int status) {
		this.status = status;
	}

	public TransportHttpStatusException(int status, String message) {
		super(message);
		this.status = status;
	}

	public TransportHttpStatusException(int status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	public TransportHttpStatusException(int status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return super.toString() + " (code=" + status + ")";
	}
}
