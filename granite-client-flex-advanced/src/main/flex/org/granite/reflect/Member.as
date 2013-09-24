/*
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
package org.granite.reflect {

	/**
	 * Base class for <code>Field</code>, <code>Method</code> and <code>Constructor</code>
	 * classes.
	 * 
	 * @author Franck WOLFF
	 */
	public class Member extends DescribedElement implements IMember, IAnnotatedElement {
		
		/**
		 * @private
		 */
		protected static const STATIC:uint      = 1 << 0;
		
		/**
		 * @private
		 */
		protected static const CONSTANT:uint    = 1 << 1;
		
		/**
		 * @private
		 */
		protected static const VARIABLE:uint    = 1 << 2;
		
		/**
		 * @private
		 */
		protected static const ACCESSOR:uint    = 1 << 3;
		
		/**
		 * @private
		 */
		protected static const METHOD:uint      = 1 << 4;
		
		/**
		 * @private
		 */
		protected static const CONSTRUCTOR:uint = 1 << 5;
		
		/**
		 * @private
		 */
		protected static const READABLE:uint    = 1 << 6;
		
		/**
		 * @private
		 */
		protected static const WRITABLE:uint    = 1 << 7;
		
		/**
		 * @private
		 */
		private var _modifiers:uint; 

		/**
		 * Constructs a new <code>Member</code> instance.
		 * 
		 * @param declaredBy the <code>Type</code> instance this member belongs to.
		 * @param desc the XML description of the member.
		 */
		function Member(declaredBy:Type, desc:XML) {
			super(declaredBy, desc);
			
			_modifiers = initModifiers();
		}
		
		/**
		 * @inheritDoc
		 */
		public function get propertyKey():String {
			return qName.toString();
		}

		/**
		 * The name of this <code>Member</code>.
		 */
		public function get name():String {
			throw new Error("Not implemented");
		}

		/**
		 * The qualified name of this <code>Member</code>.
		 */
		public function get qName():QName {
			return new QName(uri, name);
		}

		/**
		 * The namespace of this <code>Member</code>. Note that the returned namespace
		 * has a always a prefix equal to "" (describeType only returns the namespace uri).
		 */
		public function get ns():Namespace {
			return new Namespace(uri);
		}

		/**
		 * The uri of the qualified name of this <code>Member</code> (members declared in
		 * namespaces) or "" if this member is public.
		 */
		public function get uri():String {
			throw new Error("Not implemented");
		}
		
		/**
		 * Tells if this <code>Member</code> instance is declared in the the suplied ns
		 * namespace parameter. If ns is <code>null</code>, this method checks if this
		 * member is in the public namespace (uri == "") or in the builtin AS3 namespace.
		 * Otherwise, it checks this member's uri and the ns's uri for equality.
		 * 
		 * @param ns the <code>Namespace</code> to check for equality with this member's
		 * 		namespace.
		 * @return <code>true</code> if this member's namespace is the equal to the suplied
		 * 		ns parameter, <code>false</code> otherwise.
		 * 
		 * @see AS3
		 */
		public function isInNamespace(ns:Namespace):Boolean {
			if (ns == null)
				return uri == "" || uri == AS3.uri;
			return uri == ns.uri;
		}
		
		/**
		 * @private
		 */
		protected function initModifiers():uint {
			throw new Error("Not implemented");
		}
		
		/**
		 * @private
		 */
		protected function get modifiers():uint {
			return _modifiers;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isStatic():Boolean {
			return (_modifiers & STATIC) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isConstant():Boolean {
			return (_modifiers & CONSTANT) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isVariable():Boolean {
			return (_modifiers & VARIABLE) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isAccessor():Boolean {
			return (_modifiers & ACCESSOR) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isDynamicProperty():Boolean {
			return false;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isMethod():Boolean {
			return (_modifiers & METHOD) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isConstructor():Boolean {
			return (_modifiers & CONSTRUCTOR) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isReadable():Boolean {
			return (_modifiers & READABLE) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isWriteable():Boolean {
			return (_modifiers & WRITABLE) != 0;
		}
		
		/**
		 * @inheritDoc
		 */
		public function get annotations():Array {
			return getAnnotations();
		}
		
		/**
		 * @inheritDoc
		 */
		public function getAnnotations(recursive:Boolean = false, pattern:String = "^_?[^_]"):Array {
			return new Array();
		}
		
		/**
		 * @inheritDoc
		 */
		public function getAnnotation(type:String, recursive:Boolean = false):Annotation {
			return null;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isAnnotationPresent(type:String, recursive:Boolean = false):Boolean {
			return false;
		}
	}
}