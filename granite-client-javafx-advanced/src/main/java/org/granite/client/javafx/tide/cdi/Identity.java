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

