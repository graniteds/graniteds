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
package org.granite.client.android.messaging.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.*;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

/**
 * @author Franck WOLFF
 */
public class LoopjTransport extends AbstractTransport<Context> {

	private static final Logger log = Logger.getLogger(LoopjTransport.class);
	
    private AsyncHttpClient httpClient = null;
    private PersistentCookieStore cookieStore = null;


    public LoopjTransport() {
    }

    public LoopjTransport(Context context) {
        super(context);
    }

    public boolean isReconnectAfterReceive() {
        return true;
    }

    @Override
    public synchronized boolean start() {

        if (httpClient != null)
            return true;
        
        log.debug("Starting loopj transport...");
        
        Context context = getContext();
        if (context == null)
        	throw new IllegalStateException("Android application context is null");

        httpClient = new AsyncHttpClient();
        httpClient.setTimeout(60000);	// Change default to allow long-polling tunnel
        
        cookieStore = new PersistentCookieStore(context);
        httpClient.setCookieStore(cookieStore);

        log.debug("Loopj transport started");

        return true;
    }

    @Override
    public synchronized boolean isStarted() {
        return httpClient != null;
    }
    
    protected synchronized AsyncHttpClient getAsyncHttpClient() {
    	return httpClient;
    }

    @Override
    public TransportFuture send(final Channel channel, final TransportMessage message) throws TransportException {
    	
    	AsyncHttpClient httpClient = getAsyncHttpClient();
		if (httpClient == null) {
	    	TransportException e = new TransportStateException("Apache HttpAsyncClient not started");
	    	getStatusHandler().handleException(e);
	    	throw e;
		}
		
		Context context = getContext();
		if (context == null) {
	    	TransportException e = new TransportStateException("Android context is null");
	    	getStatusHandler().handleException(e);
	    	throw e;
		}

        PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(512);
        try {
            message.encode(os);
        }
        catch (IOException e) {
            throw new TransportException("Message serialization failed: " + message.getId(), e);
        }

        HttpEntity entity = new ByteArrayEntity(Arrays.copyOfRange(os.getBytes(), 0, os.size()));

        Header[] headers = new Header[] {
            new BasicHeader("GDSClientType", message.getClientType().toString())
        };

        log.trace("Posting request to %s", channel.getUri());
        
        httpClient.post(context, channel.getUri().toString(), headers, entity, message.getContentType(), new BinaryHttpResponseHandler(new String[]{message.getContentType()}) {
        	
        	private boolean handled = false;

            @Override
            public void onStart() {
                if (!message.isConnect())
                    getStatusHandler().handleIO(true);
            }

            @Override
            public void onSuccess(int statusCode, byte[] bytes) {
                handled = true;
                if (statusCode != HttpStatus.SC_OK) {
                    channel.onError(message, new TransportHttpStatusException(statusCode, "HTTP Error: " + statusCode));
                    return;
                }
                try {
                    channel.onMessage(message, new ByteArrayInputStream(bytes));
                }
                catch (Exception e) {
                    getStatusHandler().handleException(new TransportIOException(message, "Could not deserialize message", e));
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                handled = true;
                Exception e = new TransportException(throwable);
                channel.onError(message, new TransportException(e));
                getStatusHandler().handleException(new TransportIOException(message, "Request failed", e));
            }

            @Override
            public void onFinish() {
            	if (!handled)
            		channel.onMessage(message, new ByteArrayInputStream(new byte[0]));
            	
                if (!message.isConnect())
                    getStatusHandler().handleIO(false);
            }
        });

        return new TransportFuture() {
            @Override
            public boolean cancel() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public synchronized void stop() {
        if (httpClient != null) {
            log.debug("Stopping loopj transport...");

            try {
                httpClient.cancelRequests(getContext(), true);
            }
            finally {
                httpClient = null;
            }

            log.debug("Loopj transport stopped");
        }
    }
}
