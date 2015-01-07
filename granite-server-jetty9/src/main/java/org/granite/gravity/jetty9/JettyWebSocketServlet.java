/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.jetty9;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;


public class JettyWebSocketServlet extends WebSocketServlet {
	
	private static final long serialVersionUID = 1L;
	
    @Override
    public void init(ServletConfig config) throws ServletException {
        GravityServletUtil.init(config); // Must be before init() because configure() is called by init()

        super.init(config);
    }

	@Override
	public void configure(WebSocketServletFactory factory) {
        Gravity gravity = GravityManager.getGravity(getServletContext());

		factory.getPolicy().setIdleTimeout(gravity.getGravityConfig().getChannelIdleTimeoutMillis());
        factory.setCreator(new JettyWebSocketCreator(getServletContext()));
	}
}
