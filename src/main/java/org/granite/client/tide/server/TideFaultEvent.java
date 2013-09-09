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

package org.granite.client.tide.server;

import java.util.Map;
import java.util.concurrent.Future;

import org.granite.client.tide.Context;


/**
 *  Event that is provided to Tide result handlers and that holds the result object.
 * 
 *  @author William DRAI
 */
public class TideFaultEvent extends TideRpcEvent {
    
    private Fault fault;
    private Map<String, Object> extendedData;
    private ServerSession serverSession;
    private ComponentListener<?> componentListener;

    public TideFaultEvent(Context context, ServerSession serverSession, ComponentListener<?> componentListener, Fault fault, Map<String, Object> extendedData) {
        super(context, serverSession, componentListener);
        this.fault = fault;
        this.extendedData = extendedData;
        this.serverSession = serverSession;
        this.componentListener = componentListener;
    }
    
    public int getCallId() {
    	return componentListener.hashCode();
    }
    
    public Fault getFault() {
        return fault;
    }
    
    public void setFault(Fault fault) {
        this.fault = fault;
    }
    
    public Map<String, Object> getExtendedData() {
        return extendedData;
    }
    
    public void setExtendedData(Map<String, Object> extendedData) {
        this.extendedData = extendedData;
    }
    
    public Future<?> retry() {
    	return componentListener.invoke(serverSession);
    }

}
