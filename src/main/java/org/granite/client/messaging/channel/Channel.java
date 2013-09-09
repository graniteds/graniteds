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

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;

/**
 * @author Franck WOLFF
 */
public interface Channel {
	
    static final String RECONNECT_INTERVAL_MS_KEY = "reconnect-interval-ms";
    static final String RECONNECT_MAX_ATTEMPTS_KEY = "reconnect-max-attempts";
    
    static final long DEFAULT_TIME_TO_LIVE = TimeUnit.MINUTES.toMillis(1L); // 1 mn.
    
    static final String BYTEARRAY_BODY_HEADER = "GDS_BYTEARRAY_BODY";    

	Transport getTransport();
	String getId();
	URI getUri();
	String getClientId();
	
	long getDefaultTimeToLive();
	void setDefaultTimeToLive(long defaultTimeToLive);
	
	boolean start();
	boolean isStarted();
	boolean stop();
	
	void setCredentials(Credentials credentials);
	Credentials getCredentials();
	boolean isAuthenticated();
	
	ResponseMessageFuture send(RequestMessage request, ResponseListener... listeners);
	ResponseMessageFuture logout(ResponseListener... listeners);
	
	<D> D getTransportData();
	void setTransportData(Object data);

	void onMessage(InputStream is);
	void onError(TransportMessage message, Exception e);
	void onCancelled(TransportMessage message);
}
