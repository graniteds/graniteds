/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.tide.server.ComponentListener;

/**
 * @author William DRAI
 */
public class FutureResult<T> implements Future<T> {
    
	private ResponseMessageFuture responseMessageFuture;
    private ComponentListener<T> componentListener;
    
    public FutureResult(ResponseMessageFuture responseMessageFuture, ComponentListener<T> componentListener) {
    	this.responseMessageFuture = responseMessageFuture;
    	this.componentListener = componentListener;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return responseMessageFuture.cancel();
    }

    @Override
    public boolean isCancelled() {
        return responseMessageFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return responseMessageFuture.isDone();
    }

	@Override
    public T get() throws InterruptedException, ExecutionException {
		try {
			responseMessageFuture.get();
		}
		catch (TimeoutException e) {
			throw new InterruptedException("Request timeout: " + e.getMessage());
		}
		return componentListener.getResult();
    }
	
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("Cannot use timeout on get(), set timeout on request");
    }

}
