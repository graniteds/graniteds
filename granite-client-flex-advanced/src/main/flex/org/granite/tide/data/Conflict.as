/*
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
		private var _properties:Array;
        private var _resolved:Boolean = false;
        


        public function Conflict(conflicts:Conflicts, localEntity:IEntity, receivedEntity:Object, properties:Array = null):void {
            _conflicts = conflicts;
            _localEntity = localEntity;
            _receivedEntity = receivedEntity;
			_properties = properties;
        }
        
        public function get localEntity():IEntity {
        	return _localEntity;
        }        
        
        public function get receivedEntity():Object {
        	return _receivedEntity;
        }
		
		public function get properties():Array {
			return _properties;
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
