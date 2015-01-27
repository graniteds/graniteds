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
package org.granite.gravity.weblogic;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.GravityInternal;
import org.granite.logging.Logger;

import weblogic.servlet.http.AbstractAsyncServlet;
import weblogic.servlet.http.RequestResponseKey;

/**
 * @author Franck WOLFF
 */
public class WebLogicChannel extends AbstractChannel {

	private static final Logger log = Logger.getLogger(WebLogicChannel.class);
	
	// For WebLogic 9.1 compatibility.
	private static Method isValid = null;
	static {
		try {
			isValid = RequestResponseKey.class.getDeclaredMethod("isValid");
		}
		catch (Throwable t) {
		}
	}
    
    private final AtomicReference<RequestResponseKey> key = new AtomicReference<RequestResponseKey>();

    
	public WebLogicChannel(GravityInternal gravity, String id, WebLogicChannelFactory factory, String clientType) {
		super(gravity, id, factory, clientType);
	}
	
	public void setRequestResponseKey(RequestResponseKey key) {
        if (log.isDebugEnabled())
            log.debug("Channel: %s got new asyncContext: %s", getId(), key);
        
        // Set this channel's request/response key.
        RequestResponseKey previousKey = this.key.getAndSet(key);
        
        // Normally, we should have only two cases here:
        //
        // 1) this.key == null && key != null -> new (re)connect message.
        // 2) this.key != null && key == null -> timeout.
        //
        // Not sure about what should be done if this.key != null && key != null, so
        // warn about this case and close this.key if it is not the same as the key
        // parameter.
        if (previousKey != null) {
        	if (key != null) {
        		log.warn(
        			"Got a new non null key %s while current key %s isn't null",
        			key, this.key.get()
        		);
        	}
        	if (previousKey != key) {
	        	try {
	        		previousKey.getResponse().getOutputStream().close();
	        	}
	        	catch (Exception e) {
	        		log.debug(e, "Error while closing key");
	        	}
        	}
        }
        
        // Try to queue receiver if the new asyncContext isn't null.
        if (key != null)
        	queueReceiver();
	}

	@Override
	protected boolean hasAsyncHttpContext() {
		return key.get() != null;
	}

	@Override
	protected AsyncHttpContext acquireAsyncHttpContext() {
		RequestResponseKey key = this.key.getAndSet(null);
		if (key == null || !isValid(key))
			return null;

		try {
			AbstractAsyncServlet.notify(key, this);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return null;
	}

	@Override
	protected void releaseAsyncHttpContext(AsyncHttpContext context) {
		// This method shouldn't be called in a WebLogic environment, anyway...
		try {
			if (context != null)
				context.getResponse().getOutputStream().close();
		}
		catch (Exception e) {
			log.warn(e, "Could not release asyncHttpContext for channel: %s", this);
		}
	}

	@Override
	public void destroy(boolean timeout) {
		try {
			super.destroy(timeout);
		}
		finally {
			close(timeout);
		}
	}
	
	public void close(boolean timeout) {
		RequestResponseKey key = this.key.getAndSet(null);
		if (key != null) {
			try {
				key.getResponse().getOutputStream().close();
			}
			catch (Exception e) {
				log.debug(e, "Could not close key: %s for channel: %s", key, this);
			}
		}
	}
	
	private static boolean isValid(RequestResponseKey key) {
		if (isValid != null) try {
			return (Boolean)isValid.invoke(key);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
}
