/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.gravity.weblogic;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AbstractGravityServlet;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.ChannelFactory;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;
import org.granite.util.ServletParams;

import weblogic.servlet.http.AbstractAsyncServlet;
import weblogic.servlet.http.RequestResponseKey;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * Asynchronous servlet support for WebLogic 9.1+ servers.
 * <br /><br />
 * Usage:
 * <pre>
 * &lt;servlet&gt;
 *     &lt;servlet-name&gt;GravityServlet&lt;/servlet-name&gt;
 *     &lt;servlet-class&gt;org.granite.gravity.weblogic.GravityWebLogicServlet&lt;/servlet-class&gt;
 *     
 *     (optional parameter, default is 40,000 milliseconds)
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;scavangeInterval&lt;/param-name&gt;
 *         &lt;param-value&gt;40000&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *     
 * &lt;/servlet&gt;
 * </pre>
 * 
 * @author Franck WOLFF
 */
public class GravityWebLogicServlet extends AbstractAsyncServlet {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(GravityWebLogicServlet.class);

	private static final String SCAVANGE_INTERVAL = "scavangeInterval";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
		
		int scavangeInterval = ServletParams.get(
			config,
			SCAVANGE_INTERVAL,
			Integer.TYPE,
			AbstractAsyncServlet.DEFAULT_SCAVANGE_INTERVAL
		);
		
		log.info("Using scavange interval of: %s", scavangeInterval);
		AbstractAsyncServlet.setScavangeInterval(scavangeInterval);
	}

	@Override
	protected boolean doRequest(RequestResponseKey key) throws IOException, ServletException {
		Gravity gravity = GravityManager.getGravity(getServletContext());
		WebLogicChannelFactory channelFactory = new WebLogicChannelFactory(gravity);
        
		HttpServletRequest request = key.getRequest();
		HttpServletResponse response = key.getResponse();
		
		try {
            GravityServletUtil.initializeRequest(getServletConfig(), gravity, request, response);

            Message[] amf3Requests = GravityServletUtil.deserialize(gravity, request);

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

                    WebLogicChannel channel = gravity.getChannel(channelFactory, channelId);
                    if (channel == null)
                		throw new NullPointerException("No channel on tunnel connect");
                    
                    // Try to send pending messages if any (using current container thread).
                    if (channel.runReceived(new AsyncHttpContext(request, response, amf3Request)))
                    	return false;

                    // No pending messages, wait for new ones or timeout.
                	GravityServletUtil.setConnectMessage(request, amf3Request);
                	key.setTimeout((int)GravityServletUtil.getLongPollingTimeout(getServletContext()));
                	channel.setRequestResponseKey(key);
                	return true;
                }

                if (amf3Responses == null)
                	amf3Responses = new Message[amf3Requests.length];
                amf3Responses[i] = amf3Response;
            }

            log.debug("<< [AMF3 RESPONSES] %s", (Object)amf3Responses);

            GravityServletUtil.serialize(gravity, response, amf3Responses);
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
        	GravityServletUtil.cleanupRequest(request);
        }
		return false;
	}

	@Override
	protected void doResponse(RequestResponseKey key, Object context) throws IOException, ServletException {
		WebLogicChannel channel = (WebLogicChannel)context;
        HttpServletRequest request = key.getRequest();
        HttpServletResponse response = key.getResponse();
        Message requestMessage = AbstractGravityServlet.getConnectMessage(request);
		
		channel.runReceived(new AsyncHttpContext(request, response, requestMessage));
	}

	@Override
	protected void doTimeout(RequestResponseKey key) throws IOException, ServletException {
		Gravity gravity = GravityManager.getGravity(getServletContext());
		WebLogicChannelFactory channelFactory = new WebLogicChannelFactory(gravity);
		
		CommandMessage amf3Request = GravityServletUtil.getConnectMessage(key.getRequest());
		String channelId = (String)amf3Request.getClientId();
		WebLogicChannel channel = gravity.getChannel(channelFactory, channelId);
		channel.setRequestResponseKey(null);
	}
}
