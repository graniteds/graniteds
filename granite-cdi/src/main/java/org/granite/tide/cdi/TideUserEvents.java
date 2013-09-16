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
package org.granite.tide.cdi;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;


/**
 * TideAsync stores user events configuration
 * 
 * @author William DRAI
 */
@ApplicationScoped
public class TideUserEvents implements Serializable {

    private static final long serialVersionUID = -5395975397632138270L;
    
    private ConcurrentHashMap<String, UserEvents> userEventsMap = new ConcurrentHashMap<String, UserEvents>();
    
    
    public void registerEventType(String sessionId, Class<?> eventType) {
        UserEvents userEvents = userEventsMap.get(sessionId);
        if (userEvents == null) {
            userEvents = new UserEvents();
            UserEvents tmpUserEvents = userEventsMap.putIfAbsent(sessionId, userEvents); 
            if (tmpUserEvents != null) 
            	userEvents = tmpUserEvents; 
        }
        userEvents.addEventType(eventType);
    }
    
    public void unregisterSession(String sessionId) {
        userEventsMap.remove(sessionId);
    }
    
    public UserEvents getUserEvents(String sessionId) {
        return userEventsMap.get(sessionId);
    }
    
    public boolean hasEventType(String sessionId, Class<?> eventType) {
        UserEvents userEvents = userEventsMap.get(sessionId);
        if (userEvents == null)
            return false;
        return userEvents.hasEventType(eventType);
    }
}
