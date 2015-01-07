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
package org.granite.tide.seam.async;

import java.io.Serializable;
import java.util.List;

import org.granite.tide.invocation.ContextResult;
import org.jboss.seam.security.Identity;


/**
 * @author William DRAI
 */
public class AsyncContext implements Serializable {

    private static final long serialVersionUID = 1L;
    
    
    private Identity identity = null;
    private String sessionId = null;
    private List<ContextResult> results = null;
    
    
    public AsyncContext(String sessionId, List<ContextResult> results) {
        this.identity = Identity.instance();
        this.sessionId = sessionId;
        this.results = results;
    }
    
    
    public Identity getIdentity() {
        return identity;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public List<ContextResult> getResults() {
        return results;
    }
}
