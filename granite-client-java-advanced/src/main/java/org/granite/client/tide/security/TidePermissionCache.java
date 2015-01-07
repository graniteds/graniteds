/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
import java.util.IdentityHashMap;
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
public class TidePermissionCache {
	
	private final BaseIdentity identity;
	private final Map<Object, Map<String, Object>> cache = new IdentityHashMap<Object, Map<String, Object>>();
	
	public TidePermissionCache(BaseIdentity identity) {
		this.identity = identity;
	}
	
	public boolean get(Object object, String action, TideResponder<Boolean> tideResponder) {
		Map<String, Object> objectCache = cache.get(object);
		if (objectCache == null) {
			objectCache = new HashMap<String, Object>();
			cache.put(object, objectCache);
		}
        Object cached = objectCache.get(action);
        if (cached == null) {
            if (this.identity.isLoggedIn()) {
                TideResponder<Boolean> responder = new TidePermissionResponder(object, action, tideResponder);
                identity.call("hasPermission", object, action, responder);
                objectCache.put(action, responder);
            }
            return false;
        }
        if (cached instanceof TideResponder) {
        	((TidePermissionResponder)cached).addResponder(tideResponder);
        	return false;
        }
        if (tideResponder != null) {
            TideResultEvent<Boolean> event = identity.newResultEvent((Boolean)cached);
            tideResponder.result(event);
        }
        return (Boolean)cached;
	}    
	
	public void clear() {
		for (Entry<Object, Map<String, Object>> me : cache.entrySet()) {
			for (Entry<String, Object> me2 : me.getValue().entrySet()) {
				if (me2.getValue() == Boolean.TRUE)
					identity.firePropertyChange("hasPermission", true, false);
			}
		}			
	    cache.clear();
	}
	
	
	private class TidePermissionResponder implements TideResponder<Boolean> {
		
		private final Object object;
		private final String action;
		private final List<TideResponder<Boolean>> responders = new ArrayList<TideResponder<Boolean>>();
		
		public TidePermissionResponder(Object object, String action, TideResponder<Boolean> tideResponder) {
			this.object = object;
			this.action = action;
			if (tideResponder != null)
				responders.add(tideResponder);
		}
		
		public void addResponder(TideResponder<Boolean> tideResponder) {
			responders.add(tideResponder);
		}
		
	    public void result(TideResultEvent<Boolean> event) {
	        for (TideResponder<Boolean> responder : responders)
	        	responder.result(event);
	        
	        Map<String, Object> objectCache = cache.get(object);
	        Object cached = objectCache != null ? objectCache.get(action) : null;
	        boolean oldValue = cached instanceof Boolean ? (Boolean)cached : false;
	        objectCache.put(action, event.getResult());
	        boolean newValue = event.getResult() != null ? event.getResult() : false;
	        if (event.getResult() != oldValue)
	        	identity.firePropertyChange("hasPermission", oldValue, newValue);
	    }
	    
	    public void fault(TideFaultEvent event) {
	        Map<String, Object> objectCache = cache.get(object);
	        if (objectCache != null)
	        	objectCache.remove(action);
	        
	        for (TideResponder<Boolean> responder : responders)
	        	responder.fault(event);
	    }
	}
}