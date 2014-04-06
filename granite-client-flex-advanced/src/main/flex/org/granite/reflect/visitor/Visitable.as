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
package org.granite.reflect.visitor {

	import org.granite.reflect.DynamicProperty;
	import org.granite.reflect.Field;
	import org.granite.reflect.IMember;
	import org.granite.reflect.IVisitableElement;
	import org.granite.reflect.Type;

	/**
	 * A <code>Visitable</code> instance encapsulates a bean, a property of
	 * a bean or an item of a collection (at arbitrary depth).
	 * 
	 * @author Franck WOLFF
	 */
	public class Visitable {
		
		///////////////////////////////////////////////////////////////////////
		// Fields.

		private var _parent:Visitable;
		private var _element:IVisitableElement;
		private var _value:*;
		private var _data:*;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * Constructs a new <code>Visitable</code> object.
		 * 
		 * @param parent the parent of this <code>Visitable</code> element (may be
		 * 		<code>null</code> if this element is a root).
		 * @param element the type of this <code>Visitable</code> element.
		 * @param value the element to be visited.
		 * @throws ArgumentError if the <code>element</code> parameter is null.
		 */
		function Visitable(parent:Visitable, element:IVisitableElement, value:*) {
			if (element == null)
				throw new ArgumentError("Parameter element cannot be null");

			_parent = parent;
			_element = element;
			_value = value;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The parent of this <code>Visitable</code> (null if this
		 * <code>Visitable</code> is a root bean).
		 */
		public function get parent():Visitable {
			return _parent;
		}
		
		/**
		 * The type of this <code>Visitable</code>.
		 * 
		 * @see org.granite.reflect.IVisitableElement
		 */
		public function get element():IVisitableElement {
			return _element;
		}
		
		/**
		 * The value of this <code>Visitable</code>.
		 */
		public function get value():* {
			return _value;
		}
		
		/**
		 * An arbitrary user data attached to this <code>Visitable</code>.
		 */
		public function get data():* {
			return _data;
		}
		public function set data(value:*):void {
			_data = value;
		}
		
		/**
		 * The root parent of this <code>Visitable</code>.
		 */
		public function get root():Visitable {
			var root:Visitable = this;
			while (root._parent != null)
				root = root._parent;
			return root;
		}
		
		/**
		 * Tells if this <code>Visitable</code> is a root visitable (no parent).
		 * 
		 * @return <code>true</code> if this <code>Visitable</code> is a root,
		 * 		<code>false</code> otherwise.
		 */
		public function isRoot():Boolean {
			return _parent == null;
		}
		
		/**
		 * The string representation of the  path from the root visitable to this
		 * <code>Visitable</code> (this representation is dot-separated, even for
		 * a collection index or a map key, and is only meant to be used for tracing).
		 */
		public function get path():String {
			var path:String = "";
			
			for (var parent:Visitable = this; parent != null; parent = parent.parent) {
				if (parent._element is IMember)
					path = (parent._element as IMember).propertyKey + (path == "" ? "" : "." + path);
			}
			
			return path;
		}
		
		/**
		 * The name of the property represented by this <code>Visitable</code> (may be
		 * null if this <code>Visitable</code> doesn't represent a property).
		 */
		public function get property():String {
			return (_element is IMember ? (_element as IMember).propertyKey : "");
		}
		
		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		/**
		 * Returns a string representation this <code>Visitable</code> with its full
		 * path and its value.
		 * 
		 * @return a string representation this <code>Visitable</code>.
		 */
		public function toString():String {
			var s:String;
			try {
				var rootElement:IVisitableElement = root._element;
				if (rootElement is Type) {
					if (!isRoot())
						s = (rootElement as Type).name + "." + path + " = " + String(value);
					else
						s = (rootElement as Type).name  + " = " + String(value);
				}
				else
					s = "(" + path + " = " + String(value) + ")";
			}
			catch (e:Error) {
				s = e.toString();
			}
			return s;
		}
		
		/**
		 * @private
		 */
		private function getParentValue():* {
			if (_parent != null)
				return _parent.value;
			return null;
		}
	}
}