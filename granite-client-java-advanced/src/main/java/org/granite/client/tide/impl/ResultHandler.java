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
package org.granite.client.tide.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.IncomingMessageEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideMergeResponder;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.tide.invocation.InvocationResult;

/**
 * @author William DRAI
 */
public class ResultHandler<T> implements Runnable {

	private final ServerSession serverSession;
	private final Context sourceContext;
	private final String componentName;
	private final String operation;
	private final Event event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener<T> componentListener;
	private boolean executed = false;
	
	
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
        
        serverSession.handleResultEvent(event);
        
        serverSession.handleResult(context, componentName, operation, invocationResult, result, 
            tideResponder instanceof TideMergeResponder<?> ? ((TideMergeResponder<T>)tideResponder).getMergeResultWith() : null);
        if (invocationResult != null)
            result = invocationResult.getResult();
        
        @SuppressWarnings("unused")
		boolean handled = false;
        if (tideResponder != null) {
            TideResultEvent<T> resultEvent = new TideResultEvent<T>(context, serverSession, componentListener, (T)result);
            tideResponder.result(resultEvent);
            if (resultEvent.isDefaultPrevented())
                handled = true;
        }
        
        componentListener.setResult((T)result);
        
//	        context.clearData();
//	        
//	        // Should be after event result handling and responder: previous could trigger other remote calls
//	        if (context.isFinished())
//	            context.scheduleDestroy();
//	        
//	        if (!handled && !serverSession.isLogoutInProgress())
//	            context.raiseEvent(Tide.CONTEXT_RESULT, result);
        
        serverSession.tryLogout();
    }
}