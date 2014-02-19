package org.granite.gravity.websocket;

import org.granite.util.ContentType;

import javax.servlet.http.HttpSession;

/**
 * Created by william on 13/02/14.
 */
public class GravityWebSocketConfig {

    public static ThreadLocal<GravityWebSocketConfig> config = new ThreadLocal<GravityWebSocketConfig>();

    public String connectMessageId;
    public String clientId;
    public String clientType;
    public ContentType contentType;
    public HttpSession session;

    public static void set(String connectMessageId, String clientId, String clientType, ContentType contentType, HttpSession session) {
        GravityWebSocketConfig c = new GravityWebSocketConfig();
        c.connectMessageId = connectMessageId;
        c.clientId = clientId;
        c.clientType = clientType;
        c.contentType = contentType;
        c.session = session;
        config.set(c);
    }

    public static GravityWebSocketConfig remove() {
        GravityWebSocketConfig c = config.get();
        config.remove();
        return c;
    }
}
