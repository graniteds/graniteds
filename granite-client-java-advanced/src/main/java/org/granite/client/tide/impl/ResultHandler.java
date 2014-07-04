/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.impl.ChangeEntityRef;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideMergeResponder;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.invocation.InvocationResult;

/**
 * @author William DRAI
 */
public class ResultHandler<T> implements Runnable {

	private final ServerSession serverSession;
	private final Context sourceContext;
	@SuppressWarnings("unused")
	private final String componentName;
	@SuppressWarnings("unused")
	private final String operation;
	private final Event event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener<T> componentListener;
	private boolean executed = false;


    public ResultHandler(ServerSession serverSession, String componentName, String operation) {
        this.serverSession = serverSession;
        this.sourceContext = null;
        this.componentName = componentName;
        this.operation = operation;
        this.event = null;
        this.info = null;
        this.tideResponder = null;
        this.componentListener = null;
    }

	public ResultHandler(ServerSession serverSession, Event event) {
		this.serverSession = serverSession;
		this.sourceContext = null;
		this.componentName = null;
		this.operation = null;
		this.event = event;
		this.info = null;
		this.tideResponder = null;
		this.componentListener = null;
	}

	public ResultHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation,
			Event event, Object info, TideResponder<T> tideResponder, ComponentListener<T> componentListener) {
		this.serverSession = serverSession;
		this.sourceContext = sourceContext;
		this.componentName = componentName;
		this.operation = operation;
		this.event = event;
		this.info = info;
		this.tideResponder = tideResponder;
		this.componentListener = componentListener;
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		if (executed)
			return;
		executed = true;
        InvocationResult invocationResult = null;
        Object result = null; 
        if (event instanceof ResultEvent)
            result = ((ResultEvent)event).getResult();
        else if (event instanceof IncomingMessageEvent<?>)
            result = ((IncomingMessageEvent<?>)event).getMessage();
        
        if (result instanceof InvocationResult) {
            invocationResult = (InvocationResult)result;
            result = invocationResult.getResult();
        }
        
        if (tideResponder != null) {
	        for (Type type : tideResponder.getClass().getGenericInterfaces()) {
	        	if (type instanceof ParameterizedType && ((ParameterizedType)type).getRawType().equals(TideResponder.class)) {
	        		Type expectedReturnType = ((ParameterizedType)type).getActualTypeArguments()[0];
	        		result = serverSession.convert(result, expectedReturnType);
	        		if (invocationResult != null)
	        			invocationResult.setResult(result);
	        		break;
	        	}
	        }
        }
        
//        var conversationId:String = null;
//        if (event.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG])
//            conversationId = event.message.headers[Tide.CONVERSATION_TAG];
//        var wasConversationCreated:Boolean = event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
//        var wasConversationEnded:Boolean = event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
//        
//        var context:Context = _contextManager.retrieveContext(sourceContext, conversationId, wasConversationCreated, wasConversationEnded);	        
        
        Context context = sourceContext.getContextManager().retrieveContext(sourceContext, null, false, false); // conversationId, wasConversationCreated, wasConversationEnded);
        
        serverSession.onResultEvent(event);
        
        boolean handled = handleResult(context, invocationResult, result);
		
        if (invocationResult != null)
            result = invocationResult.getResult();
        
        componentListener.setResult((T)result);
        
//	        context.clearData();
//	        
//	        // Should be after event result handling and responder: previous could trigger other remote calls
//	        if (context.isFinished())
//	            context.scheduleDestroy();
//	        
        if (!handled && !serverSession.isLogoutInProgress())
            context.getEventBus().raiseEvent(context, ServerSession.CONTEXT_RESULT, event instanceof ResultEvent ? ((ResultEvent)event).getMessage() : null);

        serverSession.tryLogout();
    }


    private static final Logger log = Logger.getLogger(ResultHandler.class);

    public boolean handleResult(Context context, InvocationResult invocationResult, Object result) {
        log.debug("result %s", result);

        List<EntityManager.Update> updates = null;
        EntityManager entityManager = context.getEntityManager();

        try {
            // Clear flash context variable for Grails/Spring MVC
            context.remove("flash");

            MergeContext mergeContext = entityManager.initMerge(serverSession);

            boolean mergeExternal = true;
            if (invocationResult != null) {
                mergeExternal = invocationResult.getMerge();

                if (invocationResult.getUpdates() != null && invocationResult.getUpdates().length > 0) {
                    updates = new ArrayList<EntityManager.Update>(invocationResult.getUpdates().length);
                    for (Object[] update : invocationResult.getUpdates()) {
			        	String updateType = update[0].toString().toUpperCase();
        				Object entity = update[1];
        				if (UpdateKind.REFRESH.toString().toLowerCase().equals(updateType) && entity instanceof String)
        					entity = serverSession.getAliasRegistry().getAliasForType((String)entity);
        				else if (entity instanceof ChangeRef)
        					entity = new ChangeEntityRef(entity, serverSession.getAliasRegistry());
                    	
                        updates.add(EntityManager.Update.forUpdate(updateType, entity));
                    }
                    
                    entityManager.handleUpdates(mergeContext, null, updates);
                }
            }

            // Merges final result object
            if (result != null) {
                if (mergeExternal) {
                    Object mergeWith = tideResponder instanceof TideMergeResponder<?> ? ((TideMergeResponder<T>)tideResponder).getMergeResultWith() : null;
                    result = entityManager.mergeExternal(mergeContext, result, mergeWith, null, null, false);
                }
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
        
		if (invocationResult != null)
            result = invocationResult.getResult();
        
		boolean handled = false;
        if (tideResponder != null) {
            @SuppressWarnings("unchecked")
			TideResultEvent<T> resultEvent = new TideResultEvent<T>(context, serverSession, componentListener, (T)result);
            tideResponder.result(resultEvent);
            if (resultEvent.isDefaultPrevented())
                handled = true;
        }
        
        return handled;
    }
}