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
package org.granite.client.messaging;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.granite.client.messaging.events.AbstractResponseEvent;
import org.granite.client.messaging.events.CancelledEvent;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public final class ResponseListenerDispatcher {
	
	private static final Logger log = Logger.getLogger(ResponseListenerDispatcher.class);
	
	private ResponseListenerDispatcher() {
		throw new RuntimeException("Not instanciable");
	}
	
	public static void dispatch(ResponseListener listener, Event event) {
		if (listener == null || event == null)
			throw new NullPointerException("listener and event cannot be null");
		
		boolean unknownEventType = false;
		
		try {
			switch (event.getType()) {
				case RESULT:
					listener.onResult((ResultEvent)event);
					break;
				case FAULT:
					listener.onFault((FaultEvent)event);
					break;
				case FAILURE:
					listener.onFailure((FailureEvent)event);
					break;
				case TIMEOUT:
					listener.onTimeout((TimeoutEvent)event);
					break;
				case CANCELLED:
					listener.onCancelled((CancelledEvent)event);
					break;
				default:
					unknownEventType = true;
					break;
			}
		}
		catch (Exception e) {
			log.error(e, "ResponseListener %s threw an exception for event %s", listener, event);
		}
		
		if (unknownEventType) {
			RuntimeException e = new RuntimeException("Unknown event type: " + event);
			log.error(e, "");
			throw e;
		}
	}
	
	public static ResponseMessage getResponseMessage(Event event) throws InterruptedException, ExecutionException, TimeoutException {
		if (event == null)
			throw new NullPointerException("event cannot be null");

		switch (event.getType()) {
			case RESULT: case FAULT:
				return ((AbstractResponseEvent<?>)event).getResponse();
			case FAILURE:
				throw new ExecutionException(((FailureEvent)event).getCause());
			case TIMEOUT:
				throw new TimeoutException(((TimeoutEvent)event).toString());
			case CANCELLED:
				throw new InterruptedException(((CancelledEvent)event).toString());
			default: {
				RuntimeException e = new RuntimeException("Unknown event type: " + event);
				log.error(e, "");
				throw e;
			}
		}
	}
}
