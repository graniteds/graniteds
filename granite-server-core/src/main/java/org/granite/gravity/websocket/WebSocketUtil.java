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
package org.granite.gravity.websocket;

import org.granite.logging.Logger;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class WebSocketUtil {
    
	private static final Logger log = Logger.getLogger(WebSocketUtil.class);
	
	public static ContentType getContentType(String contentType, String protocol) {
        if (contentType == null) {
            if (protocol != null) {
        		if ("org.granite.gravity".equals(protocol))
        			contentType = ContentType.AMF.mimeType();
        		else if (protocol.startsWith("org.granite.gravity."))
        			contentType = "application/x-" + protocol.substring("org.granite.gravity.".length());
        		else
        			throw new RuntimeException("Missing Content-Type and unsupported Sec-WebSocket-Protocol: " + protocol);
        	}
            else {
            	contentType = ContentType.AMF.mimeType();
            	log.warn("Missing Content-Type and Sec-WebSocket-Protocol in handshake request. Using: %s", contentType);
            }
        }

        ContentType type = ContentType.forMimeType(contentType);
        if (type == null) {
            log.warn("No (or unsupported) content type in handshake request: %s. Using: %s", contentType, ContentType.AMF.mimeType());
            type = ContentType.AMF;
        }
        return type;
	}
}
