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
	[RemoteClass(alias="org.granite.tide.data.ChangeSet")]
    public class ChangeSet implements IExternalizable {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeSet");
    
        private var _changes:Array = new Array();
        

		public function get length():uint {
			return _changes.length;
		}
		
        public function addChange(change:Change):void {
        	_changes.push(change);
        }
		
		public function getChange(index:int):Change {
			return Change(_changes[index]);
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
