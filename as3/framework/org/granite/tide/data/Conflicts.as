/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.
  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data {
    
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.getQualifiedClassName;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.collections.ICollectionView;
    import mx.collections.IList;
    import mx.collections.ListCollectionView;
    import mx.core.IUID;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.utils.ObjectUtil;
    
    import org.granite.collections.IMap;
    import org.granite.meta;
    import org.granite.collections.IPersistentCollection;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExpression;
    import org.granite.tide.EntityDescriptor;
    import org.granite.tide.IWrapper;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.collections.PersistentMap;


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
        
        public function addConflict(localEntity:IEntity, receivedEntity:IEntity):void {
        	var conflict:Conflict = new Conflict(this, localEntity, receivedEntity);
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
        	_context.meta_tracking = false;
        	var modifiedEntity:Object = ObjectUtil.copy(conflict.localEntity);
        	_context.meta_resetEntity(conflict.localEntity);
        	_entityManager.mergeExternal(conflict.receivedEntity, conflict.localEntity);
        	
        	_entityManager.resolveMergeConflicts(modifiedEntity, conflict.localEntity, true);
        	_context.meta_tracking = saveTracking;
        }
        
        public function acceptAllClient():void {
        	for each (var c:Conflict in _conflicts)
        		acceptClient(c);
        }
        
        public function acceptServer(conflict:Conflict):void {
        	var saveTracking:Boolean = _context.meta_tracking;
        	_context.meta_tracking = false;
        	_context.meta_resetEntity(conflict.localEntity);
			
        	_entityManager.resolveMergeConflicts(conflict.receivedEntity, conflict.localEntity, false);
        	_context.meta_tracking = saveTracking;
        }
        
        public function acceptAllServer():void {
        	for each (var c:Conflict in _conflicts)
        		acceptServer(c);
        }
    }
}
