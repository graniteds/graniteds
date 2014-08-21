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
package org.granite.client.messaging.channel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.messages.ResponseMessage;

/**
 * Future-like interface to synchronously wait for a response
 *
 * @author Franck WOLFF
 */
public interface ResponseMessageFuture {

    /**
     * Cancel the current request
     * @return true if cancelled
     */
	public boolean cancel();

    /**
     * Wait synchronously for the response
     * @return response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
	public ResponseMessage get() throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Wait synchronously for the response. This method throws a TimeoutException if the
     * specified amount of time ({@code timeout}) has elapsed before the response is received.
     * If {@code timeout} is zero, however, then the method simply waits until the response
     * is received or the default transport timeout has elapsed.
     * 
     * @param timeout the maximum time to wait in milliseconds.
     * @return response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
	public ResponseMessage get(long timeout) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Is the request cancelled ?
     * @return true if cancelled
     */
	public boolean isCancelled();

    /**
     * Is the request finished ?
     * @return true if finished
     */
	public boolean isDone();
}
