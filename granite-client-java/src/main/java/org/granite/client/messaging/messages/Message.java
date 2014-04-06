/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.messages;

import java.io.Externalizable;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public interface Message extends Externalizable, Cloneable {

	public static enum Type {
		
		// Request types.
		PING,
		LOGIN,
		LOGOUT,
		INVOCATION,
		SUBSCRIBE,
		UNSUBSCRIBE,
		PUBLISH,
        REPLY,
		
		// Response types.
		RESULT,
		FAULT,
		
		// Push types.
		TOPIC,
		DISCONNECT
	}
    
    Type getType();
	
	String getId();
    void setId(String id);
	
	String getClientId();
    void setClientId(String clientId);

	long getTimestamp();
    void setTimestamp(long timestamp);
	
    long getTimeToLive();
    void setTimeToLive(long timeToLive);

    Map<String, Object> getHeaders();
    void setHeaders(Map<String, Object> headers);
	Object getHeader(String name);
    void setHeader(String name, Object value);
	boolean headerExists(String name);
    
    boolean isExpired();
    boolean isExpired(long millis);
    long getRemainingTimeToLive();
    long getRemainingTimeToLive(long millis);
    
    public Message copy();
    public Message clone() throws CloneNotSupportedException;
}
