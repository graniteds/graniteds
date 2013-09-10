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

package org.granite.util {

	import flash.utils.ByteArray;

	/**
	 * A workaround class for a bug with Flash 10+ <code>Vector.&lt;~~&gt;</code>
	 * types: <code>getDefinitionByName("__AS3__.vec::Vector.&lt;~~&gt;")</code>
	 * throws an error...
	 * 
	 * @Franck WOLFF
	 */
	public class VectorUtil {
		
		/**
		 * A <code>new Vector.&lt;~~&gt;(0)</code> serialized in AMF3.
		 * Deserialization will only be supported in Flash 10+ VMs. 
		 * 
		 * @private
		 */
		private static const VECTOR0:Array = [0x10, 0x01, 0x00, 0x03, 0x2a];

		/**
		 * @private
		 */
		private static var _initialized:Boolean = false;

		/**
		 * @private
		 */
		private static var _unqualifiedVectorClass:Class = null;

		/**
		 * @private
		 */
		private static function init():void {
			if (!_initialized) {
				_initialized = true;
				
				var bytes:ByteArray = new ByteArray();
				for each (var b:int in VECTOR0)
					bytes.writeByte(b);
				
				bytes.position = 0;
				try {
					_unqualifiedVectorClass = bytes.readObject().constructor;
				}
				catch (e:Error) {
					// Not a Flash 10+ VM...
				}
			}
		} 
		
		/**
		 * Tells if the supplied class name denotes a unqualified <code>Vector</code>
		 * class name.
		 * 
		 * @param name teh class name to be test.
		 * @return <code>true</code> if the name parameter denotes a unqualified
		 * 		<code>Vector</code> class name, <code>false</code> otherwise.
		 */
		public static function isUnqualifiedVectorClassName(name:String):Boolean {
			switch (name) {
				case "__AS3__.vec::Vector.<*>":
				case "__AS3__.vec.Vector.<*>":
				case "Vector.<*>":
					return true;
			}
			return false;
		}
		
		/**
		 * The <code>Vector.&lt;~~&gt;</code> class. Will be null if the Flash VM
		 * isn't a 10+ VM.
		 */
		public static function get unqualifiedVectorClass():Class {
			init();
			
			return _unqualifiedVectorClass;
		}
		
		/**
		 * Constructs a new <code>Vector.&lt;~~&gt;</code> instance. Will be null
		 * if the current Flash VM isn't a 10+ VM.
		 * 
		 * @param length the length of the vector.
		 * @param fixed Indicates whether the length property of the Vector can be changed.
		 * @return a new <code>Vector.&lt;~~&gt;</code> instance, or <code>null</code> if
		 * 		the current Flash VM isn't a 10+ VM.
		 */
		public static function newUnqualifiedVector(length:uint = 0, fixed:Boolean = false):Object {
			init();
			
			if (_unqualifiedVectorClass != null)
				return new _unqualifiedVectorClass(length, fixed);
			return null;
		}
	}
}