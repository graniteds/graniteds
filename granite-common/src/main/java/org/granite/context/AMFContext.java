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
package org.granite.context;

import java.util.HashMap;
import java.util.Map;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class AMFContext {

    private Map<String, Object> customResponseHeaders = new HashMap<String, Object>();

    public abstract Message getRequest();

    public String getChannelId() {
        Message message = getRequest();
        if (message != null) {
            Object id = message.getHeader(Message.ENDPOINT_HEADER);
            if (id instanceof String)
                return (String)id;
        }
        return null;
    }

    public Map<String, Object> getCustomResponseHeaders() {
        return customResponseHeaders;
    }
}
