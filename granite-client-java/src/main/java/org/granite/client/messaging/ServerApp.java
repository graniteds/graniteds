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
package org.granite.client.messaging;

/**
 * Simple server application definition holding basic network parameters
 * (server name, port, context root)
 * It can be used to create channels when you don't exactly know how the final URI should look like
 * The channel builder for the requested channel type will then build the correct URI using the server app parameters
 *
 * @see org.granite.client.messaging.channel.ChannelBuilder
 *
 * @author William DRAI
 */
public class ServerApp {

    private boolean secure = false;
    private String serverName = "localhost";
    private int serverPort = 8080;
    private String contextRoot;

    /**
     * Default empty server application, used for testing
     */
    public ServerApp() {
    }

    /**
     * Non secure development localhost application definition
     * @param contextRoot context root (should start by /)
     */
    public ServerApp(String contextRoot) {
        this(contextRoot, false, "localhost", 8080);
    }

    /**
     * Non secure server application definition
     * @param contextRoot context root (should start by /)
     * @param serverName server host name
     * @param serverPort server port
     */
    public ServerApp(String contextRoot, String serverName, int serverPort) {
        this(contextRoot, false, serverName, serverPort);
    }

    /**
     * Server application definition
     * @param contextRoot context root (should start by /)
     * @param secure true if the server URI should be secure (https/wss/...)
     * @param serverName server host name
     * @param serverPort server port
     */
    public ServerApp(String contextRoot, boolean secure, String serverName, int serverPort) {
        this.secure = secure;
        this.serverName = serverName;
        this.serverPort = serverPort;
        setContextRoot(contextRoot);
    }

    /**
     * @return true if the server app is secure (https/wss/...)
     */
    public boolean getSecure() {
        return secure;
    }

    /**
     * Set the app security mode
     * @param secure true if the server URI should be secure (https/wss/...)
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * @return server host name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set server host name
     * @param serverName host name
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return server port
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Set server port
     * @param serverPort server port
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * @return context root
     */
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * Set context root (should start by /)
     * @param contextRoot context root
     */
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot.startsWith("/") ? contextRoot : ("/" + contextRoot);
    }
}
