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

package org.granite.gravity.servlet3;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AbstractGravityServlet;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.logging.Logger;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AsyncChannel extends AbstractChannel {

    private static final Logger log = Logger.getLogger(AsyncChannel.class);
    
    private final AtomicReference<AsyncContext> asyncContext = new AtomicReference<AsyncContext>();

	public AsyncChannel(Gravity gravity, String id, AsyncChannelFactory factory) {
        super(gravity, id, factory);
	}

	public void setAsyncContext(AsyncContext asyncContext) {
        if (log.isDebugEnabled())
            log.debug("Channel: %s got new asyncContext: %s", getId(), asyncContext);
        
        // Set this channel's async context.
        AsyncContext previousAsyncContext = this.asyncContext.getAndSet(asyncContext);

        // Normally, we should have only two cases here:
        //
        // 1) this.asyncContext == null && asyncContext != null -> new (re)connect message.
        // 2) this.asyncContext != null && asyncContext == null -> timeout.
        //
        // Not sure about what should be done if this.asyncContext != null && asyncContext != null, so
        // warn about this case and close this.asyncContext if it is not the same as the asyncContext
        // parameter.
        if (previousAsyncContext != null) {
        	if (asyncContext != null) {
        		log.warn(
        			"Got a new non null asyncContext %s while current asyncContext %s isn't null",
        			asyncContext, this.asyncContext.get()
        		);
        	}
        	if (previousAsyncContext != asyncContext) {
	        	try {
	        		previousAsyncContext.complete();
	        	}
	        	catch (Exception e) {
	        		log.debug(e, "Error while closing asyncContext");
	        	}
        	}
        }
        
        // Try to queue receiver if the new asyncContext isn't null.
        if (asyncContext != null)
        	queueReceiver();
	}
    
    @Override
	protected boolean hasAsyncHttpContext() {
    	return asyncContext.get() != null;
	}

	@Override
	protected AsyncHttpContext acquireAsyncHttpContext() {

		AsyncContext asyncContext = this.asyncContext.getAndSet(null);
		if (asyncContext == null)
			return null;

    	AsyncHttpContext context = null;

        try {
	        HttpServletRequest request = null;
	        HttpServletResponse response = null;
	        try {
	        	request = (HttpServletRequest)asyncContext.getRequest();
	        	response = (HttpServletResponse)asyncContext.getResponse();
	        } catch (Exception e) {
	        	log.warn("Illegal asyncContext: %s", asyncContext);
	        	return null;
	        }
	        if (request == null || response == null) {
	        	log.warn("Illegal asyncContext (request or response is null): %s", asyncContext);
	        	return null;
	        }
	
	        Message requestMessage = AbstractGravityServlet.getConnectMessage(request);
	        if (requestMessage == null) {
	        	log.warn("No request message while running channel: %s", getId());
	        	return null;
	        }
			
	        context = new AsyncHttpContext(request, response, requestMessage, asyncContext);
        }
        finally {
    		if (context == null) {
    			try {
    				asyncContext.complete();
    			}
    			catch (Exception e) {
    				log.debug(e, "Error while closing asyncContext: %s", asyncContext);
    			}
    		}
        }
        
        return context;
	}

	@Override
	protected void releaseAsyncHttpContext(AsyncHttpContext context) {
		try {
			if (context != null && context.getObject() != null)
				((AsyncContext)context.getObject()).complete();
		}
		catch (Exception e) {
			log.warn(e, "Could not release asyncContext for channel: %s", this);
		}
	}

	@Override
	public void destroy() {
		try {
			super.destroy();
		}
		finally {
			AsyncContext asyncContext = this.asyncContext.getAndSet(null);
			if (asyncContext != null) {
				try {
					asyncContext.complete();
				}
				catch (Exception e) {
					log.debug(e, "Could not close asyncContext: %s for channel: %s", asyncContext, this);
				}
			}
		}
	}
}
