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
	 * Represents an ActionScript 3 annotation (or metadata) with all its arguments.
	 * 
	 * @see AnnotedElement
	 * 
	 * @author Franck WOLFF
	 */
	public class Annotation extends DescribedElement {

		/**
		 * @private
		 */
		private var _annotatedElement:IAnnotatedElement;

		/**
		 * @private
		 */
		private var _args:Array;
		
		/**
		 * Constructs a new <code>Annotation</code> instance.
		 * 
		 * @param annotatedElement the element (class, variable, method, etc.) to which
		 * 		this annotation is attached.
		 * @param desc the XML description of this annotation.
		 */
		function Annotation(annotatedElement:IAnnotatedElement, desc:XML) {
			super(annotatedElement.declaredBy, desc);

			_annotatedElement = annotatedElement;
		}
		
		/**
		 * The name of this annotation.
		 */
		public function get name():String {
			return desc.@name;
		}
		
		/**
		 * The <code>IAnnotatedElement</code> (class, variable, method, etc.) to which
		 * this annotation is attached.
		 */
		public function get annotatedElement():IAnnotatedElement {
			return _annotatedElement;
		}
		
		/**
		 * An array of <code>Arg</code> instances that contains all arguments of this
		 * annotation.
		 * 
		 * @see Arg
		 */
		public function get args():Array {
			initArgs();
			
			return _args.concat();
		}
		
		/**
		 * Tests if this annotation has an argument whose key is equal to the supplied
		 * parameter.
		 * 
		 * @param the key (or name) of the argument to check for ("" for the default
		 * 		argument).
		 * @return <code>true</code> if this annotation holds an argurment with this name,
		 * 		<code>false</code> otherwise.
		 */
		public function isArgPresent(key:String = ""):Boolean {
			initArgs();
			
			for each (var arg:Arg in _args) {
				if (arg.key == key)
					return true;
			}
			
			return false;
		}
		
		/**
		 * Returns the value of the first argument of this annotation whose key is equal
		 * to the supplied parameter.
		 * 
		 * @param key the key (or name) of the argument to get value from ("" for the default
		 * 		argument).
		 * @param defaultValue the value to be returned if this annotation has no
		 * 		such argument.
		 * @return the argument value, or <code>defaultValue</code> if this annotation has no
		 * 		such argument.
		 */
		public function getArgValue(key:String = "", defaultValue:String = null):String {
			initArgs();
			
			for each (var arg:Arg in _args) {
				if (arg.key == key)
					return arg.value;
			}
			
			return defaultValue;
		}

		/**
		 * Returns an array that contains all values of the arguments of this annotation
		 * whose key is equal to the supplied parameter.
		 * 
		 * @param the key (or name) of the argument to get value from.
		 * @return the argument value, or <code>null</code> if this annotation has no
		 * 		such argument.
		 */
		public function getArgValues(key:String):Array {
			initArgs();

			var values:Array = new Array();
			for each (var arg:Arg in _args) {
				if (arg.key == key)
					values.push(arg);
			}
			return values;
		}
		
		/**
		 * @private
		 */
		private function initArgs():void {
			if (_args == null) {
				_args = new Array();
				for each (var arg:XML in desc.arg)
					_args.push(new Arg(this, arg));
			}
		}
	}
}