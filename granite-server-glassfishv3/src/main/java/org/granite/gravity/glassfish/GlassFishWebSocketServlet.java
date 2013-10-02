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
	
	@SuppressWarnings("deprecation")
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
		try {
			WebSocketEngine.getEngine().register(getServletContext().getContextPath(), mapping, app);
		}
		catch (NoSuchMethodError e) {
			// Deprecated since Grizzly 1.5.?
			WebSocketEngine.getEngine().register(app);
		}
	}

    @Override
    public void destroy() {
        WebSocketEngine.getEngine().unregister(app);
        app = null;
    }

}
