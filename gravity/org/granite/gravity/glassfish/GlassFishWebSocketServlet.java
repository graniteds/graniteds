package org.granite.gravity.glassfish;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;

import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;

import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketEngine;


public class GlassFishWebSocketServlet extends HttpServlet {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(GlassFishWebSocketServlet.class);

	private static final long serialVersionUID = 1L;
	
	private WebSocketApplication app;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
		Gravity gravity = GravityManager.getGravity(getServletContext());
		String mapping = null;
		for (ServletRegistration sr : getServletContext().getServletRegistrations().values()) {
			if (!sr.getClassName().equals(getClass().getName()))
				continue;
			mapping = sr.getMappings().iterator().next(); 
		}
		app = new GlassFishWebSocketApplication(getServletContext(), gravity, mapping);
        WebSocketEngine.getEngine().register(app);
	}

    @Override
    public void destroy() {
        WebSocketEngine.getEngine().unregister(app);
        app = null;
    }

}
