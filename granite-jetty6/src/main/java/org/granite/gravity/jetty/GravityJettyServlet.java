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
package org.granite.gravity.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AbstractGravityServlet;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class GravityJettyServlet extends AbstractGravityServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(GravityJettyServlet.class);


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	log.debug("doPost: from %s:%d", request.getRemoteAddr(), request.getRemotePort());

		Gravity gravity = GravityManager.getGravity(getServletContext());
		ContinuationChannelFactory channelFactory = new ContinuationChannelFactory(gravity);
		
		try {
			// Setup context (thread local GraniteContext, etc.)
			initializeRequest(gravity, request, response);
			
			AsyncMessage connect = getConnectMessage(request);
			
			// Resumed request (pending messages or timeout).
			if (connect != null) {
				String channelId = (String)connect.getClientId();
				ContinuationChannel channel = gravity.getChannel(channelFactory, channelId);

				// Reset channel continuation instance and deliver pending messages.
				synchronized (channel) {
					channel.close();
					channel.runReceived(new AsyncHttpContext(request, response, connect));
				}
				
				return;
			}
			
			// New Request.
			Message[] amf3Requests = deserialize(gravity, request);

            log.debug(">> [AMF3 REQUESTS] %s", (Object)amf3Requests);

            Message[] amf3Responses = null;
            
            boolean accessed = false;
            for (int i = 0; i < amf3Requests.length; i++) {
                Message amf3Request = amf3Requests[i];
                
                // Ask gravity to create a specific response (will be null with a connect request from tunnel).
                Message amf3Response = gravity.handleMessage(channelFactory, amf3Request);
                String channelId = (String)amf3Request.getClientId();
                
                // Mark current channel (if any) as accessed.
                if (!accessed)
                	accessed = gravity.access(channelId);
                
                // (Re)Connect message from tunnel.
                if (amf3Response == null) {
                    if (amf3Requests.length > 1)
                        throw new IllegalArgumentException("Only one request is allowed on tunnel.");

                	ContinuationChannel channel = gravity.getChannel(channelFactory, channelId);
                	if (channel == null)
                		throw new NullPointerException("No channel on tunnel connect");
                	
                    // Try to send pending messages if any (using current container thread).
                	if (!channel.runReceived(new AsyncHttpContext(request, response, amf3Request))) {
                        
                		// No pending messages, wait for new ones or timeout.
	                    setConnectMessage(request, amf3Request);
	                	synchronized (channel) {
	                		Continuation continuation = ContinuationSupport.getContinuation(request, channel);
		                	channel.setContinuation(continuation);
		                	
		                	// This call throws a RetryRequest exception, so we'll never reach return below...
		                	continuation.suspend(getLongPollingTimeout());
	                	}
                	}
                	
                	return;
                }

                if (amf3Responses == null)
                	amf3Responses = new Message[amf3Requests.length];
                amf3Responses[i] = amf3Response;
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)amf3Responses);

            serialize(gravity, response, amf3Responses);
		}
        catch (RetryRequest e) {
            throw e;
        }
        catch (IOException e) {
            log.error(e, "Gravity message error");
            throw e;
        }
        catch (Exception e) {
            log.error(e, "Gravity message error");
            throw new ServletException(e);
        }
		finally {
			// Cleanup context (thread local GraniteContext, etc.)
			cleanupRequest(request);
		}
    }
}
