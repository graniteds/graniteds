/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.messaging.service;

import org.granite.config.flex.Destination;

import flex.messaging.messages.Message;

/**
 * @author Marcelo SCHROEDER
 */
public abstract class AbstractServiceExceptionHandler implements ServiceExceptionHandler {

	private static final long serialVersionUID = 1L;
	
	private final boolean logException;

	public AbstractServiceExceptionHandler(boolean logException) {
		this.logException = logException;
	}

	protected boolean getLogException() {
		return logException;
	}
	
	protected ServiceException getServiceException(ServiceInvocationContext context, Throwable e) {
    	String method = (context.getMethod() != null ? context.getMethod().toString() : "null");
        return getServiceException(context.getMessage(), context.getDestination(), method, e);
    }

    protected ServiceException getServiceException(Message request, Destination destination, String method, Throwable e) {

        if (e instanceof ServiceException)
            return (ServiceException)e;

        String detail = "\n" +
            "- destination: " + (destination != null ? destination.getId() : "") + "\n" +
            "- method: " + method + "\n" +
            "- exception: " + e.toString() + "\n";

        return new ServiceException(getClass().getSimpleName() + ".Call.Failed", e.getMessage(), detail, e);
    }
}
