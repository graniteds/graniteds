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
package org.granite.client.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.util.PublicByteArrayOutputStream;


public class MockRemoteService extends RemoteService {
    
    private static ResponseBuilder responseBuilder = null;
    private static boolean shouldFail = false;
    
    private static Executor executor = Executors.newSingleThreadExecutor();
    
    public MockRemoteService(RemotingChannel remotingChannel, String destination) {
    	super(remotingChannel, destination);
    }
    
    public static void setResponseBuilder(ResponseBuilder rb) {
    	responseBuilder = rb;
    }
    
    public static void setShouldFail(boolean shouldFail) {
    	MockRemoteService.shouldFail = shouldFail;
    }
    
    @Override
	public RemoteServiceInvocation newInvocation(String method, Object...parameters) {
		return new MockRemoteServiceInvocation(method, parameters);
	}
    
	public class MockRemoteServiceInvocation extends RemoteServiceInvocation {
		
		private final InvocationMessage request;
		private final List<ResponseListener> listeners = new ArrayList<ResponseListener>();
		
		public MockRemoteServiceInvocation(final String method, final Object...parameters) {
			super(MockRemoteService.this, method, parameters);
			request = new InvocationMessage(getId(), method, parameters);
		}

		@Override
		public RemoteServiceInvocationChain appendInvocation(String method, Object... parameters) {
			return null;
		}
		
		public RemoteServiceInvocation addListener(ResponseListener listener) {
			if (listener != null)
				this.listeners.add(listener);
			return this;
		}
		
		public RemoteServiceInvocation addListeners(ResponseListener...listeners) {
			if (listeners != null && listeners.length > 0)
				this.listeners.addAll(Arrays.asList(listeners));
			return this;
		}
		
		@Override
		public ResponseMessageFuture invoke() {
			final AsyncToken token = new AsyncToken(request, listeners.toArray(new ResponseListener[listeners.size()]));
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(50);
					} 
					catch (InterruptedException e1) {
					}
					
					PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(512);
					TransportMessage message = null;
					try {
						message = ((MockAMFRemotingChannel)getChannel()).createMessage(token);
						message.encode(os);
					}
					catch (IOException e) {
						throw new TransportException("Message serialization failed: " + message.getId(), e);
					}
					
					if (shouldFail) {
						token.dispatchFailure(new IOException("Connect failed"));
						shouldFail = false;
					}
					else {
						ResultMessage result = (ResultMessage)responseBuilder.buildResponseMessage(MockRemoteService.this, request);
						token.dispatchResult(result);
					}
				}
			});
			return token;
		}
	}

}
