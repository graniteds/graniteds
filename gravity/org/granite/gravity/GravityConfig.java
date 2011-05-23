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
import org.granite.util.XMap;

import javax.servlet.ServletContext;

public interface GravityConfig {

    public void onReload(ServletContext context, GraniteConfig config);

    public String getGravityFactory();

    public long getChannelIdleTimeoutMillis();

    public void setChannelIdleTimeoutMillis(long channelIdleTimeoutMillis);

    public long getLongPollingTimeoutMillis();

    public void setLongPollingTimeoutMillis(long longPollingTimeoutMillis);

    public boolean isRetryOnError();

    public void setRetryOnError(boolean retryOnError);

    public int getMaxMessagesQueuedPerChannel();

    public void setMaxMessagesQueuedPerChannel(int maxMessagesQueuedPerChannel);

    public long getReconnectIntervalMillis();

    public void setReconnectIntervalMillis(long reconnectIntervalMillis);

    public int getReconnectMaxAttempts();

    public void setReconnectMaxAttempts(int reconnectMaxAttempts);

    public XMap getExtra();

    public int getCorePoolSize();

    public void setCorePoolSize(int corePoolSize);

    public int getMaximumPoolSize();

    public void setMaximumPoolSize(int maximumPoolSize);

    public long getKeepAliveTimeMillis();

    public void setKeepAliveTimeMillis(long keepAliveTimeMillis);

    public int getQueueCapacity();

    public void setQueueCapacity(int queueCapacity);

    public ChannelFactory getChannelFactory();
}
