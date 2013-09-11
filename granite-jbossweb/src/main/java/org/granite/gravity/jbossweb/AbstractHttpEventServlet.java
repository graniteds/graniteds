/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.gravity.jbossweb;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.gravity.AbstractGravityServlet;
import org.granite.logging.Logger;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractHttpEventServlet extends AbstractGravityServlet implements HttpEventServlet {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(AbstractHttpEventServlet.class);
    
    private boolean longPollingTimeoutSupported = true;

    ///////////////////////////////////////////////////////////////////////////
    // Abstract methods.

	public abstract CometIO createCometIO();
	
    public abstract boolean handleRequest(HttpEvent event, InputStream content)
        throws IOException, ServletException;

    public abstract boolean handleEnd(HttpEvent event)
        throws IOException, ServletException;

    public abstract boolean handleError(HttpEvent event)
        throws IOException, ServletException;

    ///////////////////////////////////////////////////////////////////////////
    // CometProcessor implementation.

    public void event(HttpEvent event) throws IOException, ServletException {
    	
    	// make sure we've got a valid CometEvent (should never happen)
    	if (!EventUtil.isValid(event)) {
    		log.error("JBossWeb sent an invalid HttpEvent: %s", event.getType());
    		return;
    	}

    	if (log.isDebugEnabled()) {
	    	log.debug(
	            "%s: %s/%s",
	            event.getType(),
	            event.getHttpServletRequest(), event.getHttpServletResponse()
	        );
    	}

        if (event.getType() == HttpEvent.EventType.BEGIN)
            begin(event);
        else if (event.getType() == HttpEvent.EventType.READ)
            read(event);
        else if (event.getType() == HttpEvent.EventType.END)
            end(event);
        else if (event.getType() == HttpEvent.EventType.ERROR 
                || event.getType() == HttpEvent.EventType.EOF 
                || event.getType() == HttpEvent.EventType.TIMEOUT)
            error(event);
        else
            throw new ServletException("Unknown HttpEvent type: " + event.getType());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Comet events processing.

    protected void begin(HttpEvent event) throws IOException, ServletException {
        boolean close = true;
        try {
	    	// Event timeout isn't supported with APR connectors...
	        if (longPollingTimeoutSupported) {
	        	try {
	        		event.setTimeout((int)getLongPollingTimeout());
	        	}
	        	catch (Exception e) {
	        		longPollingTimeoutSupported = false;
	        	}
	        }

	        HttpServletRequest request = event.getHttpServletRequest();
            CometIO io = createCometIO();
        	io.readFully(request.getInputStream());
        	
        	close = handleRequest(event, io.getInputStream());
        }
        finally {
        	if (close) {
            	try {
    	        	event.close();
    	        } catch (Exception e) {
    	        	log.debug(e, "Could not close event: %s", EventUtil.toString(event));
    	        }
        	}
        }
    }

	protected void read(HttpEvent event) {
		// This implementation doesn't use asynchronous reads.
		throw new RuntimeException("Unsupported operation");
    }
    
	protected void end(HttpEvent event) throws IOException, ServletException {
		boolean close = true;
		try {
			close = handleEnd(event);
		}
		finally {
			if (close) {
		        try {
		        	event.close();
		        } catch (Exception e) {
		        	log.debug(e, "Could not close event: %s", EventUtil.toString(event));
		        }
			}
        }
    }
    
    protected void error(HttpEvent event) throws IOException, ServletException {
        boolean close = true;
        try {
	        close = handleError(event);
        }
        finally {
        	if (close) {
		        try {
		        	event.close();
		        } catch (Exception e) {
		        	log.debug(e, "Could not close event: %s", EventUtil.toString(event));
		        }
        	}
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility.

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        throw new ServletException("Not in a valid Comet configuration (use an APR or NIO connector)");
    }
}
