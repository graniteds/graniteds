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
	
	import org.granite.reflect.Type;
	import org.granite.tide.IEntityRef;

	/**
	 * 	Holds a compact reference to an entity 
	 * 
     * 	@author William DRAI
	 */
	[RemoteClass(alias="org.granite.tide.data.ChangeRef")]
    public class ChangeRef implements IEntityRef, IExternalizable {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.ChangeRef");
    
		private var _className:String;
		private var _alias:String;
        private var _uid:String;
		private var _id:Object;
        


        public function ChangeRef(className:String = null, uid:String = null, id:Object = null):void {
            _className = className;
			if (className != null)	// className == null during deserialization
				_alias = Type.forName(className).alias;
			_uid = uid;
			_id = id;
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

        public function isForEntity(entity:Object):Boolean {
            return getQualifiedClassName(entity) == _className && entity.uid == _uid;
        }

		public function readExternal(input:IDataInput):void {
            _alias = input.readObject() as String;
            _className = getQualifiedClassName(getClassByAlias(_alias));
			_uid = input.readObject() as String;
			_id = input.readObject();
		}
		
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_alias);
			output.writeObject(_uid);
			output.writeObject(_id);
		}
    }
}
