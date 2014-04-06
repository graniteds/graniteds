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
package org.granite.client.tide.spring;

import javax.inject.Named;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.tide.BaseIdentity;
import org.granite.client.tide.security.TidePermissionCache;
import org.granite.client.tide.security.TideRoleCache;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideResponder;

/**
 * @author William DRAI
 */
@RemoteAlias("org.granite.tide.spring.security.Identity")
@Named
public class Identity extends BaseIdentity {

    public Identity() {
        // proxying
    }

    public Identity(final ServerSession serverSession) {
    	super(serverSession);
    }
    
    
    private final TideRoleCache ifAllGrantedCache = new TideRoleCache(this, "ifAllGranted");
    private final TideRoleCache ifAnyGrantedCache = new TideRoleCache(this, "ifAnyGranted");
    private final TideRoleCache ifNotGrantedCache = new TideRoleCache(this, "ifNotGranted");
    private final TidePermissionCache hasPermissionCache = new TidePermissionCache(this);
    

    public boolean hasRole(String roleName, final TideResponder<Boolean> tideResponder) {
    	return ifAllGranted(roleName, tideResponder);
    }
    
    public boolean ifAllGranted(final String roleName, final TideResponder<Boolean> tideResponder) {
    	return ifAllGrantedCache.get(roleName, tideResponder);
    }

    public boolean ifAnyGranted(final String roleName, final TideResponder<Boolean> tideResponder) {
    	return ifAnyGrantedCache.get(roleName, tideResponder);
    }

    public boolean ifNotGranted(final String roleName, final TideResponder<Boolean> tideResponder) {
    	return ifNotGrantedCache.get(roleName, tideResponder);
    }
    
    public boolean hasPermission(final Object object, final String action, final TideResponder<Boolean> tideResponder) {
    	return hasPermissionCache.get(object, action, tideResponder);
    }
    
    
    protected void initSecurityCache() {    	
        ifAllGrantedCache.clear();
        ifAnyGrantedCache.clear();
        ifNotGrantedCache.clear();
        hasPermissionCache.clear();
    }
    
    /**
     * 	Clear the security cache
     */
    @Override
    public void clearSecurityCache() {
    	super.clearSecurityCache();
    	
        ifAllGrantedCache.clear();
        ifAnyGrantedCache.clear();
        ifNotGrantedCache.clear();
        hasPermissionCache.clear();
    }

}
