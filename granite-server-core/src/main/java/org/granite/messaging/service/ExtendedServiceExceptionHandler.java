/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;

import flex.messaging.messages.Message;


/**
 * @author Venkat DANDA
 * @author Cameron INGRAM
 *
 * Update services-config.xml to use the seam service exception handler
 * <factory id="tideSeamFactory" class="org.granite.tide.seam.SeamServiceFactory" >
 *	<properties>
 *		<service-exception-handler>org.granite.tide.seam.SeamServiceExceptionHandler</service-exception-handler>
 *	</properties>
 * </factory>
 */
public class ExtendedServiceExceptionHandler extends DefaultServiceExceptionHandler {

    private static final long serialVersionUID = -1L;
    private static final Logger log = Logger.getLogger(ExtendedServiceExceptionHandler.class);

    public static final Class<?> JAVAX_EJB_EXCEPTION;
    static {
        Class<?> exception = null;
        try {
            exception = Thread.currentThread().getContextClassLoader().loadClass("javax.ejb.EJBException");
        }
        catch (Exception e) {
        }
        JAVAX_EJB_EXCEPTION = exception;
    }

    public ExtendedServiceExceptionHandler() {
    	this(true);
    }

    public ExtendedServiceExceptionHandler(boolean logException) {
    	super(logException);
    }

    @Override
    protected ServiceException getServiceException(Message request, Destination destination, String method, Throwable t) {
        if (t == null)
            throw new NullPointerException("Parameter t cannot be null");

        Map<String, Object> extendedData = new HashMap<String, Object>();

        if (t instanceof ServiceException) {
            ((ServiceException)t).getExtendedData().putAll(extendedData);
            return (ServiceException)t;
        }

        List<Throwable> causes = new ArrayList<Throwable>();
        for (Throwable cause = t; cause != null; cause = getCause(cause))
            causes.add(cause);

        String detail = "\n" +
            "- destination: " + (destination != null ? destination.getId() : "") + "\n" +
            "- method: " + method + "\n" +
            "- exception: " + t.toString() + "\n";

        for (int i = causes.size()-1; i >= 0; i--) {
            Throwable cause = causes.get(i);
	        for (ExceptionConverter ec : ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getExceptionConverters()) {
                if (ec.accepts(cause, t))
                    return ec.convert(cause, detail, extendedData);
            }
        }
        
        if (getLogException())
    		log.error(t, "Could not process remoting message: %s", request);

        // Default exception handler
        ServiceException se = new ServiceException(t.getClass().getSimpleName() + ".Call.Failed", t.getMessage(), detail, t);
        se.getExtendedData().putAll(extendedData);
        return se;
    }
    
    
    public static Throwable getCause(Throwable t) {
        Throwable cause = null;
        try {
            if (JAVAX_EJB_EXCEPTION != null && JAVAX_EJB_EXCEPTION.isInstance(t)) {
                Method m = JAVAX_EJB_EXCEPTION.getMethod("getCausedByException");
                cause = (Throwable)m.invoke(t);
            }
            else if (t instanceof ServletException)
                cause = ((ServletException)t).getRootCause();
            else
                cause = t.getCause();
        }
        catch (Exception x) {
            return null;
        }
        return cause == t ? null : cause;
    }
}
