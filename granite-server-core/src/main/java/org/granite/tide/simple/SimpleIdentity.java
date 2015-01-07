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
package org.granite.tide.simple;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.security.ServerIdentity;


/**
 * @author William DRAI
 */
public class SimpleIdentity implements ServerIdentity {

    private static final Logger log = Logger.getLogger(SimpleIdentity.class);

    @Override
    public String isLoggedIn() {
        log.debug("isLoggedIn()");

    	GraniteContext context = GraniteContext.getCurrentInstance();
        if (!(context instanceof HttpGraniteContext))
            throw new IllegalStateException("Cannot call isLoggedIn() outside a http remoting request");

    	if (context != null && ((HttpGraniteContext)context).getRequest().getUserPrincipal() != null)
    		return ((HttpGraniteContext)context).getRequest().getUserPrincipal().getName();    	
    	return null;
    }
    
    
    public boolean hasRole(String role) {
        log.debug("hasRole(%s)", role);

        GraniteContext context = GraniteContext.getCurrentInstance();
    	if (context == null || !(context instanceof HttpGraniteContext))
    		return false;
    	
    	return ((HttpGraniteContext)context).getRequest().isUserInRole(role);
    }
}
