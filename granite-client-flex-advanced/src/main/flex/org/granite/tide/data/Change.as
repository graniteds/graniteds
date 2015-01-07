/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
