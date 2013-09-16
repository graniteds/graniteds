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
package org.granite.seam;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.granite.logging.Logger;
import org.granite.messaging.service.DefaultServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.service.security.SecurityServiceException;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;

/**
 * @author Venkat DANDA
 * @author Cameron INGRAM
 *
 * Update services-config.xml to use the seam service exception handler
 * <factory id="seamFactory" class="org.granite.seam.SeamServiceFactory" >
 *	<properties>
 *		<service-exception-handler>org.granite.seam.SeamServiceExceptionHandler</service-exception-handler>
 *	</properties>
 * </factory>
 */
public class SeamServiceExceptionHandler extends DefaultServiceExceptionHandler {

    private static final long serialVersionUID = -929771583032427716L;
	private static final Logger log = Logger.getLogger(SeamServiceExceptionHandler.class);

    public SeamServiceExceptionHandler() {
    	this(true);
    }

    public SeamServiceExceptionHandler(boolean logException) {
		super(logException);
	}

    @Override
    public ServiceException handleInvocationException(ServiceInvocationContext context, Throwable t) {
    	try {
            while ((!(t instanceof InvalidStateException) && (t.getCause() != null)))
                t = t.getCause();

            if (t instanceof InvalidStateException) {
                InvalidStateException ise = (InvalidStateException)t;
                InvalidValue[] temp = ise.getInvalidValues();
                if (temp != null && temp.length > 0) {
					for (int i = 0; i < temp.length; i++)
						FacesContext.getCurrentInstance().addMessage(temp[i].getPropertyName(), new FacesMessage(temp[i].getMessage()));
				}
            }
        }
        catch(Exception e) {
            log.error(e, "Could not add FacesMessage for exception: %s", t);
        }

        if (t instanceof SecurityServiceException)
            log.debug(t, "Could not process remoting message: %s", context.getMessage());
        else
            log.error(t, "Could not process remoting message: %s", context.getMessage());

        
        return getServiceException(context, t);
    }

}
