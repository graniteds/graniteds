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
package org.granite.gravity;

import javax.servlet.ServletContext;

import flex.messaging.messages.ErrorMessage;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.adapters.ServiceAdapter;
import org.granite.gravity.udp.UdpReceiverFactory;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import org.granite.messaging.jmf.SharedContext;

/**
 * @author William DRAI
 */
public class GravityProxy implements Gravity {

	private ServletContext servletContext;

    public GravityProxy() {
    }

	public GravityProxy(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	protected Gravity getGravity() {
		return GravityManager.getGravity(servletContext);
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

    public GraniteContext initThread(String sessionId, String clientType) {
    	return getGravity().initThread(sessionId, clientType);
    }
    public void releaseThread() {
    	getGravity().releaseThread();
    }
	
	public ServiceAdapter getServiceAdapter(String messageType, String destinationId) {
		return getGravity().getServiceAdapter(messageType, destinationId);
	}

	public boolean hasUdpReceiverFactory() {
		return getGravity().hasUdpReceiverFactory();
	}
    public UdpReceiverFactory getUdpReceiverFactory() {
		return getGravity().getUdpReceiverFactory();
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

    public <C extends Channel> C getChannel(ChannelFactory<C> channelFactory, String channelId) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return null;
    	return gravity.getChannel(channelFactory, channelId);
    }
    public Channel removeChannel(String channelId, boolean timeout) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return null;
    	return gravity.removeChannel(channelId, timeout);
    }
    public boolean access(String channelId) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return false;
    	return gravity.access(channelId);
    }
    public void execute(AsyncChannelRunner runnable) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return;
    	gravity.execute(runnable);
    }
    public boolean cancel(AsyncChannelRunner runnable) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return false;
    	return gravity.cancel(runnable);
    }

    public Message handleMessage(ChannelFactory<?> channelFactory, Message message) {
        // Should probably throw an exception, not intended to be used through the proxy
        Gravity gravity = getGravity();
        if (gravity == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
    	return gravity.handleMessage(channelFactory, message);
    }
    public Message handleMessage(ChannelFactory<?> channelFactory, Message message, boolean skipInterceptor) {
        // Should probably throw an exception, not intended to be used through the proxy
        Gravity gravity = getGravity();
        if (gravity == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
        return gravity.handleMessage(channelFactory, message, skipInterceptor);
    }
    public Message publishMessage(AsyncMessage message) {
    	return publishMessage(null, message);
    }
    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
        // Should probably throw an exception, not intended to be used through the proxy
        Gravity gravity = getGravity();
        if (gravity == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

    	return getGravity().publishMessage(fromChannel, message);
    }
}
