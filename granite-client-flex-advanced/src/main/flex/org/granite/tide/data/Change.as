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

	import flash.net.getClassByAlias;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.utils.getQualifiedClassName;
	
	import mx.logging.ILogger;
	import mx.logging.Log;
	
	import org.granite.IValue;
	import org.granite.reflect.Type;
	import org.granite.tide.IEntityRef;

	/**
	 * 	Holds a compacted data change to send to the server 
	 * 
     * 	@author William DRAI
	 */
	[RemoteClass(alias="org.granite.tide.data.Change")]
    public class Change implements IEntityRef, IValue, IExternalizable {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.Change");
    
        private var _className:String;
		private var _alias:String;
		private var _uid:String;
		private var _id:Object;
		private var _version:Number;
		private var _changes:Object;
		
		private var _local:Boolean = false;
        


        public function Change(className:String = null, uid:String = null, id:Object = null, version:Number = NaN, local:Boolean = false):void {
			_className = className;
			if (className != null)	// className == null during deserialization
				_alias = Type.forName(className).alias;
			_uid = uid;
			_id = id;
			_version = version;
			_changes = {};
			_local = local;
        }
		
		public function get className():String {
			return _className;
		}
		
		public function get uid():String {
			return _uid;
		}
		
		public function get id():Object {
			return _id;
		}
		
		public function get version():Number {
			return _version;
		}
        
        public function get changes():Object {
        	return _changes;
        }

        public function isForEntity(entity:Object):Boolean {
            return getQualifiedClassName(entity) == _className && entity.uid == _uid;
        }

        public function get empty():Boolean {
            for (var p:String in this) {
                if (p != "version")
                    return false;
            }
            return true;
        }
		
		public function get local():Boolean {
			return _local;
		}
		
		public function equals(o:*):Boolean {
			return o is Change && o.className == this.className && o.uid == this.uid;
		}
		
		public function readExternal(input:IDataInput):void {
			_alias = input.readObject() as String;
            _className = getQualifiedClassName(getClassByAlias(_alias));
			_uid = input.readObject() as String;
			_id = input.readObject();
			_version = input.readObject() as Number;
			_changes = input.readObject();
		}
		
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_alias);
			output.writeObject(_uid);
			output.writeObject(_id);
			output.writeObject(_version);
			output.writeObject(_changes);
		}
    }
}
