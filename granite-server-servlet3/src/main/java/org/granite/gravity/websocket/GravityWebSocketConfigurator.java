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
package org.granite.gravity.websocket;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.util.ContentType;

/**
 * Created by william on 12/02/14.
 */
public class GravityWebSocketConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger log = Logger.getLogger(GravityWebSocketConfigurator.class);

    @Override
    public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
        for (String p : requested) {
            if (supported.contains(p))
                return p;
        }
        return null;
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {

    	// Tomcat websocket impl returns request headers in lowercase ????
    	String connectMessageId = getHeaderOrParameter(request, "connectId", true);
    	String clientId = getHeaderOrParameter(request, "GDSClientId", true);
    	String clientType = getHeaderOrParameter(request, "GDSClientType", true);

        HttpSession session = (HttpSession)request.getHttpSession();

        log.debug(
        	"WebSocket configurator handshake ackId %s clientId %s sessionId %s",
        	connectMessageId, clientId, session != null ? session.getId() : "(none)"
        );

        Gravity gravity = (Gravity)config.getUserProperties().get("gravity");
        if (gravity.getGraniteConfig().getSecurityService() != null)
            gravity.getGraniteConfig().getSecurityService().prelogin(session, request, null);

        String ctype = getHeader(request, "Content-Type", true);
        
        if (ctype == null) {
            String protocol = getHeader(request, "Sec-WebSocket-Protocol", true);
        	
            if (protocol != null) {
        		if ("org.granite.gravity".equals(protocol))
        			ctype = ContentType.AMF.mimeType();
        		else if (protocol.startsWith("org.granite.gravity."))
        			ctype = "application/x-" + protocol.substring("org.granite.gravity.".length());
        		else
        			throw new RuntimeException("Missing Content-Type and unsupported Sec-WebSocket-Protocol: " + protocol);
        	}
            else {
            	ctype = ContentType.AMF.mimeType();
            	log.warn("Missing Content-Type and Sec-WebSocket-Protocol in handshake request. Using: %s", ctype);
            }
        }

        ContentType contentType = ContentType.forMimeType(ctype);
        if (contentType == null) {
            log.warn("No (or unsupported) content type in handshake request: %s. Using: %s", ctype, ContentType.AMF.mimeType());
            contentType = ContentType.AMF;
        }

        // Hack using a thread local to be sure that the endpoint gets the correct values
        // Jetty and GlassFish
        GravityWebSocketConfig.set(connectMessageId, clientId, clientType, contentType, session);
    }
    
    private String getHeader(HandshakeRequest request, String key, boolean lower) {
    	List<String> values = request.getHeaders().get(key);
    	if (values != null && values.size() > 0)
    		return values.get(0);
    	if (lower) {
	    	values = request.getHeaders().get(key.toLowerCase());
	    	if (values != null && values.size() > 0)
	    		return values.get(0);
    	}
    	return null;
    }
    
    private String getParameter(HandshakeRequest request, String key) {
    	List<String> values = request.getParameterMap().get(key);
    	if (values != null && values.size() > 0)
    		return values.get(0);
    	return null;
    }
    
    private String getHeaderOrParameter(HandshakeRequest request, String key, boolean lower) {
    	String value = getHeader(request, key, lower);
    	if (value == null)
    		value = getParameter(request, key);
    	return value;
    }
}
