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
package org.granite.client.javafx.tide.cdi;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.granite.client.javafx.tide.BaseIdentity;
import org.granite.client.javafx.tide.ObservableRole;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.tide.server.ServerSession;

/**
 * @author William DRAI
 */
@RemoteAlias("org.granite.tide.cdi.Identity")
@Named
public class Identity extends BaseIdentity {
	
    protected Identity() {
    	// CDI proxying...
    }
    
    public Identity(final ServerSession serverSession) {
    	super(serverSession);
    }
        
    
    private Map<String, ObservableRole> hasRoleCache = new HashMap<String, ObservableRole>();
    
    
    public ObservableRole hasRole(String roleName) {
    	ObservableRole role = hasRoleCache.get(roleName);
    	if (role == null) {
    		role = new ObservableRole(this, getContext(), getServerSession(), "hasRole", roleName);
    		hasRoleCache.put(roleName, role);
    	}
    	return role;
    }
    

    @Override
    protected void initSecurityCache() {
    	for (ObservableRole role : hasRoleCache.values())
    		role.clear();
    }
    
    /**
     * 	Clear the security cache
     */
    @Override
    public void clearSecurityCache() {
    	for (ObservableRole role : hasRoleCache.values())
    		role.clear();
    	hasRoleCache.clear();
    }
}

