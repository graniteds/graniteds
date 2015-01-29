/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import org.granite.clustering.DistributedData;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.adapters.AdapterFactory;
import org.granite.gravity.adapters.ServiceAdapter;
import org.granite.gravity.security.GravityDestinationSecurizer;
import org.granite.gravity.security.GravityInvocationContext;
import org.granite.gravity.udp.UdpReceiverFactory;
import org.granite.jmx.MBeanServerLocator;
import org.granite.jmx.OpenMBean;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.security.AbstractSecurityContext;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ServiceLoader;
import org.granite.util.TypeUtil;
import org.granite.util.UUIDUtil;
import org.granite.util.XMap;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 * @author Franck WOLFF
 */
public class DefaultGravity implements Gravity, GravityInternal, DefaultGravityMBean {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private static final Logger log = Logger.getLogger(Gravity.class);

    private final Map<String, Object> applicationMap = new HashMap<String, Object>();
    private final ConcurrentHashMap<String, TimeChannel<?>> channels = new ConcurrentHashMap<String, TimeChannel<?>>();
    
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<Listener>();
    
    private GravityConfig gravityConfig = null;
    private ServicesConfig servicesConfig = null;
    private GraniteConfig graniteConfig = null;

    private Channel serverChannel = null;
    private AdapterFactory adapterFactory = null;
    private GravityPool gravityPool = null;
    
    private UdpReceiverFactory udpReceiverFactory = null;

    private Timer channelsTimer;
    private Timer repliesCleanupTimer;
    private boolean started;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public DefaultGravity(GravityConfig gravityConfig, ServicesConfig servicesConfig, GraniteConfig graniteConfig) {
        if (gravityConfig == null || servicesConfig == null || graniteConfig == null)
            throw new NullPointerException("All arguments must be non null.");

        this.gravityConfig = gravityConfig;
        this.servicesConfig = servicesConfig;
        this.graniteConfig = graniteConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    public GravityConfig getGravityConfig() {
		return gravityConfig;
	}

	public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

	public GraniteConfig getGraniteConfig() {
        return graniteConfig;
    }
	
	public boolean isStarted() {
        return started;
    }
	
	public ServiceAdapter getServiceAdapter(String messageType, String destinationId) {
		return adapterFactory.getServiceAdapter(messageType, destinationId);
	}

    ///////////////////////////////////////////////////////////////////////////
    // Starting/stopping.

    public void start() throws Exception {
    	log.info("Starting Gravity...");
        synchronized (this) {
        	if (!started) {
	            adapterFactory = new AdapterFactory(this);
	            internalStart();
	            serverChannel = new ServerChannel(this, ServerChannel.class.getName(), null, null);
	            
	            if (gravityConfig.isUseUdp()) {
	            	ServiceLoader<UdpReceiverFactory> loader = ServiceLoader.load(UdpReceiverFactory.class);
	            	Iterator<UdpReceiverFactory> factories = loader.iterator();
	            	if (factories.hasNext()) {
	            		udpReceiverFactory = factories.next();
	            		udpReceiverFactory.setPort(gravityConfig.getUdpPort());
	            		udpReceiverFactory.setNio(gravityConfig.isUdpNio());
	            		udpReceiverFactory.setConnected(gravityConfig.isUdpConnected());
	            		udpReceiverFactory.setSendBufferSize(gravityConfig.getUdpSendBufferSize());
	            		udpReceiverFactory.start();
	            	}
	            	else
	            		log.warn("UDP receiver factory not found");
	            }
	            
	            started = true;
        	}
        }
    	log.info("Gravity successfully started.");
    }
    
    protected void internalStart() {
        gravityPool = new GravityPool(gravityConfig);
        channelsTimer = new Timer();
        repliesCleanupTimer = new Timer();
        repliesCleanupTimer.scheduleAtFixedRate(new AsyncRepliesCleanup(), 10000L, 10000L);
        
        if (graniteConfig.isRegisterMBeans()) {
	        try {
	            ObjectName name = new ObjectName("org.graniteds:type=Gravity,context=" + graniteConfig.getMBeanContextName());
		        log.info("Registering MBean: %s", name);
	            OpenMBean mBean = OpenMBean.createMBean(this);
	        	MBeanServerLocator.getInstance().register(mBean, name, true);
	        }
	        catch (Exception e) {
	        	log.error(e, "Could not register Gravity MBean for context: %s", graniteConfig.getMBeanContextName());
	        }
        }
    }
    
    public void restart() throws Exception {
    	synchronized (this) {
    		stop();
    		start();
    	}
	}

	public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig) {
    	this.gravityConfig = gravityConfig;
    	this.graniteConfig = graniteConfig;
    	if (gravityPool != null)
    		gravityPool.reconfigure(gravityConfig);
    }

    public void stop() throws Exception {
        stop(true);
    }

    public void stop(boolean now) throws Exception {
    	log.info("Stopping Gravity (now=%s)...", now);
        synchronized (this) {
        	if (adapterFactory != null) {
	            try {
					adapterFactory.stopAll();
				} catch (Exception e) {
        			log.error(e, "Error while stopping adapter factory");
				}
	            adapterFactory = null;
        	}

        	if (serverChannel != null) {
	            try {
					removeChannel(serverChannel.getId(), false);
				} catch (Exception e) {
        			log.error(e, "Error while removing server channel: %s", serverChannel);
				}
	            serverChannel = null;
        	}
            
            if (channelsTimer != null) {
	            try {
					channelsTimer.cancel();
				} catch (Exception e) {
        			log.error(e, "Error while cancelling channels timer");
				}
	            channelsTimer = null;
            }

            if (repliesCleanupTimer != null) {
                try {
                    repliesCleanupTimer.cancel();
                } catch (Exception e) {
                    log.error(e, "Error while cancelling replies cleanup timer");
                }
                repliesCleanupTimer = null;
            }
        	
        	if (gravityPool != null) {
        		try {
	        		if (now)
	        			gravityPool.shutdownNow();
	        		else
	        			gravityPool.shutdown();
        		}
        		catch (Exception e) {
        			log.error(e, "Error while stopping thread pool");
        		}
        		gravityPool = null;
        	}
        	
        	if (udpReceiverFactory != null) {
        		try {
        			udpReceiverFactory.stop();
        		}
        		catch (Exception e) {
        			log.error(e, "Error while stopping udp receiver factory");
        		}
        		udpReceiverFactory = null;
        	}
            
            started = false;
        }
        log.info("Gravity sucessfully stopped.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // GravityMBean attributes implementation.

	public String getGravityFactoryName() {
		return gravityConfig.getGravityFactory();
	}

	public long getChannelIdleTimeoutMillis() {
		return gravityConfig.getChannelIdleTimeoutMillis();
	}
	public void setChannelIdleTimeoutMillis(long channelIdleTimeoutMillis) {
		gravityConfig.setChannelIdleTimeoutMillis(channelIdleTimeoutMillis);
	}

	public boolean isRetryOnError() {
		return gravityConfig.isRetryOnError();
	}
	public void setRetryOnError(boolean retryOnError) {
		gravityConfig.setRetryOnError(retryOnError);
	}

	public long getLongPollingTimeoutMillis() {
		return gravityConfig.getLongPollingTimeoutMillis();
	}
	public void setLongPollingTimeoutMillis(long longPollingTimeoutMillis) {
		gravityConfig.setLongPollingTimeoutMillis(longPollingTimeoutMillis);
	}

	public int getMaxMessagesQueuedPerChannel() {
		return gravityConfig.getMaxMessagesQueuedPerChannel();
	}
	public void setMaxMessagesQueuedPerChannel(int maxMessagesQueuedPerChannel) {
		gravityConfig.setMaxMessagesQueuedPerChannel(maxMessagesQueuedPerChannel);
	}

	public long getReconnectIntervalMillis() {
		return gravityConfig.getReconnectIntervalMillis();
	}

	public int getReconnectMaxAttempts() {
		return gravityConfig.getReconnectMaxAttempts();
	}

    public int getCorePoolSize() {
    	if (gravityPool != null)
    		return gravityPool.getCorePoolSize();
    	return gravityConfig.getCorePoolSize();
	}

	public void setCorePoolSize(int corePoolSize) {
		gravityConfig.setCorePoolSize(corePoolSize);
		if (gravityPool != null)
    		gravityPool.setCorePoolSize(corePoolSize);
	}

	public long getKeepAliveTimeMillis() {
    	if (gravityPool != null)
    		return gravityPool.getKeepAliveTimeMillis();
    	return gravityConfig.getKeepAliveTimeMillis();
	}
	public void setKeepAliveTimeMillis(long keepAliveTimeMillis) {
		gravityConfig.setKeepAliveTimeMillis(keepAliveTimeMillis);
		if (gravityPool != null)
    		gravityPool.setKeepAliveTimeMillis(keepAliveTimeMillis);
	}

	public int getMaximumPoolSize() {
    	if (gravityPool != null)
    		return gravityPool.getMaximumPoolSize();
    	return gravityConfig.getMaximumPoolSize();
	}
	public void setMaximumPoolSize(int maximumPoolSize) {
		gravityConfig.setMaximumPoolSize(maximumPoolSize);
		if (gravityPool != null)
    		gravityPool.setMaximumPoolSize(maximumPoolSize);
	}

	public int getQueueCapacity() {
    	if (gravityPool != null)
    		return gravityPool.getQueueCapacity();
    	return gravityConfig.getQueueCapacity();
	}

	public int getQueueRemainingCapacity() {
    	if (gravityPool != null)
    		return gravityPool.getQueueRemainingCapacity();
    	return gravityConfig.getQueueCapacity();
	}

	public int getQueueSize() {
    	if (gravityPool != null)
    		return gravityPool.getQueueSize();
    	return 0;
	}
	
	public boolean hasUdpReceiverFactory() {
		return udpReceiverFactory != null;
	}

    public UdpReceiverFactory getUdpReceiverFactory() {
		return udpReceiverFactory;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Channel's operations.

	protected <C extends Channel> C createChannel(ChannelFactory<C> channelFactory, String clientId) {
    	C channel = null;
    	if (clientId != null) {
    		channel = getChannel(channelFactory, clientId);
	    	if (channel != null)
	    		return channel;
    	}
    	
    	String clientType = GraniteContext.getCurrentInstance().getClientType();
    	channel = channelFactory.newChannel(UUIDUtil.randomUUID(), clientType);
    	TimeChannel<C> timeChannel = new TimeChannel<C>(channel);
        for (int i = 0; channels.putIfAbsent(channel.getId(), timeChannel) != null; i++) {
            if (i >= 10)
                throw new RuntimeException("Could not find random new clientId after 10 iterations");
            channel.destroy(false);
            channel = channelFactory.newChannel(UUIDUtil.randomUUID(), clientType);
            timeChannel = new TimeChannel<C>(channel);
        }

        String channelId = channel.getId();
        
        // Save channel id in distributed data (clustering).
        try {
	        DistributedData gdd = graniteConfig.getDistributedDataFactory().getInstance();
	        if (gdd != null) {
        		log.debug("Saving channel id in distributed data: %s", channelId);
	        	gdd.addChannelId(channelId, channelFactory.getClass().getName(), clientType);
	        }
        }
        catch (Exception e) {
			log.error(e, "Could not add channel id in distributed data: %s", channelId);
        }
        
        // Initialize timer task.
        access(channelId);
    	
        return channel;
    }

    @SuppressWarnings("unchecked")
	public <C extends Channel> C getChannel(ChannelFactory<C> channelFactory, String clientId) {
        if (clientId == null)
            return null;

		TimeChannel<C> timeChannel = (TimeChannel<C>)channels.get(clientId);
        if (timeChannel == null) {
	        // Look for existing channel id/subscriptions in distributed data (clustering).
            log.debug("Lookup channel %s in distributed data", clientId);
            try {
	        	DistributedData gdd = graniteConfig.getDistributedDataFactory().getInstance();
	        	if (gdd != null && gdd.hasChannelId(clientId)) {
	        		log.debug("Found channel id in distributed data: %s", clientId);
	        		String channelFactoryClassName = gdd.getChannelFactoryClassName(clientId);
                    String clientType = gdd.getChannelClientType(clientId);
	        		channelFactory = (ChannelFactory<C>)TypeUtil.newInstance(channelFactoryClassName, new Class<?>[] { GravityInternal.class }, new Object[] { this });
	        		C channel = channelFactory.newChannel(clientId, clientType);
	    	    	timeChannel = new TimeChannel<C>(channel);
	    	        if (channels.putIfAbsent(clientId, timeChannel) == null) {
	    	        	for (CommandMessage subscription : gdd.getSubscriptions(clientId)) {
	    	        		log.debug("Resubscribing channel: %s - %s", clientId, subscription);
	    	        		handleSubscribeMessage(channelFactory, subscription, false);
	    	        	}
	    	        	access(clientId);
	    	        }
	        	}
        	}
        	catch (Exception e) {
    			log.error(e, "Could not recreate channel/subscriptions from distributed data: %s", clientId);
        	}
        }

        return (timeChannel != null ? timeChannel.getChannel() : null);
    }

    public Channel removeChannel(String channelId, boolean timeout) {
        if (channelId == null)
            return null;

        // Remove existing channel id/subscriptions in distributed data (clustering).
        try {
	        DistributedData gdd = graniteConfig.getDistributedDataFactory().getInstance();
	        if (gdd != null) {
        		log.debug("Removing channel id from distributed data: %s", channelId);
	        	gdd.removeChannelId(channelId);
	        }
		}
		catch (Exception e) {
			log.error(e, "Could not remove channel id from distributed data: %s", channelId);
		}
        
        TimeChannel<?> timeChannel = channels.get(channelId);
        Channel channel = null;
        if (timeChannel != null) {
        	try {
        		if (timeChannel.getTimerTask() != null)
        			timeChannel.getTimerTask().cancel();
        	}
        	catch (Exception e) {
        		// Should never happen...
        	}
        	
        	channel = timeChannel.getChannel();
        	
        	try {
	            for (Subscription subscription : channel.getSubscriptions()) {
	            	try {
		            	Message message = subscription.getUnsubscribeMessage();
		            	handleMessage(channel.getFactory(), message, true);
	            	}
	            	catch (Exception e) {
	            		log.error(e, "Error while unsubscribing channel: %s from subscription: %s", channel, subscription);
	            	}
	            }
        	}
        	finally {
        		channels.remove(channelId);
    			channel.destroy(timeout);
        	}
        }
        return channel;
    }
    
    public boolean access(String channelId) {
    	if (channelId != null) {
	    	TimeChannel<?> timeChannel = channels.get(channelId);
	    	if (timeChannel != null) {
	    		synchronized (timeChannel) {
	    			TimerTask timerTask = timeChannel.getTimerTask();
		    		if (timerTask != null) {
			            log.debug("Canceling TimerTask: %s", timerTask);
			            timerTask.cancel();
		    			timeChannel.setTimerTask(null);
		    		}
		            
		    		timerTask = new ChannelTimerTask(this, channelId);
		            timeChannel.setTimerTask(timerTask);
		            
		            long timeout = gravityConfig.getChannelIdleTimeoutMillis();
		            log.debug("Scheduling TimerTask: %s for %s ms.", timerTask, timeout);
		            channelsTimer.schedule(timerTask, timeout);
		            return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    public void execute(AsyncChannelRunner runner) {
    	if (gravityPool == null) {
    		runner.reset();
    		throw new NullPointerException("Gravity not started or pool disabled");
    	}
    	gravityPool.execute(runner);
    }
    
    public boolean cancel(AsyncChannelRunner runner) {
    	if (gravityPool == null) {
    		runner.reset();
    		throw new NullPointerException("Gravity not started or pool disabled");
    	}
    	return gravityPool.remove(runner);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Incoming message handling.

    public Message handleMessage(Message message) {
        return handleMessage(null, message, false);
    }

    public Message handleMessage(Message message, boolean skipInterceptor) {
        return handleMessage(null, message, skipInterceptor);
    }

    public Message handleMessage(final ChannelFactory<?> channelFactory, Message message) {
    	return handleMessage(channelFactory, message, false);
    }

    public Message handleMessage(final ChannelFactory<?> channelFactory, final Message message, boolean skipInterceptor) {

        AMF3MessageInterceptor interceptor = null;
        if (!skipInterceptor)
        	interceptor = ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getAmf3MessageInterceptor();
        
        Message reply = null;
        boolean publish = false;
        
        try {
	        if (interceptor != null)
	            interceptor.before(message);

	        if (message instanceof CommandMessage) {
	            CommandMessage command = (CommandMessage)message;
	
	            switch (command.getOperation()) {
	
	            case CommandMessage.LOGIN_OPERATION:
	            case CommandMessage.LOGOUT_OPERATION:
	                return handleSecurityMessage(channelFactory, command);
	
	            case CommandMessage.CLIENT_PING_OPERATION:
	                return handlePingMessage(channelFactory, command);
	            case CommandMessage.CONNECT_OPERATION:
	                return handleConnectMessage(channelFactory, command);
	            case CommandMessage.DISCONNECT_OPERATION:
	                return handleDisconnectMessage(channelFactory, command);
	            case CommandMessage.SUBSCRIBE_OPERATION:
	                return handleSubscribeMessage(channelFactory, command);
	            case CommandMessage.UNSUBSCRIBE_OPERATION:
	                return handleUnsubscribeMessage(channelFactory, command);
	
	            default:
	                throw new UnsupportedOperationException("Unsupported command operation: " + command);
	            }
	        }
	
	        reply = handlePublishMessage(channelFactory, (AsyncMessage)message);
	        publish = true;
        }
        finally {
	        if (interceptor != null)
	            interceptor.after(message, reply);
        }
        
        if (reply != null) {
	        GraniteContext context = GraniteContext.getCurrentInstance();
	        if (context.getSessionId() != null) {
	        	reply.setHeader("org.granite.sessionId", context.getSessionId());
	            if (publish && context instanceof ServletGraniteContext && ((ServletGraniteContext)context).getSession(false) != null) {
	            	long serverTime = new Date().getTime();
	            	((ServletGraniteContext)context).getSession().setAttribute(GraniteContext.SESSION_LAST_ACCESSED_TIME_KEY, serverTime);
	            	reply.setHeader("org.granite.time", serverTime);
	            	reply.setHeader("org.granite.sessionExp", ((ServletGraniteContext)context).getSession().getMaxInactiveInterval());
	            }
	        }
        }
        
        return reply;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other Public API methods.

    public GraniteContext initThread(String sessionId, String clientType) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null)
            context = SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, sessionId, applicationMap, clientType);
        return context;
    }
    
    public void releaseThread() {
    	GraniteContext.release();
	}
    
    public void registerListener(Listener listener) {
    	listeners.add(listener);
    }
    
    public void unregisterListener(Listener listener) {
    	listeners.remove(listener);
    }
    
    public void notifyConnected(Channel channel) {
    	for (Listener listener : listeners) {
    		try {
    			listener.connected(channel);
			}
			catch (Throwable t) {
				log.error(t, "Error calling connected listener");
			}
    	}
    }
    
    public void notifyDisconnected(Channel channel) {
    	for (Listener listener : listeners) {
    		try {
    			listener.disconnected(channel);
			}
			catch (Throwable t) {
				log.error(t, "Error calling disconnected listener");
			}
    	}
    }
    
    public void notifyAuthenticated(Channel channel, Principal principal) {
    	for (Listener listener : listeners) {
    		try {
    			listener.authenticated(channel, principal);
    		}
    		catch (Throwable t) {
    			log.error(t, "Error calling authenticated listener");
    		}
    	}
    }
    
    private void notifySubscribed(Channel channel, String subscriptionId) {
    	for (Listener listener : listeners) {
    		try {
    			listener.subscribed(channel, subscriptionId);
    		}
    		catch (Throwable t) {
    			log.error(t, "Error calling subscribed listener");
    		}
    	}
    }
    
    private void notifyUnsubscribed(Channel channel, String subscriptionId) {
    	for (Listener listener : listeners) {
    		try {
    			listener.unsubscribed(channel, subscriptionId);
    		}
    		catch (Throwable t) {
    			log.error(t, "Error calling unsubscribed listener");
    		}
    	}
    }

    public List<Channel> getConnectedChannels() {
        List<Channel> channels = new ArrayList<Channel>();
        for (TimeChannel<? extends Channel> timeChannel : this.channels.values()) {
        	if (timeChannel.getChannel().isConnected())
        		channels.add(timeChannel.getChannel());
        }
        return channels;
    }

    public Set<Principal> getConnectedUsers() {
        Set<Principal> userPrincipals =  new HashSet<Principal>();
        for (TimeChannel<? extends Channel> timeChannel : this.channels.values()) {
            if (timeChannel.getChannel().isConnected() && timeChannel.getChannel().getUserPrincipal() != null)
                userPrincipals.add(timeChannel.getChannel().getUserPrincipal());
        }
        return userPrincipals;
    }

    public List<Channel> getConnectedChannelsByDestination(String destination) {
        List<Channel> channels = new ArrayList<Channel>();
        for (TimeChannel<? extends Channel> timeChannel : this.channels.values()) {
        	if (!timeChannel.getChannel().isConnected())
        		continue;
            for (Subscription subscription : timeChannel.getChannel().getSubscriptions()) {
                if (destination.equals(subscription.getDestination())) {
                    channels.add(timeChannel.getChannel());
                    break;
                }
            }
        }
        return channels;
    }

    public Set<Principal> getConnectedUsersByDestination(String destination) {
        Set<Principal> userPrincipals = new HashSet<Principal>();
        for (TimeChannel<? extends Channel> timeChannel : this.channels.values()) {
        	if (!timeChannel.getChannel().isConnected())
        		continue;
            for (Subscription subscription : timeChannel.getChannel().getSubscriptions()) {
                if (destination.equals(subscription.getDestination())) {
                    userPrincipals.add(timeChannel.getChannel().getUserPrincipal());
                    break;
                }
            }
        }
        return userPrincipals;
    }

    public List<Channel> findConnectedChannelsByUser(String name) {
        List<Channel> channels = new ArrayList<Channel>();
        for (TimeChannel<? extends Channel> timeChannel : this.channels.values()) {
        	if (!timeChannel.getChannel().isConnected())
        		continue;
            if (timeChannel.getChannel().getUserPrincipal() != null && timeChannel.getChannel().getUserPrincipal().getName().equals(name))
                channels.add(timeChannel.getChannel());
        }
        return channels;
    }

    public Channel findChannelByClientId(String clientId) {
        if (clientId == null)
            return null;

        TimeChannel<?> timeChannel = channels.get(clientId);
        if (timeChannel != null)
            return timeChannel.getChannel();
        return null;
    }
    
    public Channel findCurrentChannel(String destination) {
    	DistributedData gdd = getGraniteConfig().getDistributedDataFactory().getInstance();
    	if (gdd != null) {
    		String clientId = gdd.getDestinationClientId(destination);
    		return findChannelByClientId(clientId);
    	}
    	return null;
    }

	public Message publishMessage(AsyncMessage message) {
    	return publishMessage(serverChannel, message);
    }
	
    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
    	boolean shouldReleaseContext = GraniteContext.getCurrentInstance() == null;
        try {
            initThread(null, fromChannel != null ? fromChannel.getClientType() : serverChannel.getClientType());
            
            return handlePublishMessage(null, message, fromChannel != null ? fromChannel : serverChannel);
        }
        finally {
        	if (shouldReleaseContext)
        		releaseThread();
        }
    }

    public Message sendRequest(Channel fromChannel, AsyncMessage message) {
    	boolean shouldReleaseContext = GraniteContext.getCurrentInstance() == null;
        try {
            initThread(null, fromChannel != null ? fromChannel.getClientType() : serverChannel.getClientType());
            
            if (message.getMessageId() == null)
                message.setMessageId(UUIDUtil.randomUUID());
            message.setTimestamp(System.currentTimeMillis());
            
            long timeout = 5000L;
            if (message.getTimeToLive() > 0)
                timeout = message.getTimeToLive();
            
            AsyncReply asyncReply = new AsyncReply(message.getTimestamp()+timeout);
            asyncReplies.put(message.getMessageId(), asyncReply);
            
            handlePublishMessage(null, message, fromChannel != null ? fromChannel : serverChannel);
            
            if (!asyncReply.await(timeout, TimeUnit.MILLISECONDS)) {
                ErrorMessage errorMessage = new ErrorMessage(message, true);
                errorMessage.setFaultCode("Server.Messaging.ReplyTimeout");
                errorMessage.setFaultString("Reply timeout for message " + message.getMessageId());
                return errorMessage;
            }
            
            return asyncReply.getReply();
        }
        catch (InterruptedException e) {
            ErrorMessage errorMessage = new ErrorMessage(message, true);
            errorMessage.setFaultCode("Server.Messaging.ReplyInterrupted");
            errorMessage.setFaultString("Reply interrupted for message " + message.getMessageId());
            return errorMessage;
        }
        finally {
        	if (shouldReleaseContext)
        		releaseThread();
        }
    }

    private final ConcurrentMap<String, AsyncReply> asyncReplies = new ConcurrentHashMap<String, AsyncReply>();

    private static class AsyncReply {

        private final CountDownLatch waitForReply;
        private final long destroyTimestamp;
        private Message reply = null;

        public AsyncReply(long destroyTimestamp) {
            this.waitForReply = new CountDownLatch(1);
            this.destroyTimestamp = destroyTimestamp;
        }
        
        public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        	return waitForReply.await(timeout, unit);
        }

        public void reply(Message reply) {
            this.reply = reply;
            waitForReply.countDown();
        }

        public Message getReply() {
            return reply;
        }

        public long getDestroyTimestamp() {
            return destroyTimestamp;
        }
    }

    private class AsyncRepliesCleanup extends TimerTask {
    	
        @Override
        public void run() {
            long time = System.currentTimeMillis() - 30000L;
            for (Iterator<Map.Entry<String, AsyncReply>> ie = asyncReplies.entrySet().iterator(); ie.hasNext(); ) {
                Map.Entry<String, AsyncReply> e = ie.next();
                if (time > e.getValue().getDestroyTimestamp())
                    ie.remove();
            }
        }
    }

    
    private Message authorize(GraniteConfig config, final Message message, final AbstractSecurityContext securityContext) {
        try {
            if (securityContext.isSecured() && config.hasSecurityService()) {
                if (!config.getSecurityService().acceptsContext()) {
                    log.debug("Could not process security operation in non supported current context: %s", message);
                    return new ErrorMessage(message, SecurityServiceException.newNotLoggedInException("Invalid context"), true);
                }
                
                return (Message)config.getSecurityService().authorize(securityContext);
            }

            return (Message)securityContext.invoke();
        }
        catch (Exception e) {
            return new ErrorMessage(message, e, true);
        }
    }

    
    private Message handlePingMessage(ChannelFactory<?> channelFactory, CommandMessage message) {
        
    	Channel channel = createChannel(channelFactory, (String)message.getClientId());
    	
        AsyncMessage reply = new AcknowledgeMessage(message);
        reply.setClientId(channel.getId());
        Map<String, Object> advice = new HashMap<String, Object>();
        advice.put(RECONNECT_INTERVAL_MS_KEY, Long.valueOf(gravityConfig.getReconnectIntervalMillis()));
        advice.put(RECONNECT_MAX_ATTEMPTS_KEY, Long.valueOf(gravityConfig.getReconnectMaxAttempts()));
        advice.put(ENCODE_MESSAGE_BODY_KEY, Boolean.valueOf(gravityConfig.isEncodeMessageBody()));
        reply.setBody(advice);
        reply.setDestination(message.getDestination());

        log.debug("handshake.handle: reply=%s", reply);

        return reply;
    }
    
    private static class LoginSecurityContext extends AbstractSecurityContext {
    	
    	private static final List<String> EMPTY_CHANNELS = Collections.emptyList();
    	private static final Destination EMPTY_DESTINATION = new Destination("__login__", EMPTY_CHANNELS, new XMap(), null, null, null); 
    	
    	public LoginSecurityContext(CommandMessage message) {
    		super(message, EMPTY_DESTINATION);
    	}
    	
    	@Override
		public Object invoke() throws Exception {
			return null;
		}
    }

    private Message handleSecurityMessage(final ChannelFactory<?> channelFactory, final CommandMessage message) {
    	GraniteContext context = GraniteContext.getCurrentInstance();
        GraniteConfig config = context.getGraniteConfig();
        
        Message response = null;
        
        if (!config.hasSecurityService())
            log.warn("Ignored security operation (no security settings in granite-config.xml): %s", message);
        else if (!config.getSecurityService().acceptsContext()) {
            log.debug("Could not process security operation in non supported current context: %s", message);
        	response = new ErrorMessage(message, SecurityServiceException.newNotLoggedInException("Invalid context"), true);
        }
        else {
            SecurityService securityService = config.getSecurityService();
            try {
                Channel channel = getChannel(channelFactory, (String)message.getClientId());
                
                if (message.isLoginOperation()) {
                    // Try to retrieve an existing authentication if the current session has already been authenticated by remoting
                    response = authorize(config, message, new LoginSecurityContext(message));
                    
                    Principal principal = context.getPrincipal();
                    if (principal == null)
                    	principal = securityService.login(message.getBody(), (String)message.getHeader(Message.CREDENTIALS_CHARSET_HEADER));
                    
                    if (principal == null)
                    	response = new ErrorMessage(message, SecurityServiceException.newNotLoggedInException("Invalid context"), true);
                    else {
                    	response = null;
                    	if (channel != null)
                    		channel.setUserPrincipal(principal);
                    }
                }
                else {
                    securityService.logout();

                    if (channel != null)
                        channel.setUserPrincipal(null);
                }
            }
            catch (Exception e) {
                if (e instanceof SecurityServiceException)
                    log.debug(e, "Could not process security operation: %s", message);
                else
                    log.error(e, "Could not process security operation: %s", message);
                response = new ErrorMessage(message, e, true);
            }
        }

        if (response == null) {
            response = new AcknowledgeMessage(message, true);
            // For SDK 2.0.1_Hotfix2.
            if (message.isSecurityOperation())
                response.setBody("success");
        }

        return response;
    }

    private Message handleConnectMessage(final ChannelFactory<?> channelFactory, CommandMessage message) {
        Channel channel = getChannel(channelFactory, (String)message.getClientId());

        if (channel == null)
            return handleUnknownClientMessage(message);

        return null;
    }

    private Message handleDisconnectMessage(final ChannelFactory<?> channelFactory, CommandMessage message) {
        Channel channel = getChannel(channelFactory, (String)message.getClientId());
        if (channel == null)
            return handleUnknownClientMessage(message);

        removeChannel(channel.getId(), false);

        AcknowledgeMessage reply = new AcknowledgeMessage(message);
        reply.setDestination(message.getDestination());
        reply.setClientId(channel.getId());
        return reply;
    }

    private Message handleSubscribeMessage(final ChannelFactory<?> channelFactory, final CommandMessage message) {
    	return handleSubscribeMessage(channelFactory, message, true);
    }
    
    private Message handleSubscribeMessage(final ChannelFactory<?> channelFactory, final CommandMessage message, final boolean saveMessageInSession) {

        final GraniteContext context = GraniteContext.getCurrentInstance();

        // Get and check destination.
        final Destination destination = ((ServicesConfig)context.getServicesConfig()).findDestinationById(
            message.getMessageRefType(),
            message.getDestination()
        );

        if (destination == null)
            return getInvalidDestinationError(message);


        GravityInvocationContext invocationContext = new GravityInvocationContext(message, destination) {
			@Override
			public Object invoke() throws Exception {
		        // Subscribe...
		        Channel channel = getChannel(channelFactory, (String)message.getClientId());
		        if (channel == null)
		            return handleUnknownClientMessage(message);
		        
		        if (context.getPrincipal() != null)
		        	channel.setUserPrincipal(context.getPrincipal());
		        
		        String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
		        if (subscriptionId == null) {
		            subscriptionId = UUIDUtil.randomUUID();
		            message.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
		        }
		        
		        DistributedData gdd = graniteConfig.getDistributedDataFactory().getInstance();
		        if (gdd != null) {
		            if (!gdd.hasChannelId(channel.getId())) {
		                gdd.addChannelId(channel.getId(), channel.getFactory().getClass().getName(), context.getClientType());
                        log.debug("Stored channel %s in distributed data", channel.getId());
                    }
		            
		        	if (Boolean.TRUE.toString().equals(destination.getProperties().get("session-selector"))) {
		        		String selector = gdd.getDestinationSelector(destination.getId());
		        		log.debug("Session selector found: %s", selector);
		        		if (selector != null)
		        			message.setHeader(CommandMessage.SELECTOR_HEADER, selector);
		        	}
		        }

		        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
		        
		        AsyncMessage reply = (AsyncMessage)adapter.manage(channel, message);
		        
		        postManage(channel);
		        
		        if (saveMessageInSession && !(reply instanceof ErrorMessage)) {
		        	// Save subscription message in distributed data (clustering).
		        	try {
			        	if (gdd != null) {
			        		log.debug("Saving new subscription message for channel: %s - %s", channel.getId(), message);
			        		gdd.addSubcription(channel.getId(), message);
			        	}
		        	}
		        	catch (Exception e) {
		        		log.error(e, "Could not add subscription in distributed data: %s - %s", channel.getId(), subscriptionId);
		        	}
		        }
	        
		        if (!(reply instanceof ErrorMessage))
		        	notifySubscribed(channel, subscriptionId);
		        
		        reply.setDestination(message.getDestination());
		        reply.setClientId(channel.getId());
		        reply.getHeaders().putAll(message.getHeaders());

		        if (gdd != null && message.getDestination() != null) {
		        	gdd.setDestinationClientId(message.getDestination(), channel.getId());
		        	gdd.setDestinationSubscriptionId(message.getDestination(), subscriptionId);
		        }

		        return reply;
			}       	
        };

        // Check security 1 (destination).
        if (destination.getSecurizer() instanceof GravityDestinationSecurizer) {
            try {
                ((GravityDestinationSecurizer)destination.getSecurizer()).canSubscribe(invocationContext);
            } 
            catch (Exception e) {
                return new ErrorMessage(message, e);
            }
        }
        
        // Check security 2 (security service).
        return authorize((GraniteConfig)context.getGraniteConfig(), message, invocationContext);
    }

    private Message handleUnsubscribeMessage(final ChannelFactory<?> channelFactory, CommandMessage message) {
    	Channel channel = getChannel(channelFactory, (String)message.getClientId());
        if (channel == null)
            return handleUnknownClientMessage(message);

        AsyncMessage reply = null;

        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
        
        reply = (AcknowledgeMessage)adapter.manage(channel, message);
        
        postManage(channel);
        
        if (!(reply instanceof ErrorMessage)) {
        	String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
        	
        	// Remove subscription message in distributed data (clustering).
        	try {
		        DistributedData gdd = graniteConfig.getDistributedDataFactory().getInstance();
		        if (gdd != null) {
		        	log.debug("Removing subscription message from channel info: %s - %s", channel.getId(), subscriptionId);
	    			gdd.removeSubcription(channel.getId(), subscriptionId);
		        }
        	}
        	catch (Exception e) {
        		log.error(
        			e, "Could not remove subscription from distributed data: %s - %s",
        			channel.getId(), message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER)
        		);
        	}
        	
        	notifyUnsubscribed(channel, subscriptionId);
        }
        
        reply.setDestination(message.getDestination());
        reply.setClientId(channel.getId());
        reply.getHeaders().putAll(message.getHeaders());

        return reply;
    }
    
    protected void postManage(Channel channel) {
    }

    private Message handlePublishMessage(final ChannelFactory<?> channelFactory, final AsyncMessage message) {
    	return handlePublishMessage(channelFactory, message, null);
    }
    
    private Message handlePublishMessage(final ChannelFactory<?> channelFactory, final AsyncMessage message, final Channel channel) {

        GraniteContext context = GraniteContext.getCurrentInstance();

        // Get and check destination.
        Destination destination = ((ServicesConfig)context.getServicesConfig()).findDestinationById(
            message.getClass().getName(),
            message.getDestination()
        );

        if (destination == null)
            return getInvalidDestinationError(message);

        if (message.getMessageId() == null)
            message.setMessageId(UUIDUtil.randomUUID());
        message.setTimestamp(System.currentTimeMillis());
        if (channel != null)
            message.setClientId(channel.getId());
        
        GravityInvocationContext invocationContext = new GravityInvocationContext(message, destination) {
            @Override
            public boolean isSecured() {
            	return channel != serverChannel;
            }
        	
			@Override
			public Object invoke() throws Exception {
		        // Publish...
		        Channel fromChannel = channel;
		        if (fromChannel == null)
		        	fromChannel = getChannel(channelFactory, (String)message.getClientId());
		        if (fromChannel == null)
		            return handleUnknownClientMessage(message);

                if (message.getCorrelationId() != null && message.getCorrelationId().length() > 0) {
                    AsyncReply asyncReply = asyncReplies.remove(message.getCorrelationId());
                    if (asyncReply != null) {
                        asyncReply.reply(message);
                        
                        AcknowledgeMessage acknowledgeMessage = new AcknowledgeMessage(message, true);
                        acknowledgeMessage.setDestination(message.getDestination());

                        return acknowledgeMessage;
                    }

                    ErrorMessage errorMessage = new ErrorMessage(message, true);
                    errorMessage.setFaultCode("Server.Messaging.InvalidReply");
                    errorMessage.setFaultString("Unknown correlationId " + message.getCorrelationId());
                    errorMessage.setDestination(message.getDestination());

                    return errorMessage;
                }

		        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
		        
		        AsyncMessage reply = (AsyncMessage)adapter.invoke(fromChannel, message);

		        reply.setDestination(message.getDestination());
		        reply.setClientId(fromChannel.getId());

		        return reply;
			}       	
        };

        // Check security 1 (destination).
        if (destination.getSecurizer() instanceof GravityDestinationSecurizer) {
            try {
                ((GravityDestinationSecurizer)destination.getSecurizer()).canPublish(invocationContext);
            } 
            catch (Exception e) {
                return new ErrorMessage(message, e, true);
            }
        }
        
        // Check security 2 (security service).
    	return authorize((GraniteConfig)context.getGraniteConfig(), message, invocationContext);
    }

    private Message handleUnknownClientMessage(Message message) {
        ErrorMessage reply = new ErrorMessage(message, true);
        reply.setFaultCode("Server.Call.UnknownClient");
        reply.setFaultString("Unknown client");
        return reply;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

    private ErrorMessage getInvalidDestinationError(Message message) {

        String messageType = message.getClass().getName();
        if (message instanceof CommandMessage)
            messageType += '[' + ((CommandMessage)message).getMessageRefType() + ']';

        ErrorMessage reply = new ErrorMessage(message, true);
        reply.setFaultCode("Server.Messaging.InvalidDestination");
        reply.setFaultString(
            "No configured destination for id: " + message.getDestination() +
            " and message type: " + messageType
        );
        return reply;
    }

    private static class ServerChannel extends AbstractChannel implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public ServerChannel(GravityInternal gravity, String channelId, ChannelFactory<ServerChannel> factory, String clientType) {
    		super(gravity, channelId, factory, clientType);
    	}

		@Override
		public GravityInternal getGravity() {
			return gravity;
		}
		
		public void close(boolean timeout) {
		}		

		@Override
		public void receive(AsyncMessage message) throws MessageReceivingException {
		}

		@Override
		protected boolean hasAsyncHttpContext() {
			return false;
		}

		@Override
		protected AsyncHttpContext acquireAsyncHttpContext() {
			return null;
		}

		@Override
		protected void releaseAsyncHttpContext(AsyncHttpContext context) {
		}
    }
}
