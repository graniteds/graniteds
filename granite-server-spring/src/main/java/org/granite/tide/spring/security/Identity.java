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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.granite.tide.annotations.TideEnabled;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;


/**
 * 	@author William DRAI
 * 
 * 	Adapted from the Spring security JSP taglib
 */
@SuppressWarnings("deprecation")
@TideEnabled
public class Identity {
        
    public Identity() {
    }
    
    
    public String isLoggedIn() {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	if (auth != null && !(auth instanceof AnonymousAuthenticationToken))
    		return auth.getName();
    	return null;
    }
    
    
    public boolean ifNotGranted(String authorities) {
        final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
        Set<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(authorities));
        return grantedCopy.isEmpty();
    }
    
    public boolean ifAllGranted(String authorities) {
        final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
        return granted.containsAll(parseAuthoritiesString(authorities));
    }
    
    public boolean ifAnyGranted(String authorities) {
        final Collection<? extends GrantedAuthority> granted = getPrincipalAuthorities();
        Set<GrantedAuthority> grantedCopy = retainAll(granted, parseAuthoritiesString(authorities));
        return !grantedCopy.isEmpty();
    }

    private Collection<? extends GrantedAuthority> getPrincipalAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null)
            return Collections.emptyList();

        return authentication.getAuthorities();
    }

	private Set<GrantedAuthority> parseAuthoritiesString(String authorizationsString) {
        final Set<GrantedAuthority> requiredAuthorities = new HashSet<GrantedAuthority>();
        final String[] authorities = StringUtils.commaDelimitedListToStringArray(authorizationsString);

        for (int i = 0; i < authorities.length; i++) {
            String authority = authorities[i];
            String role = authority.trim();
            role = StringUtils.deleteAny(role, "\t\n\r\f");

            requiredAuthorities.add(new GrantedAuthorityImpl(role));
        }

        return requiredAuthorities;
    }

    private Set<GrantedAuthority> retainAll(final Collection<? extends GrantedAuthority> granted, final Set<GrantedAuthority> required) {
        Set<String> grantedRoles = authoritiesToRoles(granted);
        Set<String> requiredRoles = authoritiesToRoles(required);
        grantedRoles.retainAll(requiredRoles);

        return rolesToAuthorities(grantedRoles, granted);
    }

    private Set<String> authoritiesToRoles(Collection<? extends GrantedAuthority> c) {
        Set<String> roles = new HashSet<String>();
        for (GrantedAuthority authority : c) {
            if (authority.getAuthority() != null)
	            roles.add(authority.getAuthority());
        }
        return roles;
    }

    private Set<GrantedAuthority> rolesToAuthorities(Set<String> grantedRoles, Collection<? extends GrantedAuthority> granted) {
        Set<GrantedAuthority> target = new HashSet<GrantedAuthority>();
        for (String role : grantedRoles) {
            for (GrantedAuthority authority : granted) {
                if (authority.getAuthority().equals(role)) {
                    target.add(authority);
                    break;
                }
            }
        }
        return target;
    }
}
