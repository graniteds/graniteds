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

	import flash.events.Event;
	import flash.net.getClassByAlias;
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	import flash.utils.Proxy;
	import flash.utils.flash_proxy;
	import flash.utils.getQualifiedClassName;
	
	import mx.core.IPropertyChangeNotifier;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.utils.ObjectProxy;
	
	import org.granite.IValue;
	import org.granite.reflect.Type;
	import org.granite.tide.IEntity;
	import org.granite.tide.IEntityRef;

	/**
	 * 	Holds a compacted data change to send to the server 
	 * 
     * 	@author William DRAI
	 */
    public dynamic class ChangeProxy extends Proxy implements IEntity, IDataProxy {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeProxy");
    
		private var _uid:String;
		private var _idPropertyName:String;
		private var _id:Object;
		private var _versionPropertyName:String;
		private var _version:Number;
		private var _changes:Object;
		private var _templateObject:Object;


        public function ChangeProxy(uid:String, idPropertyName:String, id:Object, versionPropertyName:String, version:Number, changes:Object, templateObject:Object):void {
			_uid = uid;
			_idPropertyName = idPropertyName;
			_id = id;
			_versionPropertyName = versionPropertyName;
			_version = version;
			_changes = changes;
			_templateObject = templateObject;
        }
		
		override flash_proxy function getProperty(propName:*):* {
			if (propName.toString() == _idPropertyName)
				return _id;
			else if (propName.toString() == _versionPropertyName)
				return _version;
			else if (_changes.hasOwnProperty(propName))
				return _changes[propName];
			return undefined;
		}
		
		override flash_proxy function hasProperty(propName:*):Boolean {
			if (propName.toString() == _idPropertyName)
				return true;
			else if (propName.toString() == _versionPropertyName)
				return true;
			else if (_changes.hasOwnProperty(propName))
				return true;
			return false;
		}
		
		flash_proxy function get qualifiedClassName():String {
			return getQualifiedClassName(_templateObject);
		}
		
		flash_proxy function get object():Object {
			return _templateObject;
		}
		
		override flash_proxy function setProperty(name:*, value:*):void {
			// Do nothing, cannot be changed
		}
		
		override flash_proxy function callProperty(name:*, ...rest):* {
			if (name is QName && name.localName == "isInitialized" && name.uri == "http://www.graniteds.org/2007/as3/ns/meta" && rest.length == 0)
				return true;
			throw new Error("Unsupported method on ChangeProxy " + name.toString());
		}
		
		public function get uid():String {
			return _uid;
		}
		
		public function set uid(value:String):void {
			_uid = value;
		}
		
		public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
		}
		
		public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
		}
		
		public function dispatchEvent(event:Event):Boolean {
			return false;
		}

		public function hasEventListener(type:String):Boolean {
			return false;
		}
		
		public function willTrigger(type:String):Boolean {
			return false;
		}
    }
}
