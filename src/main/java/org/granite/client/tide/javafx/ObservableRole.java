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
package org.granite.client.tide.javafx;

import javafx.beans.property.ReadOnlyBooleanPropertyBase;

import org.granite.client.tide.Context;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.SimpleTideResponder;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;


public class ObservableRole extends ReadOnlyBooleanPropertyBase {
	
	private final BaseIdentity identity;
	private final Context context;
	private final ServerSession serverSession;
	private final String name;
	private final String roleName;
	
	private Boolean hasRole = null;
	
	
	public ObservableRole(BaseIdentity identity, Context context, ServerSession serverSession, String name, String roleName) {
		super();
		this.identity = identity;
		this.context = context;
		this.serverSession = serverSession;
		this.name = name;
		this.roleName = roleName;
	}

	@Override
	public Object getBean() {
		return identity;
	}

	@Override
	public String getName() {
		return name + "." + roleName;
	}

	@Override
	public boolean get() {
		if (hasRole == null) {
			if (this.identity.isLoggedIn())
				getFromRemote(null);
			return false;
    	}
		return hasRole;
    }
	
	public boolean get(TideResponder<Boolean> tideResponder) {
		if (hasRole != null) {
	    	if (tideResponder != null) {
	    		TideResultEvent<Boolean> event = new TideResultEvent<Boolean>(context, serverSession, null, hasRole);
	    		tideResponder.result(event);
	    	}
	    	return hasRole;
		}
		if (this.identity.isLoggedIn())
			getFromRemote(tideResponder);
		return false;
	}    
	
	public void getFromRemote(final TideResponder<Boolean> tideResponder) {
		this.identity.call(name, roleName, new SimpleTideResponder<Boolean>() {
			@Override
			public void result(TideResultEvent<Boolean> event) {
				if (tideResponder != null)
					tideResponder.result(event);
				hasRole = event.getResult();
				fireValueChangedEvent();
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				if (tideResponder != null)
					tideResponder.fault(event);
				clear();
			}
		});
	}
	
	public void clear() {
		hasRole = null;
		fireValueChangedEvent();
	}
}