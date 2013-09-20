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

package org.granite.tide.data {
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.utils.ObjectUtil;
    
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IEntity;


    use namespace meta;


	/**
	 * 	Holds conflict data when locally changed data is in conflict with data coming from the server
	 * 
     * 	@author William DRAI
	 */
    public class Conflicts {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.Conflicts");
    
        private var _context:BaseContext;
        private var _entityManager:EntityManager;
        
        private var _conflicts:Array = new Array();
        


        public function Conflicts(context:BaseContext, entityManager:EntityManager):void {
            _context = context;
            _entityManager = entityManager;
        }
        
        public function addConflict(localEntity:IEntity, receivedEntity:Object, properties:Array = null):void {
        	var conflict:Conflict = new Conflict(this, localEntity, receivedEntity, properties);
        	_conflicts.push(conflict);
        }
        
        public function get conflicts():Array {
        	return _conflicts;
        }        
        
        public function get empty():Boolean {
        	return _conflicts.length == 0;
        }
        
        public function get allResolved():Boolean {
        	for each (var c:Conflict in _conflicts) {
        		if (!c.resolved)
        			return false;
        	}
        	return true;
        }
        
        public function acceptClient(conflict:Conflict):void {
        	var saveTracking:Boolean = _context.meta_tracking;
			try {
	        	_context.meta_setTracking(false);
	        	var modifiedEntity:Object = ObjectUtil.copy(conflict.localEntity);
	            // Reset the local entity to its last stable state
	        	_context.meta_resetEntity(conflict.localEntity);
	            // Merge with the incoming entity (to update version, id and all)
	            if (conflict.receivedEntity != null)
	        	    _entityManager.mergeExternal(conflict.receivedEntity, conflict.localEntity);
	        	
	        	_entityManager.resolveMergeConflicts(modifiedEntity, conflict.localEntity, true);
			}
			finally {
        		_context.meta_setTracking(saveTracking);
			}
        }
        
        public function acceptAllClient():void {
        	for each (var c:Conflict in _conflicts)
        		acceptClient(c);
        }
        
        public function acceptServer(conflict:Conflict):void {
        	var saveTracking:Boolean = _context.meta_tracking;
			try {
				_context.meta_setTracking(false);
        		_context.meta_resetEntity(conflict.localEntity);
			
        		_entityManager.resolveMergeConflicts(conflict.receivedEntity, conflict.localEntity, false);
			}
			finally {
        		_context.meta_setTracking(saveTracking);
			}
        }
        
        public function acceptAllServer():void {
        	for each (var c:Conflict in _conflicts)
        		acceptServer(c);
        }
    }
}
