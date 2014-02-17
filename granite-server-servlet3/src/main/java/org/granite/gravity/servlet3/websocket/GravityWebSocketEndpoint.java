package org.granite.gravity.servlet3.websocket;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
* Created by william on 12/02/14.
*/
public class GravityWebSocketEndpoint extends Endpoint {

    private static final Logger log = Logger.getLogger(GravityWebSocketEndpoint.class);

    private final GravityWebSocketConfig config;

    public GravityWebSocketEndpoint() {
        config = GravityWebSocketConfig.remove();
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        Gravity gravity = (Gravity)endpointConfig.getUserProperties().get("gravity");
        ServletContext servletContext = (ServletContext)endpointConfig.getUserProperties().get("servletContext");

        String connectMessageId = config.connectMessageId;
        String clientId = config.clientId;
        String clientType = config.clientType;
        ContentType contentType = config.contentType;
        HttpSession httpSession = config.session;

        try {
            String sessionId = null;
            if (httpSession != null) {
                ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
                        servletContext, httpSession, clientType);

                sessionId = httpSession.getId();
            }
            else {
                ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
                        servletContext, (String)null, clientType);
            }

            CommandMessage pingMessage = new CommandMessage();
            pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
            pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
            if (clientId != null)
                pingMessage.setClientId(clientId);

            WebSocketChannelFactory channelFactory = new WebSocketChannelFactory(gravity);
            Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
            if (sessionId != null)
                ackMessage.setHeader("JSESSIONID", sessionId);

            log.info("WebSocket connection started connectId %s clientId %s ackClientId %s sessionId %s", pingMessage.getMessageId(), clientId, ackMessage.getClientId(), sessionId);

            WebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
            channel.setSession(httpSession);

            channel.setContentType(contentType);

            channel.setConnectAckMessage(ackMessage);

            session.getUserProperties().put("channel", channel);

            session.setMaxIdleTimeout(channel.getGravity().getGravityConfig().getChannelIdleTimeoutMillis());
            channel.onWebSocketConnect(session);
        }
        finally {
            GraniteContext.release();
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        WebSocketChannel channel = (WebSocketChannel)session.getUserProperties().get("channel");
        channel.onWebSocketClose(closeReason.getCloseCode().getCode(), closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable error) {
        WebSocketChannel channel = (WebSocketChannel)session.getUserProperties().get("channel");
        if (channel != null)
            channel.onWebSocketError(error);
        else
            log.error(error, "WebSocket error for session %s", session.getId());
    }
}
