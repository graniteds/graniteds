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

package org.granite.tide.seam21;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.tide.TideMessage;
import org.granite.tide.seam.AbstractSeamServiceContext;
import org.granite.util.Reflections;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;


/**
 * @author William DRAI
 */
@Scope(ScopeType.SESSION)
@Name("org.granite.tide.seam.serviceContext")
@Install(precedence=FRAMEWORK+1)
@BypassInterceptors
public class Seam21ServiceContext extends AbstractSeamServiceContext {
    
    private static final long serialVersionUID = 1L;


    @Override
    protected void initTideMessages() {
        StatusMessages.instance();
    }
    
    @Override
    protected void clearTideMessages() {
        StatusMessages statusMessages = StatusMessages.instance();
        if (statusMessages instanceof TideStatusMessages)
            ((TideStatusMessages)statusMessages).clear();
        else {
        	try {
        		Reflections.invoke(statusMessages.getClass().getMethod("clear"), statusMessages);
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
    protected org.granite.tide.TideStatusMessages getTideMessages() {
        StatusMessages statusMessages = StatusMessages.instance();
        if (statusMessages == null)
            return org.granite.tide.TideStatusMessages.EMPTY_STATUS_MESSAGES;
        
        try {
            // Execute and get the messages (once again reflection hack to use protected methods) 
            Method m = StatusMessages.class.getDeclaredMethod("doRunTasks");
            m.setAccessible(true);
            m.invoke(statusMessages);
            
            Method m2 = StatusMessages.class.getDeclaredMethod("getMessages");
            m2.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<StatusMessage> messages = (List<StatusMessage>)m2.invoke(statusMessages);
            List<TideMessage> tideMessages = new ArrayList<TideMessage>(messages.size());
            
            log.debug("Found Messages: %b", !messages.isEmpty());
            for (StatusMessage msg : messages) {
                String severity = null;
                if (msg.getSeverity() == StatusMessage.Severity.INFO)
                    severity = TideMessage.INFO;
                else if (msg.getSeverity() == StatusMessage.Severity.WARN)
                    severity = TideMessage.WARNING;
                else if (msg.getSeverity() == StatusMessage.Severity.ERROR)
                    severity = TideMessage.ERROR;
                else if (msg.getSeverity() == StatusMessage.Severity.FATAL)
                    severity = TideMessage.FATAL;
                
                tideMessages.add(new TideMessage(severity, msg.getSummary(), msg.getDetail()));
            }
            
            Method m3 = StatusMessages.class.getDeclaredMethod("getKeyedMessages");
            m3.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, List<StatusMessage>> keyedMessages = (Map<String, List<StatusMessage>>)m3.invoke(statusMessages);
            Map<String, List<TideMessage>> tideKeyedMessages = new HashMap<String, List<TideMessage>>(keyedMessages.size());
            for (Map.Entry<String, List<StatusMessage>> me : keyedMessages.entrySet()) {
            	List<TideMessage> tmsgs = new ArrayList<TideMessage>(me.getValue().size());
	            for (StatusMessage msg : me.getValue()) {
	                String severity = null;
	                if (msg.getSeverity() == StatusMessage.Severity.INFO)
	                    severity = TideMessage.INFO;
	                else if (msg.getSeverity() == StatusMessage.Severity.WARN)
	                    severity = TideMessage.WARNING;
	                else if (msg.getSeverity() == StatusMessage.Severity.ERROR)
	                    severity = TideMessage.ERROR;
	                else if (msg.getSeverity() == StatusMessage.Severity.FATAL)
	                    severity = TideMessage.FATAL;
	                
	                tmsgs.add(new TideMessage(severity, msg.getSummary(), msg.getDetail()));
	            }
	            tideKeyedMessages.put(me.getKey(), tmsgs);
            }
            
            return new org.granite.tide.TideStatusMessages(tideMessages, tideKeyedMessages);
        }
        catch (Exception e) {
            log.error("Could not get status messages", e);
        }
        
        return org.granite.tide.TideStatusMessages.EMPTY_STATUS_MESSAGES;
    }
}
