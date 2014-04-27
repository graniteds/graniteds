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
package org.granite.gravity;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public interface Gravity {

    ///////////////////////////////////////////////////////////////////////////
    // Granite/Services configs access.

    public GravityConfig getGravityConfig();
    public ServicesConfig getServicesConfig();
    public GraniteConfig getGraniteConfig();

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

	public boolean isStarted();

    ///////////////////////////////////////////////////////////////////////////
    // Operations.

    public void start() throws Exception;
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig);
    public void stop() throws Exception;
    public void stop(boolean now) throws Exception;

    public List<Channel> getConnectedChannels();
    public Set<Principal> getConnectedUsers();
    public List<Channel> getConnectedChannelsByDestination(String destination);
    public Set<Principal> getConnectedUsersByDestination(String destination);
    public List<Channel> findConnectedChannelsByUser(String name);
    public Channel findConnectedChannelByClientId(String clientId);
    public Channel findCurrentChannel(String destination);
    
    public Message handleMessage(Message message);
    public Message handleMessage(Message message, boolean skipInterceptor);

    public Message publishMessage(AsyncMessage message);
    public Message publishMessage(Channel fromChannel, AsyncMessage message);
    public Message sendRequest(Channel fromChannel, AsyncMessage message);
}
