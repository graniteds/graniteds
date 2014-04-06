/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.server;

import org.granite.client.tide.Context;

/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideRpcEvent {
    
    private Context context;
    private ServerSession serverSession;
    private ComponentListener<?> componentListener;
    private boolean defaultPrevented = false;

    
    public TideRpcEvent(Context context, ServerSession serverSession, ComponentListener<?> componentListener) {
        this.context = context;
        this.serverSession = serverSession;
        this.componentListener = componentListener;
    }
    
    public Context getContext() {
        return context;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
    }
    
    public ComponentListener<?> getComponentListener() {
        return componentListener;
    }
    
    public void preventDefault() {
        defaultPrevented = true;
    }
    
    public boolean isDefaultPrevented() {
        return defaultPrevented;
    }
    
}
