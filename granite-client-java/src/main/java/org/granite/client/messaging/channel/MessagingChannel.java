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

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResponseListener;

/**
 * SPI for messaging channels
 * A messaging channel adds some functionality to channel, mostly consumer management
 *
 * @author Franck WOLFF
 */
public interface MessagingChannel extends Channel, SessionAwareChannel {

    /**
     * Set the current session id
     * Necessary to synchronize session ids between remoting and messaging channels, usually the remoting channel
     * acts as the master channel which handles authentication and session management and propagated it to other
     * messaging channels
     * @param sessionId session id
     */
	void setSessionId(String sessionId);

    /**
     * Register a consumer for this channel
     * @param consumer consumer
     * @see org.granite.client.messaging.Consumer
     */
	void addConsumer(Consumer consumer);

    /**
     * Unregister a consumer for this channel
     * @param consumer consumer
     * @return true if the consumer was registered before the method was called
     * @see org.granite.client.messaging.Consumer
     */
	boolean removeConsumer(Consumer consumer);

    /**
     * Disconnect the channel
     * @param listeners array of listener to notify when the channel is disconnected
     * @return future that will be triggered when the channel is disconnected
     */
	public ResponseMessageFuture disconnect(ResponseListener...listeners);
}
