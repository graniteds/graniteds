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
package org.granite.gravity.websocket;

import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;

/**
 * Note: MUST implement ServerApplicationConfig to force Tomcat 7.0.52+ to initialize ServerContainer
 *
 * @author wdrai
 */
public class GravityWebSocketDeployer implements ServletContextListener {

	private static final long serialVersionUID = 1L;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServerContainer serverContainer = (ServerContainer)servletContextEvent.getServletContext().getAttribute(ServerContainer.class.getName());
        if (serverContainer != null) {
            Gravity gravity = GravityManager.getGravity(servletContextEvent.getServletContext());
            try {
                if (gravity == null)
                    gravity = GravityManager.start(servletContextEvent.getServletContext());

                try {
                    serverContainer.setDefaultMaxSessionIdleTimeout(gravity.getGravityConfig().getChannelIdleTimeoutMillis());
                }
                catch (UnsupportedOperationException e) {
                    // GlassFish v4 ?
                }
                ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(GravityWebSocketEndpoint.class, "/websocketamf/amf")
                        .configurator(new GravityWebSocketConfigurator())
                        .subprotocols(Arrays.asList("org.granite.gravity.amf", "org.granite.gravity.jmf+amf"))
                        .build();
                serverEndpointConfig.getUserProperties().put("servletContext", servletContextEvent.getServletContext());
                serverEndpointConfig.getUserProperties().put("gravity", gravity);
                serverContainer.addEndpoint(serverEndpointConfig);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not deploy gravity websocket endpoint", e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
