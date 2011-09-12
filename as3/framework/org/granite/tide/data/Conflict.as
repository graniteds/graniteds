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
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    
    import org.granite.meta;
    import org.granite.tide.IEntity;


    use namespace meta;


	/**
	 * 	Holds conflict data when locally changed data is in conflict with data coming from the server
	 * 
     * 	@author William DRAI
	 */
    public class Conflict {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.Conflict");
    
        private var _conflicts:Conflicts;
        
        private var _localEntity:IEntity;
        private var _receivedEntity:Object;
        private var _resolved:Boolean = false;
        


        public function Conflict(conflicts:Conflicts, localEntity:IEntity, receivedEntity:Object):void {
            _conflicts = conflicts;
            _localEntity = localEntity;
            _receivedEntity = receivedEntity;
        }
        
        public function get localEntity():IEntity {
        	return _localEntity;
        }        
        
        public function get receivedEntity():Object {
        	return _receivedEntity;
        }

        public function get isRemoval():Boolean {
            return _receivedEntity == null;
        }
        
        public function get resolved():Boolean {
        	return _resolved;
        }
        
        public function acceptClient():void {
        	_conflicts.acceptClient(this);
        	_resolved = true;
        }
        
        public function acceptServer():void {
        	_conflicts.acceptServer(this);
        	_resolved = true;
        }
    }
}
