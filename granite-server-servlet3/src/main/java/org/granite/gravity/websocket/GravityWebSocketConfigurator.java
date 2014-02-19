package org.granite.gravity.websocket;

import org.granite.logging.Logger;
import org.granite.util.ContentType;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;

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
        String connectMessageId = request.getHeaders().get("connectId") != null
                ? request.getHeaders().get("connectId").get(0)
                : (request.getHeaders().get("connectid") != null
                    ? request.getHeaders().get("connectid").get(0)
                    : (request.getParameterMap().get("connectId") != null ? request.getParameterMap().get("connectId").get(0) : null));
        String clientId = request.getHeaders().get("GDSClientId") != null
                ? request.getHeaders().get("GDSClientId").get(0)
                : (request.getHeaders().get("gdsclientid") != null
                    ? request.getHeaders().get("gdsclientid").get(0)
                    : (request.getParameterMap().get("GDSClientId") != null ? request.getParameterMap().get("GDSClientId").get(0) : null));
        String clientType = request.getHeaders().get("GDSClientType") != null
                ? request.getHeaders().get("GDSClientType").get(0)
                : (request.getHeaders().get("gdsclienttype") != null
                    ? request.getHeaders().get("gdsclienttype").get(0)
                    : (request.getParameterMap().get("GDSClientType") != null ? request.getParameterMap().get("GDSClientType").get(0) : null));

        HttpSession session = (HttpSession)request.getHttpSession();

        log.debug("WebSocket configurator handshake ackId %s clientId %s sessionId %s", connectMessageId, clientId, session != null ? session.getId() : "(none)");

        String ctype = request.getHeaders().get("Content-Type") != null
                ? request.getHeaders().get("Content-Type").get(0)
                : (request.getHeaders().get("content-type") != null ? request.getHeaders().get("content-type").get(0) : null);
        String protocol = null;
        if (request.getHeaders().get("Sec-WebSocket-Protocol") != null)
            protocol = request.getHeaders().get("Sec-WebSocket-Protocol").get(0);
        else if (request.getHeaders().get("sec-websocket-protocol") != null)    // Tomcat
            protocol = request.getHeaders().get("sec-websocket-protocol").get(0);
        if (ctype == null && protocol != null)
            ctype = "application/x-" + protocol.substring("org.granite.gravity.".length());

        ContentType contentType = ContentType.forMimeType(ctype);
        if (contentType == null) {
            log.warn("No (or unsupported) content type in request: %s", ctype);
            contentType = ContentType.AMF;
        }

        // Hack using a thread local to be sure that the endpoint gets the correct values
        // Jetty and GlassFish
        GravityWebSocketConfig.set(connectMessageId, clientId, clientType, contentType, session);
    }
}
