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
package org.granite.client.messaging.transport;

import org.granite.client.messaging.channel.Channel;

/**
 * Transport is a SPI describing a communication layer handling a low-level network protocol
 * It is used by a Channel to actually transmit the messages to or from the remote server
 *
 * Usually transports are implicitly defined for the current {@link org.granite.client.platform.Platform} and created
 * internally by a {@link org.granite.client.messaging.channel.ChannelFactory} but it's possible to define custom
 * transport and register them
 *
 * @author Franck WOLFF
 */
public interface Transport {

    /**
     * Set the current context for this transport (usually depends on the {@link org.granite.client.platform.Platform})
     * @param context current context
     *
     */
	void setContext(Object context);

    /**
     * The generic context object for this transport (actual type of the context depends on the {@link org.granite.client.platform.Platform})
     * @return current context
     */
	Object getContext();

    /**
     * Start the transport and enable transmission of data on the network
     * If there is an exception during start, the status handler is notified {@link org.granite.client.messaging.transport.TransportStatusHandler#handleException}
     * @return true if correctly started, false if the transport could not start
     */
	boolean start();

    /**
     * Is the transport started ?
     * @return true if started
     */
	boolean isStarted();

    /**
     * Stop the transport and stop any communication on the network
     */
	void stop();

    /**
     * Indicates to the calling Channel that the transport should reconnect when data has been received
     * @return true if the transport should reconnect
     */
    boolean isReconnectAfterReceive();

    /**
     * Attach a status handler that will be notified of network activity and errors
     * @param statusHandler status handler
     * @see org.granite.client.messaging.transport.TransportStatusHandler
     */
	void setStatusHandler(TransportStatusHandler statusHandler);

    /**
     * Get the status handler for this transport
     * @return current status handler
     * @see org.granite.client.messaging.transport.TransportStatusHandler
     */
	TransportStatusHandler getStatusHandler();

    /**
     * Register a stop listener for this transport
     * @param listener stop listener
     * @see org.granite.client.messaging.transport.TransportStopListener
     */
	void addStopListener(TransportStopListener listener);

    /**
     * Unregister a stop listener for this transport
     * @param listener stop listener
     * @return true if the listener was unregistered, false if it was not present
     * @see org.granite.client.messaging.transport.TransportStopListener
     */
	boolean removeStopListener(TransportStopListener listener);

    /**
     * Send a message on the network
     * @param channel originating channel
     * @param message message to send
     * @return future object that will be triggered asynchronously when the message is sent
     * @throws TransportException when an error occurs during send
     */
	TransportFuture send(Channel channel, TransportMessage message) throws TransportException;
}
