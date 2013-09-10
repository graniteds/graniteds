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

package org.granite.tide.seam;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.granite.config.flex.Destination;
import org.granite.messaging.service.ExtendedServiceExceptionHandler;
import org.granite.messaging.service.ServiceException;
import org.granite.tide.TideMessage;
import org.jboss.seam.faces.FacesMessages;

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
public class SeamServiceExceptionHandler extends ExtendedServiceExceptionHandler {

    private static final long serialVersionUID = -1L;

    public SeamServiceExceptionHandler() {
    	super(true);
    }

    public SeamServiceExceptionHandler(boolean logException) {
    	super(logException);
    }

	@Override
    protected ServiceException getServiceException(Message request, Destination destination, String method, Throwable t) {
        ServiceException se = super.getServiceException(request, destination, method, t);
        
        // Add messages
        // Prepare for the messages. First step is convert the tasks to Seam FacesMessages
        FacesMessages.afterPhase();
        // Second step is add the Seam FacesMessages to JSF FacesContext Messages
        FacesMessages.instance().beforeRenderResponse();

        List<FacesMessage> facesMessages = FacesMessages.instance().getCurrentMessages();
        List<TideMessage> tideMessages = new ArrayList<TideMessage>(facesMessages.size());
        for (FacesMessage fm : facesMessages) {
            String severity = null;
            if (fm.getSeverity() == FacesMessage.SEVERITY_INFO)
                severity = TideMessage.INFO;
            else if (fm.getSeverity() == FacesMessage.SEVERITY_WARN)
                severity = TideMessage.WARNING;
            else if (fm.getSeverity() == FacesMessage.SEVERITY_ERROR)
                severity = TideMessage.ERROR;
            else if (fm.getSeverity() == FacesMessage.SEVERITY_FATAL)
                severity = TideMessage.FATAL;
            
            tideMessages.add(new TideMessage(severity, fm.getSummary(), fm.getDetail()));
        }
        se.getExtendedData().put("messages", tideMessages);
        return se;
    }
}
