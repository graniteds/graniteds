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
package org.granite.client.messaging.channel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.messages.ResponseMessage;

/**
 * @author Franck WOLFF
 */
public class ImmediateFailureResponseMessageFuture implements ResponseMessageFuture {

	private final Exception cause;
	
	public ImmediateFailureResponseMessageFuture(Exception cause) {
		if (cause == null)
			throw new NullPointerException("cause cannot be null");
		this.cause = cause;
	}

	@Override
	public boolean cancel() {
		return false;
	}

	@Override
	public ResponseMessage get() throws InterruptedException, ExecutionException, TimeoutException {
		throw new ExecutionException(cause);
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}
}
