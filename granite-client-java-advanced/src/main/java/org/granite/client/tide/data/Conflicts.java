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
package org.granite.client.tide.data;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;

/**
 *  Holds conflict data when locally changed data is in conflict with data coming from the server
 * 
 *  @author William DRAI
 */
public class Conflicts {
    
    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger("org.granite.client.tide.data.Conflicts");

    private final EntityManager entityManager;
    private final ServerSession serverSession;
    
    private List<Conflict> conflicts = new ArrayList<Conflict>();
    


    public Conflicts(EntityManager entityManager, ServerSession serverSession) {
        this.entityManager = entityManager;
        this.serverSession = serverSession;
    }
    
    public void addConflict(Object localEntity, Object receivedEntity, List<String> properties) {
        Conflict conflict = new Conflict(this, localEntity, receivedEntity, properties);
        conflicts.add(conflict);
    }
    
    public List<Conflict> getConflicts() {
        return conflicts;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
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
