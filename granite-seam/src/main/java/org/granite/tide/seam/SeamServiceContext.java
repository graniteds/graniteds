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

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.granite.tide.TideMessage;
import org.granite.tide.TideStatusMessages;
import org.granite.util.Reflections;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;


/**
 * @author William DRAI
 */
@Scope(ScopeType.SESSION)
@Name("org.granite.tide.seam.serviceContext")
@Install(precedence=FRAMEWORK)
@BypassInterceptors
public class SeamServiceContext extends AbstractSeamServiceContext {
    
    private static final long serialVersionUID = 1L;


    @Override
    protected void initTideMessages() {
        FacesMessages.instance();   // Forces initialization of TideMessages component
    }
    
    @Override
    protected void clearTideMessages() {
        if (FacesMessages.instance() instanceof TideMessages)
            ((TideMessages)FacesMessages.instance()).clearTideMessages();
        else {
        	try {
        		Reflections.invoke(FacesMessages.instance().getClass().getMethod("clear"), FacesMessages.instance());
        	}
        	catch (Exception e) {
        		log.error("Could not clear list of TideMessages", e);
        	}
        }
    }
    
    /**
     * Retrieve current messages
     * 
     * @return list of messages
     */
    @Override
    protected TideStatusMessages getTideMessages() {
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
        return new TideStatusMessages(tideMessages);
    }
}
