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
package org.granite.client.tide.data.impl;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeRef;

/**
 * Created by william on 05/03/14.
 */
public class ChangeEntityRef implements EntityRef {

    private String className;
    private String uid;
    
    public ChangeEntityRef(Object change, ClientAliasRegistry aliasRegistry) {
    	if (change instanceof Change) {
    		className = ((Change)change).getClassName();
    		uid = ((Change)change).getUid();
    	}
    	else if (change instanceof ChangeRef) {
    		className = ((ChangeRef)change).getClassName();
    		uid = ((ChangeRef)change).getUid();
    	}
    	if (aliasRegistry.getTypeForAlias(className) != null)
    		className = aliasRegistry.getTypeForAlias(className);
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getUid() {
        return uid;
    }
}
