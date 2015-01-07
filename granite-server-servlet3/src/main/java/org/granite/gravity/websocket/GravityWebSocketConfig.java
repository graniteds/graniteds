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

import org.granite.util.ContentType;

import javax.servlet.http.HttpSession;

/**
 * Created by william on 13/02/14.
 */
public class GravityWebSocketConfig {

    public static ThreadLocal<GravityWebSocketConfig> config = new ThreadLocal<GravityWebSocketConfig>();

    public String connectMessageId;
    public String clientId;
    public String clientType;
    public ContentType contentType;
    public HttpSession session;

    public static void set(String connectMessageId, String clientId, String clientType, ContentType contentType, HttpSession session) {
        GravityWebSocketConfig c = new GravityWebSocketConfig();
        c.connectMessageId = connectMessageId;
        c.clientId = clientId;
        c.clientType = clientType;
        c.contentType = contentType;
        c.session = session;
        config.set(c);
    }

    public static GravityWebSocketConfig remove() {
        GravityWebSocketConfig c = config.get();
        config.remove();
        return c;
    }
}
