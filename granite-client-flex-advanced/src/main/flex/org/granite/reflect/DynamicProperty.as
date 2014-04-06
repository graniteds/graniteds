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
package org.granite.reflect {

	/**
	 * Represents a dynamic propert of a given <code>Type</code> <i>instance</i>.
	 * 
	 * <p>
	 * Note: dynamic properties are never returned as part of <code>Type</code>'s members.
	 * They belong, by definition, to <i>instances</i>, not classes, and their purpose is
	 * to represent a special kind of property when visiting a dynamic object or key/value
	 * pair class instances (such as <code>Dictionary</code> and the various collection
	 * classes with numeric keys).
	 * </p>
	 * 
	 * @see org.granite.reflect.Visitable
	 * 
	 * @author Franck WOLFF
	 */
	public class DynamicProperty extends DescribedElement implements IMember, IVisitableElement {
		
		/**
		 * @private
		 */
		private static const _DEFAULT_GETTER:Function = function(holder:Object, key:*):* {
			return holder[key];
		} 
		
		/**
		 * @private
		 */
		private static const _DEFAULT_SETTER:Function = function(holder:Object, key:*, value:*):void {
			holder[key] = value;
		} 
		
		/**
		 * @private
		 */
		private var _holder:Object;
		
		/**
		 * @private
		 */
		private var _key:*;
		
		/**
		 * @private
		 */
		private var _getter:Function;
		
		/**
		 * @private
		 */
		private var _setter:Function;

		/**
		 * Constructs a new <code>DynamicProperty</code> instance.
		 * 
		 * <p>
		 * The optional getter and setter functions are expected to have the following signatures:
		 * </p>
		 * 
		 * <listing>
		 * function (holder:Object, key:~~):~~</listing>
		 * <listing>
		 * function (holder:Object, key:~~, value:~~):void</listing>
		 * 
		 * <p>
		 * If no getter or setter functions are provided, it is expected that getting and setting
		 * this dynamic property value will follow this standard:
		 * </p>
		 * 
		 * <listing>
		 * holder[key]</listing>
		 * <listing>
		 * holder[key] = value</listing>
		 * 
		 * <p> </p>
		 * 
		 * @param declaredBy the <code>Type</code> instance this dynamic property belongs to.
		 * @param key the key (name or object) of this dynamic property.
		 * @param getter the getter function to be used to get this dynamic property value.
		 * @param setter the setter function to be used to set this dynamic property value.
		 */
		function DynamicProperty(holder:Object, key:*, getter:Function = null, setter:Function = null) {
			super(Type.forInstance(holder), new XML('<dynamic name="' + keyToXMLString(key) + '"/>'));
			
			_holder = holder;
			_key = key;
			_getter = (getter != null ? getter : _DEFAULT_GETTER);
			_setter = (setter != null ? setter : _DEFAULT_SETTER);
		}
		
		/**
		 * @private
		 */
		private static function keyToXMLString(key:*):String {
			return String(key).replace(/\</g, "&lt;").replace(/\"/g, "&quot;");
		}
		
		/**
		 * The instance thet holds this dynamic property. 
		 */
		public function get holder():Object {
			return _holder;
		}
		
		/**
		 * The key (name or object) of this dynamic property. 
		 */
		public function get key():* {
			return _key;
		}
		
		/**
		 * @inheritDoc
		 */
		public function get propertyKey():String {
			return String(_key);
		}
		
		/**
		 * The value of this dynamic property. 
		 */
		public function get value():* {
			return _getter(_holder, _key);
		}
		public function set value(value:*):void {
			_setter(_holder, _key, value);
		}
		
		/**
		 * The <code>Type</code> of the value of this dynamic property. 
		 */
		public function get type():Type {
			return Type.forInstance(value);
		}
		
		/**
		 * @inheritDoc
		 */
		public function isStatic():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isConstant():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isVariable():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isAccessor():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isDynamicProperty():Boolean {
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isMethod():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isConstructor():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isReadable():Boolean {
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isWriteable():Boolean {
			return true;
		}
	}
}