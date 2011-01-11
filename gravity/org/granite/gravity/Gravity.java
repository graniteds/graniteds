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

import org.granite.config.GraniteConfig;
import org.granite.config.ShutdownListener;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.adapters.ServiceAdapter;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public interface Gravity extends ShutdownListener {

    ///////////////////////////////////////////////////////////////////////////
    // Constants.

    public static final String RECONNECT_INTERVAL_MS_KEY = "reconnect-interval-ms";
    public static final String RECONNECT_MAX_ATTEMPTS_KEY = "reconnect-max-attempts";
    
    public static final String BYTEARRAY_BODY_HEADER = "GDS_BYTEARRAY_BODY";    

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

    public GraniteContext initThread();
    public void releaseThread();
	
	public ServiceAdapter getServiceAdapter(String messageType, String destinationId);
	
    public void start() throws Exception;
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig);
    public void stop() throws Exception;
    public void stop(boolean now) throws Exception;

    public Channel getChannel(String channelId);
    public Channel removeChannel(String channelId);
    public boolean access(String channelId);
    public void execute(AsyncChannelRunner runnable);
    public boolean cancel(AsyncChannelRunner runnable);

    public Message handleMessage(Message message);
    public Message handleMessage(Message message, boolean skipInterceptor);
    public Message publishMessage(AsyncMessage message);
    public Message publishMessage(Channel fromChannel, AsyncMessage message);
}
