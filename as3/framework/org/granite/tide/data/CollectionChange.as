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
	 * 	Holds a compact reference to an entity 
	 * 
     * 	@author William DRAI
	 */
	[RemoteClass(alias="org.granite.tide.data.CollectionChange")]
    public class CollectionChange implements IExternalizable {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.CollectionChange");
    
		private var _type:int;
        private var _key:*;
		private var _value:Object;
        


        public function CollectionChange(type:int, key:*, value:Object):void {
			_type = type;
			_key = key;
			_value = value;
        }
		
		public function get type():int {
			return _type;
		}
		
		public function get key():* {
			return _key;
		}
		
		public function get value():Object {
			return _value;
		}
		
		public function readExternal(input:IDataInput):void {
			_type = input.readInt();
			_key = input.readObject();
			_value = input.readObject();
		}
		
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_type);
			output.writeObject(_key);
			output.writeObject(_value);
		}
    }
}
