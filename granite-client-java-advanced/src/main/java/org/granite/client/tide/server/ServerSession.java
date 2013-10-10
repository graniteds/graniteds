/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.TopicAgent;
import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelBuilder;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.SessionAwareChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.platform.Platform;
import org.granite.client.tide.ApplicationConfigurable;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.Identity;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.Update;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.validation.InvalidValue;
import org.granite.config.GraniteConfig;
import org.granite.logging.Logger;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.ContentType;


/**
 * @author William DRAI
 */
@ApplicationConfigurable
@Named
public class ServerSession implements ContextAware {

	private static Logger log = Logger.getLogger(ServerSession.class);
	
	public static final String SERVER_TIME_TAG = "org.granite.time";
	public static final String SESSION_ID_TAG = "org.granite.sessionId";
	public static final String SESSION_EXP_TAG = "org.granite.sessionExp";
    public static final String CONVERSATION_TAG = "conversationId";
    public static final String CONVERSATION_PROPAGATION_TAG = "conversationPropagation";
    public static final String IS_LONG_RUNNING_CONVERSATION_TAG = "isLongRunningConversation";
    public static final String WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG = "wasLongRunningConversationEnded";
    public static final String WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG = "wasLongRunningConversationCreated";
    public static final String IS_FIRST_CALL_TAG = "org.granite.client.tide.isFirstCall";
    public static final String IS_FIRST_CONVERSATION_CALL_TAG = "org.granite.client.tide.isFirstConversationCall";
	
    public static final String CONTEXT_RESULT = "org.granite.tide.result";
    public static final String CONTEXT_FAULT = "org.granite.tide.fault";
    
	public static final String LOGIN = "org.granite.client.tide.login";
	public static final String LOGOUT = "org.granite.client.tide.logout";
	public static final String SESSION_EXPIRED = "org.granite.client.tide.sessionExpired";


    private ServerApp serverApp = new ServerApp();
	private ContentType contentType = ContentType.JMF_AMF;
	private Class<? extends ChannelFactory> channelFactoryClass = null;
    private Transport remotingTransport = null;
    private Transport messagingTransport = null;
    private Map<String, Transport> messagingTransports = new HashMap<String, Transport>();
    
	private Context context = null;

	private Status status = new DefaultStatus();
	
	private String sessionId = null;
	private boolean isFirstCall = true;
	
	private LogoutState logoutState = new LogoutState();
		
	private String destination = "server";
	private Object appContext = null;
    private ChannelBuilder defaultChannelBuilder = null;
	private ChannelFactory channelFactory;
    private RemotingChannel remotingChannel = null;
    private Map<String, MessagingChannel> messagingChannelsByType = new HashMap<String, MessagingChannel>();
	protected Map<String, RemoteService> remoteServices = new HashMap<String, RemoteService>();
	protected Map<String, TopicAgent> topicAgents = new HashMap<String, TopicAgent>();
	private Set<String> packageNames = new HashSet<String>();
	
	
    public ServerSession() throws Exception {
    	// Used for testing
    }
    
    public ServerSession(String contextRoot, String serverName, int serverPort) throws Exception {
    	this(contextRoot, false, serverName, serverPort, null);
    }

    public ServerSession(String contextRoot, boolean secure, String serverName, int serverPort) throws Exception {
        this(contextRoot, secure, serverName, serverPort, null);
    }

    public ServerSession(String contextRoot, Boolean secure, String serverName, int serverPort, String destination) throws Exception {
        if (destination != null)
        	this.destination = destination;
        this.serverApp = new ServerApp(contextRoot, secure, serverName, serverPort);
    }

    public ServerSession(ServerApp serverApp) throws Exception {
        this.serverApp = serverApp;
    }

    public ServerSession(ServerApp serverApp, String destination) throws Exception {
        this.serverApp = serverApp;
        this.destination = destination;
    }

    public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		if (contentType == null)
			throw new NullPointerException("contentType cannot be null");
		this.contentType = contentType;
	}

    public void setDefaultChannelBuilder(ChannelBuilder channelBuilder) {
        this.defaultChannelBuilder = channelBuilder;
    }
	
	public void setChannelFactoryClass(Class<? extends ChannelFactory> channelFactoryClass) {
	    this.channelFactoryClass = channelFactoryClass;
	}

	public void setServerApp(ServerApp serverApp) {
        this.serverApp = serverApp;
    }

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	public Context getContext() {
		return this.context;
	}
	
	public void setAppContext(Object appContext) {
	    this.appContext = appContext;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}

	public void setRemotingTransport(Transport transport) {
		this.remotingTransport = transport;
	}
	
	public void setMessagingTransport(Transport transport) {
		this.messagingTransport = transport;
	}

    public void setMessagingTransport(String channelType, Transport transport) {
        this.messagingTransports.put(channelType, transport);
    }

	public void addRemoteAliasPackage(String packageName) {		
		this.packageNames.add(packageName);
	}
	
	public void setRemoteAliasPackage(Set<String> packageNames) {
		this.packageNames.clear();
		this.packageNames.addAll(packageNames);
	}
	
	private GraniteConfig graniteConfig = null;
	
	public Object convert(Object value, Type expectedType) {
		if (contentType == ContentType.JMF_AMF || graniteConfig == null)
			return value;
		return graniteConfig.getConverters().convert(value, expectedType);
	}
	
	@PostConstruct
	public void start() throws Exception {
	    if (channelFactory != null)    // Already started
	        return;
	    
	    ClientAliasRegistry aliasRegistry = new ClientAliasRegistry();
	    aliasRegistry.registerAlias(InvalidValue.class);

	    if (channelFactoryClass != null) {
	        Constructor<? extends ChannelFactory> constructor = null;
	        try {
	            constructor = channelFactoryClass.getConstructor(Object.class, Configuration.class);
	            Configuration configuration = Platform.getInstance().newConfiguration();
	            configuration.setClientType(ClientType.JAVA);
	            configuration.load();
	            graniteConfig = configuration.getGraniteConfig();
	            channelFactory = constructor.newInstance(appContext, configuration);
	        }
	        catch (NoSuchMethodException e) {
	            constructor = channelFactoryClass.getConstructor(Object.class);
                channelFactory = constructor.newInstance(appContext);
	        }	        
	    }
	    else if (contentType == ContentType.JMF_AMF)
			channelFactory = new JMFChannelFactory(appContext);
		else {
			Configuration configuration = Platform.getInstance().newConfiguration();
			configuration.setClientType(ClientType.JAVA);
			configuration.load();
			graniteConfig = configuration.getGraniteConfig();
			channelFactory = new AMFChannelFactory(appContext, configuration);
		}
        channelFactory.setAliasRegistry(aliasRegistry);
		channelFactory.setScanPackageNames(packageNames);
		
		if (remotingTransport != null) {
			channelFactory.setRemotingTransport(remotingTransport);
            remotingTransport.setStatusHandler(statusHandler);
        }
		if (messagingTransport != null) {
			channelFactory.setMessagingTransport(messagingTransport);
            messagingTransport.setStatusHandler(statusHandler);
        }
        for (Map.Entry<String, Transport> me : messagingTransports.entrySet()) {
            channelFactory.setMessagingTransport(me.getKey(), me.getValue());
            me.getValue().setStatusHandler(statusHandler);
        }
        if (defaultChannelBuilder != null)
            channelFactory.setDefaultChannelBuilder(defaultChannelBuilder);
		
		if (defaultTimeToLive >= 0)
		    channelFactory.setDefaultTimeToLive(defaultTimeToLive);
		
		channelFactory.start();

        remotingChannel = channelFactory.newRemotingChannel("graniteamf", serverApp, 1);

		sessionExpirationTimer = Executors.newSingleThreadScheduledExecutor();
	}
	
	@PreDestroy
	public void stop()throws Exception {
		try {
			if (sessionExpirationFuture != null) {
				sessionExpirationFuture.cancel(false);
				sessionExpirationFuture = null;
			}
			if (sessionExpirationTimer != null) {
				sessionExpirationTimer.shutdownNow();
				sessionExpirationTimer = null;
			}
		}
		finally {
			if (channelFactory != null) {
				channelFactory.stop();
				channelFactory = null;
			}
	            
            remotingChannel = null;
			messagingChannelsByType.clear();
		}
	}
	
	
	public static interface ServiceFactory {
		
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination);
		
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic);
		
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic);
	}
	
	private static class DefaultServiceFactory implements ServiceFactory {
		
		@Override
		public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination) {
			return new RemoteService(remotingChannel, destination);
		}
		
		@Override
		public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Producer(messagingChannel, destination, topic);
		}
		
		@Override
		public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic) {
			return new Consumer(messagingChannel, destination, topic);
		}
	}
	
	private ServiceFactory serviceFactory = new DefaultServiceFactory();
	
	public void setServiceFactory(ServiceFactory serviceFactory) {
		this.serviceFactory = serviceFactory;
	}
	
	public RemoteService getRemoteService() {
		return getRemoteService(destination);
	}
	public synchronized RemoteService getRemoteService(String destination) {
		if (remotingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		RemoteService remoteService = remoteServices.get(destination);
		if (remoteService == null) {
			remoteService = serviceFactory.newRemoteService(remotingChannel, destination);
			remoteServices.put(destination, remoteService);
		}
		return remoteService;
	}

    public MessagingChannel getMessagingChannel(String channelType) {
        MessagingChannel messagingChannel = messagingChannelsByType.get(channelType);
        if (messagingChannel != null)
            return messagingChannel;

        messagingChannel = channelFactory.newMessagingChannel(channelType, channelType + "amf", serverApp);
        return messagingChannel;
    }

	public synchronized Consumer getConsumer(String channelType, String destination, String topic) {
        MessagingChannel messagingChannel = getMessagingChannel(channelType);
		if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined in server session for type " + channelType + "");
		
		String key = destination + '@' + topic;
		TopicAgent consumer = topicAgents.get(key);
		if (consumer == null) {
			consumer = serviceFactory.newConsumer(messagingChannel, destination, topic);
			topicAgents.put(key, consumer);
		}
		return consumer instanceof Consumer ? (Consumer)consumer : null;
	}

	public synchronized Producer getProducer(String channelType, String destination, String topic) {
        MessagingChannel messagingChannel = getMessagingChannel(channelType);
        if (messagingChannel == null)
			throw new IllegalStateException("Channel not defined for server session");
		
		String key = destination + '@' + topic;
		TopicAgent producer = topicAgents.get(key);
		if (producer == null) {
			producer = serviceFactory.newProducer(messagingChannel, destination, topic);
			topicAgents.put(key, producer);
		}
		return producer instanceof Producer ? (Producer)producer : null;
	}
	
	public boolean isFirstCall() {
		return isFirstCall;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public void restoreSessionId(String sessionId) {
		this.sessionId = sessionId;

        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
			messagingChannel.setSessionId(sessionId);
	}
	
	public boolean isLogoutInProgress() {
		return logoutState.logoutInProgress;
	}
	
	public void trackCall() {
		isFirstCall = false;
	}
	
	private ScheduledExecutorService sessionExpirationTimer = null;
	private ScheduledFuture<?> sessionExpirationFuture = null;
	
	private Runnable sessionExpirationTask = new Runnable() {
		@Override
		public void run() {
			Identity identity = context.byType(Identity.class);
			identity.checkLoggedIn(null);
		}
	};
	
	private void rescheduleSessionExpirationTask(long serverTime, int sessionExpirationDelay) {
		Identity identity = context.byType(Identity.class);
		if (identity == null || !identity.isLoggedIn())	// No session expiration tracking if user not logged in
			return;
		
		long clientOffset = serverTime - new Date().getTime();
		sessionExpirationFuture = sessionExpirationTimer.schedule(sessionExpirationTask, clientOffset + sessionExpirationDelay*1000L + 1500L, TimeUnit.MILLISECONDS);
	}
	
	public void handleResultEvent(Event event) {
		if (sessionExpirationFuture != null)
			sessionExpirationFuture.cancel(false);
		
		String oldSessionId = sessionId;
		
		if (event instanceof ResultEvent) {
			ResultMessage message = ((ResultEvent)event).getMessage();
			sessionId = (String)message.getHeader(SESSION_ID_TAG);
			if (sessionId != null) {
				long serverTime = (Long)message.getHeader(SERVER_TIME_TAG);
				int sessionExpirationDelay = (Integer)message.getHeader(SESSION_EXP_TAG);
				rescheduleSessionExpirationTask(serverTime, sessionExpirationDelay);
			}
		}
		else if (event instanceof IncomingMessageEvent<?>)
			sessionId = (String)((IncomingMessageEvent<?>)event).getMessage().getHeader(SESSION_ID_TAG);
		
		if (sessionId == null || !sessionId.equals(oldSessionId))
		    log.info("Received new sessionId %s", sessionId);
		
		if (oldSessionId != null || sessionId != null) {
            for (MessagingChannel messagingChannel : messagingChannelsByType.values())
			    messagingChannel.setSessionId(sessionId);
        }
		
		isFirstCall = false;
		status.setConnected(true);
	}
	
	public void handleFaultEvent(FaultEvent event, FaultMessage emsg) {
		if (sessionExpirationFuture != null)
			sessionExpirationFuture.cancel(false);
				
        String oldSessionId = sessionId;

		sessionId = (String)event.getMessage().getHeader(SESSION_ID_TAG);
		if (sessionId != null) {
			long serverTime = (Long)event.getMessage().getHeader(SERVER_TIME_TAG);
			int sessionExpirationDelay = (Integer)event.getMessage().getHeader(SESSION_EXP_TAG);
			rescheduleSessionExpirationTask(serverTime, sessionExpirationDelay);
		}
		
        if (sessionId == null || !sessionId.equals(oldSessionId))
            log.info("Received new sessionId %s", sessionId);
        
		if (oldSessionId != null || sessionId != null) {
            for (MessagingChannel messagingChannel : messagingChannelsByType.values())
                messagingChannel.setSessionId(sessionId);
        }
		
        if (emsg != null && emsg.getCode().equals(Code.SERVER_CALL_FAILED))
        	status.setConnected(false);            
	}
	
	public void handleIssueEvent(IssueEvent event) {
    	status.setConnected(false);            
	}
	
	private final TransportStatusHandler statusHandler = new TransportStatusHandler() {
		
		private int busyCount = 0;
		
		@Override
		public void handleIO(boolean active) {			
			if (active)
				busyCount++;
			else
				busyCount--;
			status.setBusy(busyCount > 0);
			notifyIOListeners(status.isBusy());
		}
		
		@Override
		public void handleException(TransportException e) {
			log.warn(e, "Transport failed");
			notifyExceptionListeners(e);
		}
	};
	
	public Transport getRemotingTransport() {
		return channelFactory != null ? channelFactory.getRemotingTransport() : null;
	}
	
	public Transport getMessagingTransport() {
		return channelFactory != null ? channelFactory.getMessagingTransport() : null;
	}
	
	
	/**
	 * 	Implementation of login
	 * 	
	 * 	@param username user name
	 *  @param password password
	 */
    public void login(String username, String password) {
    	remotingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
    	    messagingChannel.setCredentials(new UsernamePasswordCredentials(username, password));
    }

	/**
	 * 	Implementation of login using a specific charset for username/password encoding
	 * 	
	 * 	@param username user name
	 *  @param password password
	 *  @param charset charset used for encoding
	 */
    public void login(String username, String password, Charset charset) {
    	remotingChannel.setCredentials(new UsernamePasswordCredentials(username, password, charset));
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
    	    messagingChannel.setCredentials(new UsernamePasswordCredentials(username, password, charset));
    }

    public void afterLogin() {
		log.info("Application session authenticated");
		
		context.getEventBus().raiseEvent(context, LOGIN);
    }
    
    public void sessionExpired() {
		log.info("Application session expired");

        if (remotingChannel.isAuthenticated())
            remotingChannel.logout(false);
        for (MessagingChannel messagingChannel : messagingChannelsByType.values()) {
            if (messagingChannel.isAuthenticated())
                messagingChannel.logout(false);
        }

		sessionId = null;
		if (remotingChannel instanceof SessionAwareChannel)
		    ((SessionAwareChannel)remotingChannel).setSessionId(null);
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
            messagingChannel.setSessionId(null);
		
		logoutState.sessionExpired();
		
		context.getEventBus().raiseEvent(context, SESSION_EXPIRED);		
		context.getEventBus().raiseEvent(context, LOGOUT);		
    }
    
    public void loggedOut(TideRpcEvent event) {
    	log.info("User logged out");
    	
        sessionId = null;
        if (remotingChannel instanceof SessionAwareChannel)
            ((SessionAwareChannel)remotingChannel).setSessionId(null);
        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
            messagingChannel.setSessionId(null);
    	
    	logoutState.loggedOut(event);
    }
    
	/**
	 * 	Implementation of logout
	 * 	
	 * 	@param logoutObserver observer that will be notified of logout result
	 */
	public void logout(final Observer logoutObserver) {
		if (sessionExpirationFuture != null) {
			sessionExpirationFuture.cancel(false);
			sessionExpirationFuture = null;
		}
		
		logoutState.logout(logoutObserver, new TimerTask() {
			@Override
			public void run() {
				log.info("Force session logout");
				logoutState.logout(logoutObserver);
				tryLogout();
			}
		});
		
		context.getEventBus().raiseEvent(context, LOGOUT);
		
        tryLogout();
    }
	
	/**
	 * 	Notify the framework that it should wait for a async operation before effectively logging out.
	 *  Only if a logout has been requested.
	 */
	public void checkWaitForLogout() {
		isFirstCall = false;
		
		logoutState.checkWait();
	}
	
	/**
	 * 	Try logout. Should be called after all remote operations on a component are finished.
	 *  The effective logout is done when all remote operations on all components have been notified as finished.
	 */
	public void tryLogout() {
		if (logoutState.stillWaiting())
			return;
		
		if (logoutState.isSessionExpired()) {  // Don't remotely logout again if we detected a session expired
		    logoutState.loggedOut(null);
		    return;
		}
		
		if (remotingChannel.isAuthenticated()) {
			remotingChannel.logout(new ResultFaultIssuesResponseListener() {
				@Override
				public void onResult(final ResultEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.info("Application session logged out");
							
							handleResult(context, null, "logout", null, null, null);
							context.getContextManager().destroyContexts(false);
							
							logoutState.loggedOut(new TideResultEvent<Object>(context, ServerSession.this, null, event.getResult()));
						}
					});
				}
	
				@Override
				public void onFault(final FaultEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.error("Could not log out %s", event.getDescription());
							
							handleFault(context, null, "logout", event.getMessage());
							
					        Fault fault = new Fault(event.getCode(), event.getDescription(), event.getDetails());
					        fault.setContent(event.getMessage());
					        fault.setCause(event.getCause());				        
							logoutState.loggedOut(new TideFaultEvent(context, ServerSession.this, null, fault, event.getExtended()));
						}
					});
				}
				
				@Override
				public void onIssue(final IssueEvent event) {
					context.callLater(new Runnable() {
						public void run() {
							log.error("Could not logout %s", event.getType());
							
							handleFault(context, null, "logout", null);
							
					        Fault fault = new Fault(Code.SERVER_CALL_FAILED, event.getType().name(), "");
							logoutState.loggedOut(new TideFaultEvent(context, ServerSession.this, null, fault, null));
						}
					});
				}
			});
		}

        for (MessagingChannel messagingChannel : messagingChannelsByType.values()) {
		    if (messagingChannel != remotingChannel && messagingChannel.isAuthenticated())
			    messagingChannel.logout();
        }
		
		isFirstCall = true;
	}
	
	
    /**
     *  (Almost) abstract method: manages a remote call result
     *  This should be called by the implementors at the end of the result processing
     * 
     * 	@param context source context of the remote call
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param invocationResult invocation result object
     *  @param result result object
     *  @param mergeWith previous value with which the result will be merged
     */
    public void handleResult(Context context, String componentName, String operation, InvocationResult invocationResult, Object result, Object mergeWith) {
        log.debug("result {0}", result);
        
        List<Update> updates = null;
        EntityManager entityManager = context.getEntityManager();
        
        try {
            // Clear flash context variable for Grails/Spring MVC
            context.remove("flash");
            
            MergeContext mergeContext = entityManager.initMerge();
            mergeContext.setServerSession(this);
            
            boolean mergeExternal = true;
            if (invocationResult != null) {
                mergeExternal = invocationResult.getMerge();
                
                if (invocationResult.getUpdates() != null && invocationResult.getUpdates().length > 0) {
                    updates = new ArrayList<Update>(invocationResult.getUpdates().length);
                    for (Object[] u : invocationResult.getUpdates())
                        updates.add(Update.forUpdate((String)u[0], u[1]));
                    entityManager.handleUpdates(mergeContext, null, updates);
                }
            }
            
            // Merges final result object
            if (result != null) {
                if (mergeExternal)
                    result = entityManager.mergeExternal(mergeContext, result, mergeWith, null, null, false);
                else
                    log.debug("skipped merge of remote result");
                if (invocationResult != null)
                    invocationResult.setResult(result);
            }
        }
        finally {
            MergeContext.destroy(entityManager);
        }

        // Dispatch received data update events         
        if (invocationResult != null) {
            // Dispatch received data update events
            if (updates != null)
                entityManager.raiseUpdateEvents(context, updates);
            
            // TODO: dispatch received context events
//            List<ContextEvent> events = invocationResult.getEvents();
//            if (events != null && events.size() > 0) {
//                for (ContextEvent event : events) {
//                    if (event.params[0] is Event)
//                        meta_dispatchEvent(event.params[0] as Event);
//                    else if (event.isTyped())
//                        meta_internalRaiseEvent("$TideEvent$" + event.eventType, event.params);
//                    else
//                        _tide.invokeObservers(this, TideModuleContext.currentModulePrefix, event.eventType, event.params);
//                }
//            }
        }
        
        log.debug("result merged into local context");
    }

    /**
     *  (almost) abstract method: manages a remote call fault
     * 	
     *  @param context source context of the remote call
     *  @param componentName name of the target component
     *  @param operation name of the called operation
     *  @param emsg error message
     */
    public void handleFault(Context context, String componentName, String operation, FaultMessage emsg) {
    }
	
	private static class LogoutState extends Observable {
		
		private boolean logoutInProgress = false;
		private int waitForLogout = 0;
		private boolean sessionExpired = false;
		private Timer logoutTimeout = null;
		
		public synchronized void logout(Observer logoutObserver, TimerTask forceLogout) {
			logout(logoutObserver);
			logoutTimeout = new Timer(true);
			logoutTimeout.schedule(forceLogout, 1000L);
		}
		
		public synchronized void logout(Observer logoutObserver) {
			if (logoutObserver != null)
				addObserver(logoutObserver);
			if (!logoutInProgress) {
		        logoutInProgress = true;
			    waitForLogout = 1;
			}
		}
		
		public synchronized void checkWait() {
			if (logoutInProgress)
				waitForLogout++;
		}
		
		public synchronized boolean stillWaiting() {
			if (sessionExpired)
				return false;
			
			if (!logoutInProgress)
				return true;
			
			waitForLogout--;
			if (waitForLogout > 0)
				return true;
			
			return false;
		}
		
		public boolean isSessionExpired() {
			return sessionExpired;
		}
		
		public synchronized void loggedOut(TideRpcEvent event) {
			if (logoutTimeout != null) {
				logoutTimeout.cancel();
				logoutTimeout = null;
			}
			
			if (event != null) {
    			setChanged();
    			notifyObservers(event);
    			deleteObservers();
			}
			
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = false;
		}
		
		public synchronized void sessionExpired() {
			logoutInProgress = false;
			waitForLogout = 0;
			sessionExpired = true;
		}
	}
	
	
	public interface Status {

		public boolean isBusy();
		
		public void setBusy(boolean busy);
		
		public boolean isConnected();
		
		public void setConnected(boolean connected);
		
		public boolean isShowBusyCursor();
		
		public void setShowBusyCursor(boolean showBusyCursor);
	}

	
	public static class DefaultStatus implements Status {
		
		private boolean showBusyCursor = true;
		
		private boolean connected = false;
		private boolean busy = false;

		@Override
		public boolean isBusy() {
			return busy;
		}
		
		public void setBusy(boolean busy) {
			this.busy = busy;
		}

		@Override
		public boolean isConnected() {
			return connected;
		}

		public void setConnected(boolean connected) {
			this.connected = connected;
		}

		@Override
		public boolean isShowBusyCursor() {
			return showBusyCursor;
		}

		@Override
		public void setShowBusyCursor(boolean showBusyCursor) {
			this.showBusyCursor = showBusyCursor;			
		}		
	}
	
	
	private long defaultTimeToLive = -1;
	
	public void setDefaultTimeToLive(long timeToLive) {
	    defaultTimeToLive = timeToLive;
	    
	    if (channelFactory != null)
	        channelFactory.setDefaultTimeToLive(timeToLive);

        for (MessagingChannel messagingChannel : messagingChannelsByType.values())
            messagingChannel.setDefaultTimeToLive(timeToLive);

	    if (remotingChannel != null)
	        remotingChannel.setDefaultTimeToLive(timeToLive);
	}
	
	private List<TransportIOListener> transportIOListeners = new ArrayList<TransportIOListener>();
	private List<TransportExceptionListener> transportExceptionListeners = new ArrayList<TransportExceptionListener>();
	
	public void addListener(TransportIOListener listener) {
		transportIOListeners.add(listener);
	}
	public void removeListener(TransportIOListener listener) {
		transportIOListeners.remove(listener);
	}
	
	public void addListener(TransportExceptionListener listener) {
		transportExceptionListeners.add(listener);
	}
	public void removeListener(TransportExceptionListener listener) {
		transportExceptionListeners.remove(listener);
	}
	
	public interface TransportIOListener {		
		public void handleIO(boolean busy);
	}
	
	public interface TransportExceptionListener {		
		public void handleException(TransportException e);
	}
	
	public void notifyIOListeners(boolean busy) {
		for (TransportIOListener listener : transportIOListeners)
			listener.handleIO(busy);
	}
	
	public void notifyExceptionListeners(TransportException e) {
		for (TransportExceptionListener listener : transportExceptionListeners)
			listener.handleException(e);
	}
}
