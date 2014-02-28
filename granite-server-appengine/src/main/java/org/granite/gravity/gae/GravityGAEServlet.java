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
package org.granite.gravity.gae;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AbstractGravityServlet;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;

import com.google.apphosting.api.DeadlineExceededException;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;


/**
 * @author William DRAI
 */
public class GravityGAEServlet extends AbstractGravityServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(GravityGAEServlet.class);


    private static long GAE_TIMEOUT = 20000L;
    private static long GAE_POLLING_INTERVAL = 500L;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        GravityInternal gravity = (GravityInternal)GravityManager.getGravity(getServletContext());
		GAEChannelFactory channelFactory = new GAEChannelFactory((GAEGravity)gravity);
		
		try {
			// Setup context (thread local GraniteContext, etc.)
			initializeRequest(gravity, request, response);
			
			// New Request.
			Message[] amf3Requests = deserialize(gravity, request);

            log.debug(">> [AMF3 REQUESTS] %s", (Object)amf3Requests);

            Message[] amf3Responses = null;
            
            boolean accessed = false;
            for (int i = 0; i < amf3Requests.length; i++) {
                Message amf3Request = amf3Requests[i];
                
                // Ask gravity to create a specific response (will be null for connect request from tunnel).
                Message amf3Response = gravity.handleMessage(channelFactory, amf3Request);
                String channelId = (String)amf3Request.getClientId();
                
                // Mark current channel (if any) as accessed.
                if (!accessed)
                	accessed = gravity.access(channelId);
                
                // (Re)Connect message from tunnel.
                if (amf3Response == null) {
                    if (amf3Requests.length > 1)
                        throw new IllegalArgumentException("Only one request is allowed on tunnel.");

                	GAEChannel channel = gravity.getChannel(channelFactory, channelId);
                	if (channel == null)
                		throw new NullPointerException("No channel on connect");
                	
                	long pollingInterval = gravity.getGravityConfig().getExtra().get("gae/@polling-interval", Long.TYPE, GAE_POLLING_INTERVAL);

                    long initialTime = System.currentTimeMillis();
                    do {
    	                // Get messages or wait
    	                List<Message> messages = null;
    	                synchronized (channel) {
    	                    // Get pending messages from client queue
    	                    messages = channel.takeMessages();
    	                }
    	
    	                // Send the messages
    	                if (messages != null) {
    	                	amf3Responses = messages.toArray(new Message[0]);
    	                	((AsyncMessage)amf3Responses[i]).setCorrelationId(amf3Requests[i].getMessageId());
    	                	break;
    	                }
    	                
    	                try {
    	                	Thread.sleep(pollingInterval);
    	                }
    	                catch (InterruptedException e) {
    	                	break;
    	                }
    	                catch (DeadlineExceededException e) {
    	                	break;
    	                }
                    }
                    while (System.currentTimeMillis()-initialTime < GAE_TIMEOUT);
                    
                    if (amf3Responses == null)
                    	amf3Responses = new Message[0];
                }
                else {
	                if (amf3Responses == null)
	                	amf3Responses = new Message[amf3Requests.length];
	                amf3Responses[i] = amf3Response;
                }
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)amf3Responses);

            serialize(gravity, response, amf3Responses);
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
