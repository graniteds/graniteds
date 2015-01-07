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
package org.granite.tide.spring.security;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.granite.tide.annotations.TideEnabled;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * 	@author William DRAI
 * 
 * 	Adapted from the Spring security JSP taglib
 */
@TideEnabled
public class AclIdentity extends Identity {
    
    private SidRetrievalStrategy sidRetrievalStrategy = null;
    private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = null;
    private AclService aclService = null;
    
    
    public AclIdentity() {
    	sidRetrievalStrategy = new SidRetrievalStrategyImpl();
    	objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();
    }
    
    public void setSidRetrievalStrategy(SidRetrievalStrategy strategy) {
    	sidRetrievalStrategy = strategy;
    }
    
    public void setObjectIdentityRetrievalStrategy(ObjectIdentityRetrievalStrategy strategy) {
    	objectIdentityRetrievalStrategy = strategy;
    }
    
    public void setAclService(AclService aclService) {
    	this.aclService = aclService;
    }
    
    
    public boolean hasPermission(Object entity, String permissions) {
    	if (entity == null)
    		return true;
    	
        List<Permission> requiredPermissions = null;
        try {
            requiredPermissions = parsePermissionsString(permissions);
        } 
        catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
        	return false;

        List<Sid> sids = sidRetrievalStrategy.getSids(authentication);
        ObjectIdentity oid = objectIdentityRetrievalStrategy.getObjectIdentity(entity);

        // Obtain aclEntrys applying to the current Authentication object
        try {
            Acl acl = aclService.readAclById(oid, sids);

            return acl.isGranted(requiredPermissions, sids, false);
        } 
        catch (NotFoundException nfe) {
            return false;
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Permission> parsePermissionsString(String integersString) throws NumberFormatException {
    	final List permissions = new ArrayList();   // Voluntarily not typed to avoid compilation problem with both Spring 2 and Spring 3
    	final StringTokenizer tokenizer;
    	tokenizer = new StringTokenizer(integersString, ",", false);
    	
    	PermissionFactory pf = new DefaultPermissionFactory();
    	while (tokenizer.hasMoreTokens()) {
    		String integer = tokenizer.nextToken();
    		permissions.add(pf.buildFromMask(new Integer(integer).intValue()));
    	}
    	
    	return permissions;
    }
}
