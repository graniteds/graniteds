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

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;

/**
 * Interface for communication channels
 * A channel handles the protocol (ping, reconnections, ...) between the client and the server
 * A Channel should be created using a ChannelFactory
 *
 * @author Franck WOLFF
 */
public interface Channel extends ChannelStatusNotifier {
	
    static final String RECONNECT_INTERVAL_MS_KEY = "reconnect-interval-ms";
    static final String RECONNECT_MAX_ATTEMPTS_KEY = "reconnect-max-attempts";
    
    static final long DEFAULT_TIME_TO_LIVE = TimeUnit.MINUTES.toMillis(1L); // 1 mn.
    
    static final String BYTEARRAY_BODY_HEADER = "GDS_BYTEARRAY_BODY";

    /**
     * Transport used by this channel
     * @return transport
     */
	Transport getTransport();

    /**
     * Channel identifier
     * @return id
     */
	String getId();

    /**
     * Server application uri
     * @return uri
     */
	URI getUri();

    /**
     * Internal client id received from the server on the first ping
     * @return client id
     */
	String getClientId();
	
    /**
     * Default time to live for messages sent from this channel
     * @return default time to live in milliseconds
     */
	long getDefaultTimeToLive();

    /**
     * Set the default time to live for messages sent from this channel
     * @param defaultTimeToLive default time to live
     */
	void setDefaultTimeToLive(long defaultTimeToLive);

    /**
     * Start the channel. This is where the channel can initialize its internal resources, thread pools...
     * @return true if the channel has correctly started
     */
	boolean start();

    /**
     * @return true if the channel is started
     */
	boolean isStarted();

    /**
     * Stop the channel
     * @return true if the channel has been stopped, false if it was not started before the method was called
     */
	boolean stop();

    /**
     * Set the security credentials for this channel
     * Once the credentials are set, the next {@link #send} will trigger the authentication with the server
     * @param credentials credentials
     */
	void setCredentials(Credentials credentials);

    /**
     * Current security credentials for this channel
     * @return credentials
     */
	Credentials getCredentials();

    /**
     * True if the channel has been authenticated (exchange of a login message)
     * @return true if authenticated
     */
	boolean isAuthenticated();
	
    /**
     * Send a message on this channel
     * @param request message to send
     * @param listeners array of listeners to notify asynchronously when the response is received
     * @return future that will be triggered when the response is received
     */
	ResponseMessageFuture send(RequestMessage request, ResponseListener... listeners);

    /**
     * Logout from the server application
     * Equivalent to {@link #logout(boolean,org.granite.client.messaging.ResponseListener...)} with sendLogout true
     * @param listeners array of listeners to notify asynchronously when the response is received
     * @return future that will be triggered when the response is received
     */
	ResponseMessageFuture logout(ResponseListener... listeners);

    /**
     * Logout from the server application.
     * If sendLogout is false, the logout message will not be sent to the server so this method can also be used
     * when you know that the session is lost (expired session, lost connection, ...)
     * @param sendLogout if true a logout message will be sent
     * @param listeners array of listeners to notify asynchronously when the response is received
     * @return future that will be triggered when the response is received
     */
    ResponseMessageFuture logout(boolean sendLogout, ResponseListener... listeners);

    /**
     * Internal data object that can be used by a transport to store channel-specific state
     * @param <D> type of transport data
     * @return transport state object
     */
	<D> D getTransportData();

    /**
     * Set the channel-specific state object for the transport
     * @param data transport state object
     */
	void setTransportData(Object data);

    /**
     * Callback method called by the transport when a message is received
     * @param is data input stream of the incoming message
     */
	void onMessage(TransportMessage message, InputStream is);

    /**
     * Callback method called by the transport when it is disconnecting
     */
	void onDisconnect();

    /**
     * Callback method called by the transport when an exception occurs during communication
     * @param message message triggering the error
     * @param e exception throws during send of message
     */
	void onError(TransportMessage message, Exception e);

    /**
     * Callback method called by the transport when a message send is cancelled
     * @param message message cancelled
     */
	void onCancelled(TransportMessage message);
	
	
	public TransportMessage createConnectMessage(String id, boolean reconnect);
	
	
	public void bindStatus(ChannelStatusNotifier notifier);
	
	public void unbindStatus(ChannelStatusNotifier notifier);
	
	public void addListener(ChannelStatusListener listener);
	
	public void removeListener(ChannelStatusListener listener);
}
