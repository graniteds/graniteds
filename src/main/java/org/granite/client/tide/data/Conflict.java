/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.client.tide.data;

import java.util.List;

import org.granite.logging.Logger;

/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflict {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(Conflict.class);

    private Conflicts conflicts;
    
    private Object localEntity;
    private Object receivedEntity;
    private List<String> properties;
    private boolean resolved = false;
    


    public Conflict(Conflicts conflicts, Object localEntity, Object receivedEntity, List<String> properties) {
        this.conflicts = conflicts;
        this.localEntity = localEntity;
        this.receivedEntity = receivedEntity;
        this.properties = properties;
    }
    
    public Object getLocalEntity() {
        return localEntity;
    }        
    
    public Object getReceivedEntity() {
        return receivedEntity;
    }
    
    public List<String> getProperties() {
    	return properties;
    }
    
    public boolean isRemoval() {
        return receivedEntity == null;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void acceptClient() {
        conflicts.acceptClient(this);
        resolved = true;
    }
    
    public void acceptServer() {
        conflicts.acceptServer(this);
        resolved = true;
    }
}
