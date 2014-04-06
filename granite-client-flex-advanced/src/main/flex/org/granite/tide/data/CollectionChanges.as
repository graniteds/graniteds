/*
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
package org.granite.tide.data {
	
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	import mx.logging.ILogger;
	import mx.logging.Log;
    
	/**
	 * 	Holds a compacted list of data changes to send to the server 
	 * 
     * 	@author William DRAI
	 */
	[RemoteClass(alias="org.granite.tide.data.CollectionChanges")]
    public class CollectionChanges implements IExternalizable {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.CollectionChanges");
    
        private var _changes:Array = new Array();
        
		
		public function addChange(type:int, key:Object, value:Object):void {
			_changes.push(new CollectionChange(type, key, value));
		}
		
		public function get length():uint {
			return changes.length;
		}
		
		public function getChange(index:int):CollectionChange {
			return CollectionChange(changes[index]);
		}		

        public function get changes():Array {
        	return _changes;
        }
		
		public function readExternal(input:IDataInput):void {
			_changes = input.readObject() as Array;
		}
		
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_changes);
		}
    }
}
