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

import java.util.Map;

import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.messages.responses.FaultMessage;
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
public class IssueHandler<T> implements Runnable {
	
	private static final Logger log = Logger.getLogger(IssueHandler.class);
	
	private final ServerSession serverSession;
	private final Context sourceContext;
	private final String componentName;
	private final String operation;
	private final FaultEvent event;
	@SuppressWarnings("unused")
	private final Object info;
	private final TideResponder<T> tideResponder;
	private final ComponentListener<T> componentResponder;
	private boolean executed = false;
	
	
	public IssueHandler(ServerSession serverSession, Context sourceContext, String componentName, String operation, FaultEvent event, Object info, 
			TideResponder<T> tideResponder, ComponentListener<T> componentResponder) {
		this.serverSession = serverSession;
		this.sourceContext = sourceContext;
		this.componentName = componentName;
		this.operation = operation;
		this.event = event;
		this.info = info;
		this.tideResponder = tideResponder;
		this.componentResponder = componentResponder;
	}

	public void run() {
		if (executed)
			return;
		executed = true;
		
        log.error("fault %s", event.toString());
       
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
        
       	FaultMessage emsg = event.getMessage();
       	FaultMessage m = emsg;
        Map<String, Object> extendedData = emsg != null ? emsg.getExtended() : null;
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
        
        serverSession.handleFaultEvent(event, emsg);
        
        serverSession.handleFault(context, componentName, operation, emsg);
        
        boolean handled = false;
        Fault fault = new Fault(emsg.getCode(), emsg.getDescription(), emsg.getDetails());
        fault.setContent(event.getMessage());
        fault.setCause(event.getCause());
        
        TideFaultEvent faultEvent = new TideFaultEvent(context, serverSession, componentResponder, fault, extendedData);
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
            else if (exceptionHandlers != null && exceptionHandlers.length > 0 && event.getMessage() instanceof FaultMessage) {
                // Handle fault with default exception handler
                exceptionHandlers[0].handle(context, (FaultMessage)event.getMessage(), faultEvent);
            }
            else {
                log.error("Unknown fault: " + event.toString());
            }
        }
        
        if (!handled && !serverSession.isLogoutInProgress())
        	context.getEventBus().raiseEvent(context, ServerSession.CONTEXT_FAULT, event.getMessage());
        
        serverSession.tryLogout();
    }
}