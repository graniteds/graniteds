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
package org.granite.gravity.tomcat;

import java.lang.reflect.Field;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsHttpServletRequestWrapper;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class TomcatWebSocketServlet extends WebSocketServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(TomcatWebSocketServlet.class);

    private static Field requestField = null;
    static {
        try {
            requestField = WsHttpServletRequestWrapper.class.getDeclaredField("request");
            requestField.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
        }
    }
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
	}
	
	@Override
	protected String selectSubProtocol(List<String> subProtocols) {
        for (String protocol : subProtocols) {
            if (protocol.startsWith("org.granite.gravity"))
                return protocol;
        }
		return null;
	}
	
	@Override
	protected StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request) {
		Gravity gravity = GravityManager.getGravity(getServletContext());
		TomcatWebSocketChannelFactory channelFactory = new TomcatWebSocketChannelFactory(gravity);
		
		try {
			String connectMessageId = request.getHeader("connectId") != null ? request.getHeader("connectId") : request.getParameter("connectId");
			String clientId = request.getHeader("GDSClientId") != null ? request.getHeader("GDSClientId") : request.getParameter("GDSClientId");
			String clientType = request.getHeader("GDSClientType") != null ? request.getHeader("GDSClientType") : request.getParameter("GDSClientType");
			String sessionId = null;
			HttpSession session = request.getSession(false);
            ServletGraniteContext graniteContext = null;
			if (session != null) {
                graniteContext = ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
		        	getServletContext(), session, clientType);
		        
				sessionId = session.getId();
			}
			else if (request.getCookies() != null) {
				for (int i = 0; i < request.getCookies().length; i++) {
					if ("JSESSIONID".equals(request.getCookies()[i].getName())) {
						sessionId = request.getCookies()[i].getValue();
						break;
					}
				}

                graniteContext = ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
	        		getServletContext(), sessionId, clientType); 
			}
            else {
                graniteContext = ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
                        getServletContext(), (String)null, clientType);
            }

            log.info("WebSocket connection started %s clientId %s sessionId %s", protocol, clientId, sessionId);
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			if (clientId != null)
				pingMessage.setClientId(clientId);
			
			Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
            if (sessionId != null)
                ackMessage.setHeader("JSESSIONID", sessionId);

			TomcatWebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
            channel.setSession(session);

            if (gravity.getGraniteConfig().getSecurityService() != null) {
                try {
                    gravity.getGraniteConfig().getSecurityService().prelogin(session, request instanceof WsHttpServletRequestWrapper ? requestField.get(request) : request);
                }
                catch (IllegalAccessException e) {
                    log.warn(e, "Could not get internal request object");
                }
            }

            String ctype = request.getContentType();
            if (ctype == null && protocol.length() > "org.granite.gravity".length())
                ctype = "application/x-" + protocol.substring("org.granite.gravity.".length());

            ContentType contentType = ContentType.forMimeType(ctype);
			if (contentType == null) {
				log.warn("No (or unsupported) content type in request: %s", request.getContentType());
				contentType = ContentType.AMF;
			}
			channel.setContentType(contentType);
			
			if (!ackMessage.getClientId().equals(clientId))
				channel.setConnectAckMessage(ackMessage);
			
			return channel.getStreamInbound();
		}
		finally {
			GraniteContext.release();
		}
	}
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//
//        // Information required to send the server handshake message
//        String key;
//        String subProtocol = null;
//        List<String> extensions = Collections.emptyList();
//
//        if (!headerContainsToken(req, "upgrade", "websocket")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//
//        if (!headerContainsToken(req, "connection", "upgrade")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//
//        if (!headerContainsToken(req, "sec-websocket-version", "13")) {
//            resp.setStatus(426);
//            resp.setHeader("Sec-WebSocket-Version", "13");
//            return;
//        }
//
//        key = req.getHeader("Sec-WebSocket-Key");
//        if (key == null) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//
//        String origin = req.getHeader("Origin");
//        if (!verifyOrigin(origin)) {
//            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }
//
//        // Fix for Tomcat-7.0.29 bad header name (was Sec-WebSocket-Protocol-Client")
//        List<String> subProtocols = getTokensFromHeader(req, "Sec-WebSocket-Protocol");
//        if (!subProtocols.isEmpty())
//            subProtocol = selectSubProtocol(subProtocols);
//
//        // TODO Read client handshake - Sec-WebSocket-Extensions
//
//        // TODO Extensions require the ability to specify something (API TBD)
//        //      that can be passed to the Tomcat internals and process extension
//        //      data present when the frame is fragmented.
//
//        // If we got this far, all is good. Accept the connection.
//        resp.setHeader("Upgrade", "websocket");
//        resp.setHeader("Connection", "upgrade");
//        resp.setHeader("Sec-WebSocket-Accept", getWebSocketAccept(key));
//        if (subProtocol != null)
//            resp.setHeader("Sec-WebSocket-Protocol", subProtocol);
//
//        if (!extensions.isEmpty()) {
//            // TODO
//        }
//
//        WsHttpServletRequestWrapper wrapper = new WsHttpServletRequestWrapper(req);
//        StreamInbound inbound = createWebSocketInbound(subProtocol, wrapper);
//        wrapper.invalidate();
//
//        // Hack to avoid chunked transfer
//        resp.setContentLength(((TomcatWebSocketChannel.MessageInboundImpl)inbound).getAckLength());
//
//        // Small hack until the Servlet API provides a way to do this.
//        ServletRequest inner = req;
//        // Unwrap the request
//        while (inner instanceof ServletRequestWrapper)
//            inner = ((ServletRequestWrapper)inner).getRequest();
//
//        if (inner instanceof RequestFacade)
//            ((RequestFacade)inner).doUpgrade(inbound);
//        else
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sm.getString("servlet.reqUpgradeFail"));
//    }
//
//
//    private boolean headerContainsToken(HttpServletRequest req,
//            String headerName, String target) {
//        Enumeration<String> headers = req.getHeaders(headerName);
//        while (headers.hasMoreElements()) {
//            String header = headers.nextElement();
//            String[] tokens = header.split(",");
//            for (String token : tokens) {
//                if (target.equalsIgnoreCase(token.trim())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    private List<String> getTokensFromHeader(HttpServletRequest req,
//            String headerName) {
//        List<String> result = new ArrayList<String>();
//
//        Enumeration<String> headers = req.getHeaders(headerName);
//        while (headers.hasMoreElements()) {
//            String header = headers.nextElement();
//            String[] tokens = header.split(",");
//            for (String token : tokens) {
//                result.add(token.trim());
//            }
//        }
//        return result;
//    }
//
//	private String getWebSocketAccept(String key) throws ServletException {
//
//        MessageDigest sha1Helper = sha1Helpers.poll();
//        if (sha1Helper == null) {
//            try {
//                sha1Helper = MessageDigest.getInstance("SHA1");
//            } catch (NoSuchAlgorithmException e) {
//                throw new ServletException(e);
//            }
//        }
//
//        sha1Helper.reset();
//        sha1Helper.update(key.getBytes(B2CConverter.ISO_8859_1));
//        String result = Base64.encode(sha1Helper.digest(WS_ACCEPT));
//
//        sha1Helpers.add(sha1Helper);
//
//        return result;
//    }
}
