/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.tomcat;

import flex.messaging.messages.Message;

import org.apache.catalina.CometEvent;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;
import org.granite.util.ContentType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Franck WOLFF
 */
public class GravityTomcatServlet extends AbstractCometProcessor {
	
    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(GravityTomcatServlet.class);

    
    @Override
	public CometIO createCometIO() {
		return new ByteArrayCometIO();
	}

	@Override
    public boolean handleRequest(CometEvent event, InputStream content) throws IOException, ServletException {

        HttpServletRequest request = event.getHttpServletRequest();
        HttpServletResponse response = event.getHttpServletResponse();

        GravityInternal gravity = (GravityInternal)GravityManager.getGravity(getServletContext());
		TomcatChannelFactory channelFactory = new TomcatChannelFactory(gravity);

        try {
            initializeRequest(gravity, request, response);

            Message[] amf3Requests = deserialize(gravity, request, content);

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
                
                // (Re)Connect message from tunnel...
                if (amf3Response == null) {
                    if (amf3Requests.length > 1)
                        throw new IllegalArgumentException("Only one connect request is allowed on tunnel.");

                    TomcatChannel channel = gravity.getChannel(channelFactory, channelId);
                    if (channel == null)
                		throw new NullPointerException("No channel on tunnel connect");
                    
                    // Try to send pending messages if any (using current container thread).
                    if (channel.runReceived(new AsyncHttpContext(request, response, amf3Request)))
                    	return true; // Some messages have been delivered, close http event.
                    
                    // No pending messages, wait for new ones or timeout (do not close http event).
                    setConnectMessage(request, amf3Request);
                    channel.setCometEvent(event);
                    return false;
                }

                if (amf3Responses == null)
                	amf3Responses = new Message[amf3Requests.length];
                amf3Responses[i] = amf3Response;
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)amf3Responses);

            serialize(gravity, response, amf3Responses, ContentType.forMimeType(request.getContentType()));
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
        	try {
        		if (content != null)
        			content.close();
        	}
        	finally {
        		cleanupRequest(request);
        	}
        }

        return true; // Close http event.
    }

    @Override
	public boolean handleEnd(CometEvent event) throws IOException, ServletException {
        return true; // Close http event.
	}

	@Override
    public boolean handleError(CometEvent event) throws IOException {
		if (EventUtil.isErrorButNotTimeout(event))
			log.warn("Got an error event: %s", EventUtil.toString(event));
        
        try {
    		HttpServletRequest request = event.getHttpServletRequest();
        	Message connect = getConnectMessage(request);
        	if (connect != null) { // This should be a timeout.
        		GravityInternal gravity = (GravityInternal)GravityManager.getGravity(getServletContext());
        		TomcatChannelFactory channelFactory = new TomcatChannelFactory(gravity);
        		
		        String channelId = (String)connect.getClientId();
	            TomcatChannel channel = gravity.getChannel(channelFactory, channelId);
	            
	            // Cancel channel's execution (timeout or other errors).
	            if (channel != null)
	            	channel.setCometEvent(null);
        	}
        }
        catch (Exception e) {
        	log.error(e, "Error while processing event: %s", EventUtil.toString(event));
        }
		
		return true; // Close http event.
    }
}
