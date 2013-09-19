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
package org.granite.gravity.tomcat;

import org.apache.catalina.CometEvent;
import org.apache.catalina.CometEvent.EventSubType;
import org.apache.catalina.CometEvent.EventType;

/**
 * @author Franck WOLFF
 */
public class EventUtil {

    public static boolean isTimeout(CometEvent event) {
        return (
            event.getEventType() == EventType.ERROR &&
            event.getEventSubType() == EventSubType.TIMEOUT
        );
    }

    public static boolean isError(CometEvent event) {
        return (event.getEventType() == EventType.ERROR);
    }

    public static boolean isErrorButNotTimeout(CometEvent event) {
        return (
            event.getEventType() == EventType.ERROR &&
            event.getEventSubType() != EventSubType.TIMEOUT
        );
    }
    
    public static boolean isValid(CometEvent event) {
    	if (event != null) {
	    	try {
	    		return event.getHttpServletRequest() != null && event.getHttpServletResponse() != null;
	    	} catch (Exception e) {
	    	}
    	}
    	return false;
    }

    public static String toString(CometEvent event) {
    	if (event == null)
    		return "null";
    	try {
    		return event.getClass().getName() + " [" + event.getEventType() + '.' + event.getEventSubType() + ']';
    	}
    	catch (Exception e) {
    		return e.toString();
    	}
    }
}
