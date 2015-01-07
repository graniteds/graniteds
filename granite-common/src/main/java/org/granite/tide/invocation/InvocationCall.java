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

import org.granite.tide.IInvocationCall;


/**
 * @author William DRAI
 */
public class InvocationCall implements IInvocationCall {

    private static final long serialVersionUID = 1L;
    
    
    private List<String> listeners;
    private List<ContextUpdate> updates;
    private Object[] results;
    
    
    public InvocationCall() {
    }

    public List<String> getListeners() {
        return listeners;
    }
    public void setListeners(List<String> listeners) {
        this.listeners = listeners;
    }
    
    public List<ContextUpdate> getUpdates() {
        return updates;
    }
    public void setUpdates(List<ContextUpdate> updates) {
        this.updates = updates;
    }

    public Object[] getResults() {
        return results;
    }
    public void setResults(Object[] results) {
        this.results = results;
    }
    
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(getClass().getName()).append(" {\n");
    	if (listeners != null) {
    		sb.append("\tlisteners: [");
    		for (String listener : listeners)
    			sb.append(listener).append(" ");
    		sb.append("]\n");
    	}
    	if (updates != null) {
    		sb.append("\tupdates: [");
    		for (ContextUpdate update : updates)
    			sb.append(update).append(" ");
    		sb.append("]\n");
    	}
    	if (results != null) {
    		sb.append("\tresults: [");
    		for (Object result : results)
    			sb.append(result != null ? result.toString() : "(null)").append(" ");
    		sb.append("]\n");
    	}
    	sb.append("}");
    	return sb.toString();
    }
}
