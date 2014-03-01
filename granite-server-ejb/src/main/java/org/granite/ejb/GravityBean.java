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
package org.granite.ejb;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.gravity.Channel;
import org.granite.gravity.GravityConfig;
import org.granite.messaging.jmf.SharedContext;

import javax.ejb.Local;
import javax.ejb.Singleton;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@Singleton(name="org.granite.ejb.Gravity")
@Local(Gravity.class)
public class GravityBean implements Gravity {

    private org.granite.gravity.Gravity gravity;

    public void setGravity(org.granite.gravity.Gravity gravity) {
        this.gravity = gravity;
    }

    private org.granite.gravity.Gravity getGravity() {
        return gravity;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Granite/Services configs access.

    public GravityConfig getGravityConfig() {
        return getGravity().getGravityConfig();
    }
    public ServicesConfig getServicesConfig() {
        return getGravity().getServicesConfig();
    }
    public GraniteConfig getGraniteConfig() {
        return getGravity().getGraniteConfig();
    }
    public SharedContext getSharedContext() {
        return getGravity().getSharedContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    public boolean isStarted() {
        return getGravity().isStarted();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Operations.

    @Override
    public List<Channel> getConnectedChannels() {
        return getGravity().getConnectedChannels();
    }
    @Override
    public Set<Principal> getConnectedUsers() {
        return getGravity().getConnectedUsers();
    }
    @Override
    public List<Channel> getConnectedChannelsByDestination(String destination) {
        return getGravity().getConnectedChannelsByDestination(destination);
    }
    @Override
    public Set<Principal> getConnectedUsersByDestination(String destination) {
        return getGravity().getConnectedUsersByDestination(destination);
    }
    @Override
    public List<Channel> findConnectedChannelsByUser(String name) {
        return getGravity().findConnectedChannelsByUser(name);
    }
    @Override
    public Channel findConnectedChannelByClientId(String clientId) {
        return getGravity().findConnectedChannelByClientId(clientId);
    }

    public void start() throws Exception {
        getGravity().start();
    }
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig) {
        getGravity().reconfigure(gravityConfig, graniteConfig);
    }
    public void stop() throws Exception {
        getGravity().stop();
    }
    public void stop(boolean now) throws Exception {
        getGravity().stop(now);
    }

    public Message handleMessage(Message message) {
        return getGravity().handleMessage(message);
    }
    public Message handleMessage(Message message, boolean skipInterceptor) {
        return getGravity().handleMessage(message, skipInterceptor);
    }
    public Message publishMessage(AsyncMessage message) {
        return publishMessage(null, message);
    }
    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
        if (getGravity() == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity EJB not yet ready"));

        return getGravity().publishMessage(fromChannel, message);
    }
    public Message sendRequest(Channel fromChannel, AsyncMessage message) {
        if (getGravity() == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

        return getGravity().sendRequest(fromChannel, message);
    }

}
