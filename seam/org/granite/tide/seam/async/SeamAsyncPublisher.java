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

package org.granite.tide.seam.async;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.tide.async.AsyncPublisher;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.ServletLifecycle;

import flex.messaging.messages.AsyncMessage;


/**
 * Async publisher using Gravity to send messages to the client
 * 
 * @author William DRAI
 */
@Name("org.granite.tide.seam.async.publisher")
@Install(precedence=FRAMEWORK, classDependencies={"org.granite.gravity.Gravity"})
@Scope(ScopeType.STATELESS)
@BypassInterceptors
@AutoCreate
public class SeamAsyncPublisher implements AsyncPublisher {
    
    public static final String DESTINATION_NAME = "seamAsync";
    
    private Gravity getGravity() {
        return GravityManager.getGravity(ServletLifecycle.getServletContext());
    }

    public void initThread() {
    	Gravity gravity = getGravity();
    	if (gravity == null)
    		throw new RuntimeException("Gravity service not configured, it is required for asynchronous event publishing");
    	
    	gravity.initThread();
    }
    
    public void publishMessage(String sessionId, Object body) {
    	AsyncMessage message = new AsyncMessage();
        message.setHeader(AsyncMessage.SUBTOPIC_HEADER, "tide.events." + sessionId);
        message.setDestination(DESTINATION_NAME);
        message.setBody(body);
        
        getGravity().publishMessage(message);
    }
}
