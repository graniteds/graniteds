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

import java.util.Arrays;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;

/**
 * Note: MUST implement ServerApplicationConfig to force Tomcat 7.0.52+ to initialize ServerContainer
 *
 * @author wdrai
 */
public class GravityWebSocketDeployer implements ServletContextListener {

	private static final String WEBSOCKET_DEPLOYED_ATTR = GravityWebSocketDeployer.class.getName() + "_deployed";
	
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServerContainer serverContainer = (ServerContainer)servletContextEvent.getServletContext().getAttribute(ServerContainer.class.getName());
        if (serverContainer == null)
        	return;
    
    	Object deployed = servletContextEvent.getServletContext().getAttribute(WEBSOCKET_DEPLOYED_ATTR);
    	if (deployed != null)
    		return;
    	
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
            
            servletContextEvent.getServletContext().setAttribute(WEBSOCKET_DEPLOYED_ATTR, true);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not deploy gravity websocket endpoint", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
