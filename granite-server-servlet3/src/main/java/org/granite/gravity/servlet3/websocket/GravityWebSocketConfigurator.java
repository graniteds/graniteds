package org.granite.gravity.servlet3.websocket;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
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
        log.debug("WebSocket configurator handshake %s", config);

        String connectMessageId = request.getHeaders().get("connectId") != null
                ? request.getHeaders().get("connectId").get(0)
                : (request.getParameterMap().get("connectId") != null ? request.getParameterMap().get("connectId").get(0) : null);
        String clientId = request.getHeaders().get("GDSClientId") != null
                ? request.getHeaders().get("GDSClientId").get(0)
                : (request.getParameterMap().get("GDSClientId") != null ? request.getParameterMap().get("GDSClientId").get(0) : null);
        String clientType = request.getHeaders().get("GDSClientType") != null
                ? request.getHeaders().get("GDSClientType").get(0)
                : (request.getParameterMap().get("GDSClientType") != null ? request.getParameterMap().get("GDSClientType").get(0) : null);

        HttpSession session = (HttpSession)request.getHttpSession();

        String ctype = request.getHeaders().get("Content-Type") != null ? request.getHeaders().get("Content-Type").get(0) : null;
        String protocol = request.getHeaders().get("Sec-WebSocket-Protocol").get(0);
        if (ctype == null && protocol != null)
            ctype = "application/x-" + protocol.substring("org.granite.gravity.".length());

        ContentType contentType = ContentType.forMimeType(ctype);
        if (contentType == null) {
            log.warn("No (or unsupported) content type in request: %s", ctype);
            contentType = ContentType.AMF;
        }

        // Hack using a thread local to be sure that the endpoint gets the correct values
        GravityWebSocketConfig.set(connectMessageId, clientId, clientType, contentType, session);
    }
}
