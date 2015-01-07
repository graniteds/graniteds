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
package org.granite.tide.invocation;

import java.util.List;
import java.util.Map;

import org.granite.tide.IInvocationResult;
import org.granite.tide.TideMessage;


/**
 * @author William DRAI
 */
public class InvocationResult implements IInvocationResult {

    private static final long serialVersionUID = 1L;
    
    
    private Object result;
    private int scope;
    private boolean restrict;
    private boolean merge = true;
    private Object[][] updates;
    private List<ContextUpdate> results;
    private List<ContextEvent> events;
    private List<TideMessage> messages;
    private Map<String, List<TideMessage>> keyedMessages;
    
    
    public InvocationResult() {
    }
    
    public InvocationResult(Object result) {
        this.result = result;
    }
    
    public InvocationResult(Object result, List<ContextUpdate> results) {
        this.result = result;
        this.results = results;
    }

    public Object getResult() {
        return result;
    }
    public void setResult(Object result) {
        this.result = result;
    }
    
    public int getScope() {
        return scope;
    }
    public void setScope(int scope) {
        this.scope = scope;
    }
    
    public boolean getRestrict() {
        return restrict;
    }
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }
    
    public boolean getMerge() {
        return merge;
    }
    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public Object[][] getUpdates() {
        return updates;
    }
    public void setUpdates(Object[][] updates) {
    	if (updates == null) {
    		this.updates = null;
    		return;
    	}
    	
        this.updates = new Object[updates.length][];
        for (int i = 0; i < updates.length; i++)
        	this.updates[i] = new Object[] { updates[i][0], updates[i][1] };
    }
    
    public List<ContextUpdate> getResults() {
        return results;
    }
    public void setResults(List<ContextUpdate> results) {
        this.results = results;
    }
    
    public List<ContextEvent> getEvents() {
        return events;
    }
    public void setEvents(List<ContextEvent> events) {
        this.events = events;
    }

    public List<TideMessage> getMessages() {
        return messages;
    }
    public void setMessages(List<TideMessage> messages) {
        this.messages = messages;
    }

    public Map<String, List<TideMessage>> getKeyedMessages() {
        return keyedMessages;
    }
    public void setKeyedMessages(Map<String, List<TideMessage>> keyedMessages) {
        this.keyedMessages = keyedMessages;
    }
    
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(getClass().getName()).append(" ");
    	if (scope == 1)
    		sb.append("(SESSION) ");
    	else if (scope == 2)
    		sb.append("(CONVERSATION) ");
    	if (restrict)
    		sb.append("(restricted) ");
    	sb.append("{\n");
    	sb.append("\tresult: ").append(result != null ? result : "(null)");
    	if (results != null) {
    		sb.append("\tresults: [");
    		for (Object result : results)
    			sb.append(result != null ? result.toString() : "(null)").append(" ");
    		sb.append("]\n");
    	}
    	if (updates != null) {
    		sb.append("\tupdates: [");
    		for (Object[] update : updates)
    			sb.append(update[0]).append(":").append(update[1]).append(" ");
    		sb.append("]\n");
    	}
    	if (events != null) {
    		sb.append("\tevents: [");
    		for (ContextEvent event : events)
    			sb.append(event).append(" ");
    		sb.append("]\n");
    	}
    	sb.append("}");
    	return sb.toString();
    }
}
