package org.granite.gravity.tomcat;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.util.Base64;
import org.apache.catalina.websocket.Constants;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.res.StringManager;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class TomcatWebSocketServlet extends WebSocketServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(TomcatWebSocketServlet.class);
	
    private static final byte[] WS_ACCEPT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes(B2CConverter.ISO_8859_1);
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private final Queue<MessageDigest> sha1Helpers = new ConcurrentLinkedQueue<MessageDigest>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
	}
	
	@Override
	protected String selectSubProtocol(List<String> subProtocols) {
		return subProtocols != null && subProtocols.contains("org.granite.gravity") ? "org.granite.gravity" : null;
	}
	
	@Override
	protected StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request) {
		Gravity gravity = GravityManager.getGravity(getServletContext());
		TomcatWebSocketChannelFactory channelFactory = new TomcatWebSocketChannelFactory(gravity, getServletContext());
		
		try {
			String connectMessageId = request.getHeader("connectId") != null ? request.getHeader("connectId") : request.getParameter("connectId");
			String clientId = request.getHeader("GDSClientId") != null ? request.getHeader("GDSClientId") : request.getParameter("GDSClientId");
			String sessionId = null;
			HttpSession session = request.getSession(false);
			if (session != null)
				sessionId = session.getId();
			if (request.getHeader("GDSSessionId") != null)
				sessionId = request.getHeader("GDSSessionId");
			if (sessionId == null && request.getParameter("GDSSessionId") != null)
				sessionId = request.getParameter("GDSSessionId");
			
	        ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), 
	        		getServletContext(), sessionId); 
			
			log.info("WebSocket connection started %s clientId %s sessionId %s", protocol, clientId, sessionId);
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			if (clientId != null)
				pingMessage.setClientId(clientId);
			
			Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
			
			TomcatWebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
			
			if (!ackMessage.getClientId().equals(clientId))
				channel.setConnectAckMessage(ackMessage);
			
			return channel.getStreamInbound();
		}
		finally {
			GraniteContext.release();
		}
	}
	
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Information required to send the server handshake message
        String key;
        String subProtocol = null;
        List<String> extensions = Collections.emptyList();

        if (!headerContainsToken(req, "upgrade", "websocket")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!headerContainsToken(req, "connection", "upgrade")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!headerContainsToken(req, "sec-websocket-version", "13")) {
            resp.setStatus(426);
            resp.setHeader("Sec-WebSocket-Version", "13");
            return;
        }

        key = req.getHeader("Sec-WebSocket-Key");
        if (key == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String origin = req.getHeader("Origin");
        if (!verifyOrigin(origin)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        // Fix for Tomcat-7.0.29 bad header name (was Sec-WebSocket-Protocol-Client")
        List<String> subProtocols = getTokensFromHeader(req, "Sec-WebSocket-Protocol");
        if (!subProtocols.isEmpty())
            subProtocol = selectSubProtocol(subProtocols);

        // TODO Read client handshake - Sec-WebSocket-Extensions

        // TODO Extensions require the ability to specify something (API TBD)
        //      that can be passed to the Tomcat internals and process extension
        //      data present when the frame is fragmented.

        // If we got this far, all is good. Accept the connection.
        resp.setHeader("Upgrade", "websocket");
        resp.setHeader("Connection", "upgrade");
        resp.setHeader("Sec-WebSocket-Accept", getWebSocketAccept(key));
        if (subProtocol != null)
            resp.setHeader("Sec-WebSocket-Protocol", subProtocol);
        
        if (!extensions.isEmpty()) {
            // TODO
        }

        WsHttpServletRequestWrapper wrapper = new WsHttpServletRequestWrapper(req);
        StreamInbound inbound = createWebSocketInbound(subProtocol, wrapper);
        wrapper.invalidate();
        
        // Hack to avoid chunked transfer
        resp.setContentLength(((TomcatWebSocketChannel.MessageInboundImpl)inbound).getAckLength());
        
        // Small hack until the Servlet API provides a way to do this.
        ServletRequest inner = req;
        // Unwrap the request
        while (inner instanceof ServletRequestWrapper)
            inner = ((ServletRequestWrapper)inner).getRequest();
        
        if (inner instanceof RequestFacade)
            ((RequestFacade)inner).doUpgrade(inbound);
        else
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sm.getString("servlet.reqUpgradeFail"));
    }
    
    
    private boolean headerContainsToken(HttpServletRequest req,
            String headerName, String target) {
        Enumeration<String> headers = req.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] tokens = header.split(",");
            for (String token : tokens) {
                if (target.equalsIgnoreCase(token.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private List<String> getTokensFromHeader(HttpServletRequest req,
            String headerName) {
        List<String> result = new ArrayList<String>();

        Enumeration<String> headers = req.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] tokens = header.split(",");
            for (String token : tokens) {
                result.add(token.trim());
            }
        }
        return result;
    }
    
    private String getWebSocketAccept(String key) throws ServletException {

        MessageDigest sha1Helper = sha1Helpers.poll();
        if (sha1Helper == null) {
            try {
                sha1Helper = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException(e);
            }
        }

        sha1Helper.reset();
        sha1Helper.update(key.getBytes(B2CConverter.ISO_8859_1));
        String result = Base64.encode(sha1Helper.digest(WS_ACCEPT));

        sha1Helpers.add(sha1Helper);

        return result;
    }
}
