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

import java.util.Map;

import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.Event.Type;
import org.granite.client.messaging.events.FailureEvent;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.TimeoutEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ComponentListener;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.Fault;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.logging.Logger;

/**
 *  Implementation of fault handler
 *  
 * 	@author William DRAI
 */
public class FaultHandler<T> implements Runnable {
	
	private static final Logger log = Logger.getLogger(FaultHandler.class);
	
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


    public FaultHandler(ServerSession serverSession, String componentName, String operation) {
        this.serverSession = serverSession;
        this.sourceContext = null;
        this.componentName = componentName;
        this.operation = operation;
        this.event = null;
        this.info = null;
        this.tideResponder = null;
        this.componentListener = null;
    }

	public FaultHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation, Event event, Object info,
			TideResponder<T> tideResponder, ComponentListener<T> componentListener) {
		this.serverSession = serverSession;
		this.sourceContext = sourceContext;
		this.componentName = componentName;
		this.operation = operation;
		this.event = event;
		this.info = info;
		this.tideResponder = tideResponder;
		this.componentListener = componentListener;
	}

	public FaultHandler(ServerSession serverSession, Event event) {
		this.serverSession = serverSession;
		this.sourceContext = null;
		this.componentName = null;
		this.operation = null;
		this.event = event;
		this.info = null;
		this.tideResponder = null;
		this.componentListener = null;
	}

	public void run() {
		if (executed)
			return;
		executed = true;
		
        log.debug("fault %s", event.toString());
       
        // TODO: conversation contexts
//        var sessionId:String = faultEvent.message.headers[Tide.SESSION_ID_TAG];
//        var conversationId:String = null;
//        if (faultEvent.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG])
//            conversationId = faultEvent.message.headers[Tide.CONVERSATION_TAG];
//        var wasConversationCreated:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
//        var wasConversationEnded:Boolean = faultEvent.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
//        
//        var context:Context = _contextManager.retrieveContext(sourceContext, conversationId, wasConversationCreated, wasConversationEnded);
        
        Context context = sourceContext.getContextManager().retrieveContext(sourceContext, null, false, false);

        FaultMessage emsg = null;
        Map<String, Object> extendedData = null;
        if (event instanceof FaultEvent) {
	       	emsg = ((FaultEvent)event).getMessage();
	       	FaultMessage m = emsg;
	       	extendedData = emsg != null ? emsg.getExtended() : null;
	        do {
	            if (m != null && m.getCode() != null && m.isSecurityFault()) {
	                emsg = m;
	                extendedData = emsg != null ? emsg.getExtended() : null;
	                break;
	            }
	            // TODO: check WTF we should do here
	            if (m != null && m.getCause() instanceof FaultEvent)
	                m = (FaultMessage)((FaultEvent)m.getCause()).getCause();
	            else if (m.getCause() instanceof FaultMessage)
	                m = (FaultMessage)m.getCause();
	            else
	            	m = null;
	        }
	        while (m != null);
	        
	        serverSession.onFaultEvent((FaultEvent)event, emsg);
        }
        else
	        serverSession.onIssueEvent((IssueEvent)event);
        
        boolean handled = handleFault(context, emsg, extendedData);
        
        if (!handled && !serverSession.isLogoutInProgress())
        	context.getEventBus().raiseEvent(context, ServerSession.CONTEXT_FAULT, event instanceof FaultEvent ? ((FaultEvent)event).getMessage() : null);
        
        serverSession.tryLogout();
    }


    public boolean handleFault(Context context, FaultMessage emsg, Map<String, Object> extendedData) {
        
        boolean handled = false;
        
        Fault fault = null;
        if (event instanceof FaultEvent) {
        	fault = new Fault(emsg.getCode(), emsg.getDescription(), emsg.getDetails());
	        fault.setContent(((FaultEvent)event).getMessage());
	        fault.setCause(((FaultEvent)event).getCause());
        }
        else if (event != null && event.getType() == Type.FAILURE) {
        	fault = new Fault(Code.CLIENT_CALL_FAILED, null, ((FailureEvent)event).getCause() != null ? ((FailureEvent)event).getCause().getMessage() : null);
        	fault.setCause(((FailureEvent)event).getCause());
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_FAILED, null, null, null, null);
        }
        else if (event != null && event.getType() == Type.TIMEOUT) {
        	fault = new Fault(Code.CLIENT_CALL_TIMED_OUT, null, String.valueOf(((TimeoutEvent)event).getTime()));
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_TIMED_OUT, null, null, null, null);
        }
        else if (event != null && event.getType() == Type.CANCELLED) {
        	fault = new Fault(Code.CLIENT_CALL_CANCELLED, null, null);
        	emsg = new FaultMessage(null, null, Code.CLIENT_CALL_CANCELLED, null, null, null, null);
        }
        else {
        	fault = new Fault(Code.UNKNOWN, null, null);
        	emsg = new FaultMessage(null, null, Code.UNKNOWN, null, null, null, null);
        }
        
        TideFaultEvent faultEvent = new TideFaultEvent(context, serverSession, componentListener, fault, extendedData);
        if (tideResponder != null) {
            tideResponder.fault(faultEvent);
            if (faultEvent.isDefaultPrevented())
                handled = true;
        }
        
        if (!handled) {
            ExceptionHandler[] exceptionHandlers = context.getContextManager().getContext(null).allByType(ExceptionHandler.class);
            if (exceptionHandlers != null && emsg != null) {
                // Lookup for a suitable exception handler
                for (ExceptionHandler handler : exceptionHandlers) {
                    if (handler.accepts(emsg)) {
                        handler.handle(context, emsg, faultEvent);
                        handled = true;
                        break;
                    }
                }
                if (!handled)
                    log.error("Unhandled fault: " + emsg.getCode() + ": " + emsg.getDescription());
            }
            else if (exceptionHandlers != null && exceptionHandlers.length > 0 && event instanceof FaultEvent) {
                // Handle fault with default exception handler
                exceptionHandlers[0].handle(context, ((FaultEvent)event).getMessage(), faultEvent);
            }
            else {
                log.error("Unknown fault: " + event.toString());
            }
        }
        
        return handled;
    }
}