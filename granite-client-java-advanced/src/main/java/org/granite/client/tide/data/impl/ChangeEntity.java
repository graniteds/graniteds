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
package org.granite.client.tide.data.impl;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.spi.EntityRef;
import org.granite.tide.data.Change;

/**
 * Created by william on 05/03/14.
 */
public class ChangeEntity extends Change implements EntityRef {

    private static final long serialVersionUID = 1L;

    public ChangeEntity(Change change, ClientAliasRegistry aliasRegistry) {
    	super(getTypeForAlias(aliasRegistry, change), change.getId(), change.getVersion(), change.getUid());
    	getChanges().putAll(change.getChanges());
    }
    
    private static String getTypeForAlias(ClientAliasRegistry aliasRegistry, Change change) {
    	if (aliasRegistry.getTypeForAlias(change.getClassName()) != null)
    		return aliasRegistry.getTypeForAlias(change.getClassName());
    	return change.getClassName();
    }
}
