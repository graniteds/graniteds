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

package org.granite.gravity.generic;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.MessageReceivingException;
import org.granite.logging.Logger;

import flex.messaging.messages.AsyncMessage;

/**
 * @author William DRAI
 */
public class GenericChannel extends AbstractChannel {

    private static final Logger log = Logger.getLogger(GenericChannel.class);

    private WaitingContinuation continuation = null;

    public GenericChannel(Gravity gravity, String id, GenericChannelFactory factory) {
    	super(gravity, id, factory);
    }

    public void setContinuation(WaitingContinuation continuation) {
        try {
            if (this.continuation != null && this.continuation.isPending()) {
                log.debug("Set pending continuation for client: %s", getId());
                this.continuation.resume();
            }
        }
        finally {
            this.continuation = continuation;
        }
    }
    
    public void reset() {
    	try {
            if (this.continuation != null)
                this.continuation.reset();
        }
    	finally {
            this.continuation = null;
    	}
    }
    

    public void resume() {
    	try {
            if (this.continuation != null) {
                log.debug("Resume pending continuation for client: %s", getId());
                this.continuation.resume();
            }
        }
    	finally {
            this.continuation = null;
    	}
    }
    
    @Override
	public void receive(AsyncMessage message) throws MessageReceivingException {
		if (message == null)
			throw new NullPointerException("message cannot be null");
		
		receivedQueueLock.lock();
		try {
			receivedQueue.add(message);
		}
		catch (Exception e) {
			throw new MessageReceivingException(message, "Could not queue message", e);
		}
		finally {
			receivedQueueLock.unlock();
		}
		
		synchronized (this) {
			resume();
		}
	}

	@Override
	protected boolean hasAsyncHttpContext() {
		return false;
	}

	@Override
	protected void releaseAsyncHttpContext(AsyncHttpContext context) {
	}

	@Override
	protected AsyncHttpContext acquireAsyncHttpContext() {
    	return null;
    }

    public boolean isLocal() {
        return true;
    }

	@Override
	public void destroy() {
		try {
			super.destroy();
		}
		finally {
			synchronized (this) {
				reset();
			}
		}
	}
}
