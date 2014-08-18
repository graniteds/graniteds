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

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.messages.ResponseMessage;

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
	
	/**
	 * Register a listener which will be notified of messages occuring on the channel
	 * @param listener listener
	 */
	public void addListener(ChannelResponseListener listener);
	
	/**
	 * Unregister a listener which will be notified of messages occuring on the channel
	 * @param listener listener
	 */
	public void removeListener(ChannelResponseListener listener);
	
	
	public static interface ChannelResponseListener {
		
		public void onResponse(ResponseMessage responseMessage);
	}
}
