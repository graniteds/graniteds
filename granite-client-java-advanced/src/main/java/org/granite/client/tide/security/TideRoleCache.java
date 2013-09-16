/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.granite.client.tide.BaseIdentity;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;

/**
 * @author William DRAI
 */
public class TideRoleCache {
	
	private final BaseIdentity identity;
	private final String type;
	private final Map<String, Object> cache = new HashMap<String, Object>();
	
	public TideRoleCache(BaseIdentity identity, String type) {
		this.identity = identity;
		this.type = type;
	}
	
	public boolean get(String roleName, TideResponder<Boolean> tideResponder) {
        Object cached = cache.get(roleName);
        if (cached == null) {
            if (this.identity.isLoggedIn()) {
                TideResponder<Boolean> responder = new TideRoleResponder(roleName, tideResponder);
                identity.call(type, roleName, responder);
                cache.put(roleName, responder);
            }
            return false;
        }
        if (cached instanceof TideResponder) {
        	((TideRoleResponder)cached).addResponder(tideResponder);
        	return false;
        }
        if (tideResponder != null) {
            TideResultEvent<Boolean> event = identity.newResultEvent((Boolean)cached);
            tideResponder.result(event);
        }
        return (Boolean)cached;
	}    
	
	public void clear() {
		for (Entry<String, Object> me : cache.entrySet()) {
			if (me.getValue() == Boolean.TRUE)
				identity.firePropertyChange(type, true, false);
		}
			
	    cache.clear();
	}
	
	
	private class TideRoleResponder implements TideResponder<Boolean> {
		
		private final String roleName;
		private final List<TideResponder<Boolean>> responders = new ArrayList<TideResponder<Boolean>>();
		
		public TideRoleResponder(String roleName, TideResponder<Boolean> tideResponder) {
			this.roleName = roleName;
			if (tideResponder != null)
				responders.add(tideResponder);
		}
		
		public void addResponder(TideResponder<Boolean> tideResponder) {
			responders.add(tideResponder);
		}
		
	    public void result(TideResultEvent<Boolean> event) {
	        for (TideResponder<Boolean> responder : responders)
	        	responder.result(event);
	        Object cached = cache.get(roleName);
	        boolean oldValue = cached instanceof Boolean ? (Boolean)cached : false;
	        cache.put(roleName, event.getResult());
	        boolean newValue = event.getResult() != null ? event.getResult() : false;
	        if (event.getResult() != oldValue)
	        	identity.firePropertyChange(type, oldValue, newValue);
	    }
	    
	    public void fault(TideFaultEvent event) {
	        cache.remove(roleName);
	        for (TideResponder<Boolean> responder : responders)
	        	responder.fault(event);
	    }
	}
}