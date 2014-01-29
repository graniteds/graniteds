/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.gravity;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.adapters.ServiceAdapter;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class GravityProxy implements Gravity {

	private ServletContext servletContext;
	
	public GravityProxy(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	private Gravity getGravity() {
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

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

	public boolean isStarted() {
		return getGravity().isStarted();
	}

    ///////////////////////////////////////////////////////////////////////////
    // Operations.

    public GraniteContext initThread() {
    	return getGravity().initThread();
    }
    public void releaseThread() {
    	getGravity().releaseThread();
    }
	
	public ServiceAdapter getServiceAdapter(String messageType, String destinationId) {
		return getGravity().getServiceAdapter(messageType, destinationId);
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

    public Channel getChannel(String channelId) {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return null;
    	return gravity.getChannel(channelId);
    }
    public Channel removeChannel(String channelId) {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return null;
    	return getGravity().removeChannel(channelId);
    }
    public boolean access(String channelId) {
    	// Should probably throw an exception, not intended to be used through the proxy
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return false;
    	return gravity.access(channelId);
    }
    public void execute(AsyncChannelRunner runnable) {
    	// Should probably throw an exception, not intended to be used through the proxy
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return;
    	gravity.execute(runnable);
    }
    public boolean cancel(AsyncChannelRunner runnable) {
    	// Should probably throw an exception, not intended to be used through the proxy
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return false;
    	return gravity.cancel(runnable);
    }
    
    public Message handleMessage(Message message) {
    	// Should probably throw an exception, not intended to be used through the proxy
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
    	return gravity.handleMessage(message);
    }
    public Message handleMessage(Message message, boolean skipInterceptor) {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
    	return gravity.handleMessage(message, skipInterceptor);
    }
    public Message publishMessage(AsyncMessage message) {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
    	return gravity.publishMessage(message);
    }
    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));
    	return gravity.publishMessage(fromChannel, message);
    }
}
