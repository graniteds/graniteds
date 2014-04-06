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
package org.granite.client.javafx.tide;

import javafx.beans.property.ReadOnlyBooleanPropertyBase;

import org.granite.client.javafx.tide.spring.Identity;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.SimpleTideResponder;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;

/**
 * @author William DRAI
 */
public class ObservablePermission extends ReadOnlyBooleanPropertyBase {
	
	private final BaseIdentity identity;
	private final Context context;
	private final ServerSession serverSession;
	private final String name;
	private final Object entity;
	private final String action;
	
	private Boolean hasPermission = null;
	
	
	public ObservablePermission(Identity identity, Context context, ServerSession serverSession, String name, Object entity, String action) {
		super();
		this.identity = identity;
		this.context = context;
		this.serverSession = serverSession;
		this.name = name;
		this.entity = entity;
		this.action = action;
	}

	@Override
	public Object getBean() {
		return identity;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean get() {
		if (hasPermission == null) {
			if (this.identity.isLoggedIn())
				getFromRemote(null);
			return false;
    	}
		return hasPermission;
    }
	
	public boolean get(TideResponder<Boolean> tideResponder) {
		if (hasPermission != null) {
	    	if (tideResponder != null) {
	    		TideResultEvent<Boolean> event = new TideResultEvent<Boolean>(context, serverSession, null, hasPermission);
	    		tideResponder.result(event);
	    	}
	    	return hasPermission;
		}
		if (this.identity.isLoggedIn())
			getFromRemote(tideResponder);
		return false;
	}    
	
	public void getFromRemote(final TideResponder<Boolean> tideResponder) {
		this.identity.call(name, entity, action, new SimpleTideResponder<Boolean>() {
			@Override
			public void result(TideResultEvent<Boolean> event) {
				if (tideResponder != null)
					tideResponder.result(event);
				hasPermission = event.getResult();
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
		hasPermission = null;
		fireValueChangedEvent();
	}
}