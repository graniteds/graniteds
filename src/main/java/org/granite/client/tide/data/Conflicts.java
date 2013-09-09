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

import java.util.ArrayList;
import java.util.List;

import org.granite.logging.Logger;

/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflicts {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.Conflicts");

    private EntityManager entityManager;
    
    private List<Conflict> conflicts = new ArrayList<Conflict>();
    


    public Conflicts(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public void addConflict(Object localEntity, Object receivedEntity, List<String> properties) {
        Conflict conflict = new Conflict(this, localEntity, receivedEntity, properties);
        conflicts.add(conflict);
    }
    
    public List<Conflict> getConflicts() {
        return conflicts;
    }        
    
    public boolean isEmpty() {
        return conflicts.size() == 0;
    }
    
    public boolean isAllResolved() {
        for (Conflict c : conflicts) {
            if (!c.isResolved())
                return false;
        }
        return true;
    }
    
    public void acceptClient(Conflict conflict) {
        entityManager.acceptConflict(conflict, true);
    }
    
    public void acceptAllClient() {
        for (Conflict c : conflicts)
            acceptClient(c);
    }
    
    public void acceptServer(Conflict conflict) {
        entityManager.acceptConflict(conflict, false);
    }
    
    public void acceptAllServer() {
        for (Conflict c : conflicts)
            acceptServer(c);
    }
}
