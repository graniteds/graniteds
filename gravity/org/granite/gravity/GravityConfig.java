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
