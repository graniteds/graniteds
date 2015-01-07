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
package org.granite.gravity.generic;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.MessageReceivingException;
import org.granite.logging.Logger;

import flex.messaging.messages.AsyncMessage;

/**
 * @author William DRAI
 */
public class GenericChannel extends AbstractChannel {

    private static final Logger log = Logger.getLogger(GenericChannel.class);

    private WaitingContinuation continuation = null;

    public GenericChannel(GravityInternal gravity, String id, GenericChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
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
    
    public void close() {
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
	public void destroy(boolean timeout) {
		try {
			super.destroy(timeout);
		}
		finally {
			synchronized (this) {
				close();
			}
		}
	}
}
