/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
