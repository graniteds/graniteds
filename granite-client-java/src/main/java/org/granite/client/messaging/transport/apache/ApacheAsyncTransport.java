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
package org.granite.client.messaging.transport.apache;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportHttpStatusException;
import org.granite.client.messaging.transport.TransportIOException;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.TransportStateException;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;

/**
 * @author Franck WOLFF
 */
public class ApacheAsyncTransport extends AbstractTransport<Object> {
	
	private static final Logger log = Logger.getLogger(ApacheAsyncTransport.class);

	private CloseableHttpAsyncClient httpClient = null;
	private RequestConfig defaultRequestConfig = null;
	private BasicCookieStore cookieStore = new BasicCookieStore();
	
	public ApacheAsyncTransport() {
	}

	public void configure(HttpAsyncClientBuilder clientBuilder) {
		// Can be overwritten...
	}

	public RequestConfig getDefaultRequestConfig() {
		return defaultRequestConfig;
	}

	public void setDefaultRequestConfig(RequestConfig defaultRequestConfig) {
		this.defaultRequestConfig = defaultRequestConfig;
	}

	protected synchronized CloseableHttpAsyncClient getCloseableHttpAsyncClient() {
		return httpClient;
	}

    public boolean isReconnectAfterReceive() {
        return true;
    }

    public boolean isDisconnectAfterAuthenticationFailure() {
        return false;
    }

	@Override
	public synchronized boolean start() {
		if (httpClient != null)
			return true;
		
		log.info("Starting Apache HttpAsyncClient transport...");
		
		try {
			if (defaultRequestConfig == null)
				defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
			
			HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
			httpClientBuilder.setDefaultCookieStore(cookieStore);
			httpClientBuilder.setDefaultRequestConfig(defaultRequestConfig);
			configure(httpClientBuilder);
			httpClient = httpClientBuilder.build();
			
			httpClient.start();
			
			log.info("Apache HttpAsyncClient transport started.");
			return true;
		}
		catch (Exception e) {
			httpClient = null;
			getStatusHandler().handleException(new TransportException("Could not start Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to start.");
			return false;
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return httpClient != null;
	}

	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) throws TransportException {
		CloseableHttpAsyncClient httpClient = getCloseableHttpAsyncClient();
	    if (httpClient == null) {
	    	TransportException e = new TransportStateException("Apache HttpAsyncClient not started");
	    	getStatusHandler().handleException(e);
	    	throw e;
		}
	    
		if (!message.isConnect())
			getStatusHandler().handleIO(true);
		
		try {
		    HttpPost request = new HttpPost(channel.getUri());
			request.setHeader("Content-Type", message.getContentType());
			request.setHeader("GDSClientType", message.getClientType().toString());
			
			PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(512);
			try {
				message.encode(os);
			}
			catch (IOException e) {
				throw new TransportException("Message serialization failed: " + message.getId(), e);
			}
			request.setEntity(new ByteArrayEntity(os.getBytes(), 0, os.size()));
			
			final Future<HttpResponse> future = httpClient.execute(request, new FutureCallback<HttpResponse>() {
	
	            public void completed(HttpResponse response) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	                if (message.isDisconnect()) {
	                	channel.onDisconnect();
	                	return;
	                }
	            	
	            	if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	            		channel.onError(message, new TransportHttpStatusException(
	            			response.getStatusLine().getStatusCode(),
	            			response.getStatusLine().getReasonPhrase())
	            		);
	            		return;
	            	}
	            	
	        		InputStream is = null;
	        		try {
	        			is = response.getEntity().getContent();
	        			channel.onMessage(message, is);
	        		}
	        		catch (Exception e) {
		            	getStatusHandler().handleException(new TransportIOException(message, "Could not deserialize message", e));
					}
	        		finally {
	        			if (is != null) try {
	        				is.close();
	        			}
	        			catch (Exception e) {
	        			}
	        		}
	            }
	
	            public void failed(Exception e) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	                if (message.isDisconnect()) {
	                	channel.onDisconnect();
	                	return;
	                }
	                
                	channel.onError(message, e);
                	getStatusHandler().handleException(new TransportIOException(message, "Request failed", e));
	            }
	
	            public void cancelled() {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	                if (message.isDisconnect()) {
	                	channel.onDisconnect();
	                	return;
	                }
	            	
	            	channel.onCancelled(message);
	            }
	        });
			
			return new TransportFuture() {
				@Override
				public boolean cancel() {
					boolean cancelled = false;
					try {
						cancelled = future.cancel(true);
					}
					catch (Exception e) {
						log.error(e, "Cancel request failed");
					}
					return cancelled;
				}
			};
		}
		catch (Exception e) {
        	if (!message.isConnect())
        		getStatusHandler().handleIO(false);
			
			TransportIOException f = new TransportIOException(message, "Request failed", e);
        	getStatusHandler().handleException(f);
			throw f;
		}
	}

	@Override
	public synchronized void stop() {
		if (httpClient == null)
			return;
		
		log.info("Stopping Apache HttpAsyncClient transport...");

		super.stop();
		
		try {
			httpClient.close();
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not stop Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to stop properly.");
		}
		finally {
			httpClient = null;
		}
		
		log.info("Apache HttpAsyncClient transport stopped.");
	}
}
