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
package org.granite.reflect {

	/**
	 * Represents the constructor of a given <code>Type</code>.
	 * 
	 * <p>
	 * Note: even if this class implements the <code>IAnnotatedElement</code> interface,
	 * annotations are not supported on ActionScript 3 constructors (language limitation)
	 * or not returned by <code>flash.utils.describeType</code> calls (reflection API limitation).
	 * All <code>IAnnotatedElement</code> methods will return empty, null or false results.
	 * </p>
	 * 
	 * @author Franck WOLFF
	 */
	public class Constructor extends Parameterizable {

		/**
		 * Constructs a new <code>Constructor</code> instance.
		 * 
		 * @param declaredBy the <code>Type</code> instance this constructor belongs to.
		 * @param desc the XML description of the constructor.
		 */
		function Constructor(declaredBy:Type, desc:XML) {
			super(declaredBy, desc);
		}
		
		/**
		 * The name of this constructor (always equal to the unqualified class name)
		 */
		override public function get name():String {
			return declaredBy.simpleName;
		}
		
		/**
		 * The uri of this constructor (always "" because constructors cannot
		 * use specific namespaces).
		 */
		override public function get uri():String {
			return "";
		}
		
		/**
		 * @private
		 */
		override protected function initModifiers():uint {
			return CONSTRUCTOR;
		}
		
		/**
		 * Returns a new instance of this constructor's declaring class.
		 * 
		 * <p>
		 * This is just a utility method, limited to 10 maximum arguments. It may be simpler
		 * and more powerful to call directly the constructor the following way:
		 * </p>
		 * 
		 * <listing>
		 * var myInstance:MyClass = new myType.getClass()(...);</listing>
		 * 
		 * @param args a variable list of arguments to pass to the constructor call.
		 * @return the new instance of this constructor's declaring class.
		 * @throws ArgumentError if supplied arguments are not fulfilling the class
		 * 		constructor requirements (count or type) or if an array of more than 10 arguments
		 * 		is passed to this method.
		 * @throws org.granite.reflect.InvocationTargetError if the constructor has thrown an
		 * 		error.
		 */
		public function newInstance(... args):* {
			return newInstanceWithArray(args);
		}
		
		
		/**
		 * Returns a new instance of this constructor's declaring class.
		 * 
		 * <p>
		 * This is just a utility method, limited to 10 maximum arguments. It may be simpler
		 * and more powerful to call directly the constructor the following way:
		 * </p>
		 * 
		 * <listing>
		 * var myInstance:MyClass = new myType.getClass()(...);</listing>
		 * 
		 * @param args an array of arguments to pass to the constructor call.
		 * @return the new instance of this constructor's declaring class.
		 * @throws ArgumentError if supplied arguments are not fulfilling the class
		 * 		constructor requirements (count or type) or if an array of more than 10 arguments
		 * 		is passed to this method.
		 * @throws org.granite.reflect.InvocationTargetError if the constructor has thrown an
		 * 		error.
		 */
		public function newInstanceWithArray(args:Array):* {
			if (args == null)
				args = [];

			var clazz:Class = declaredBy.getClass();

			if (args.length > 10)
				throw new ArgumentError("Constructor with more than 10 arguments are not supported");
			
			var error:Error = null;
			try {
				switch (args.length) {
					case 0:
						return new clazz();
					case 1:
						return new clazz(args[0]);
					case 2:
						return new clazz(args[0], args[1]);
					case 3:
						return new clazz(args[0], args[1], args[2]);
					case 4:
						return new clazz(args[0], args[1], args[2], args[3]);
					case 5:
						return new clazz(args[0], args[1], args[2], args[3], args[4]);
					case 6:
						return new clazz(args[0], args[1], args[2], args[3], args[4], args[5]);
					case 7:
						return new clazz(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
					case 8:
						return new clazz(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
					case 9:
						return new clazz(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
					case 10:
						return new clazz(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
				}
			}
			catch (e:ArgumentError) {
				// If the second line of the stack trace contains a Function.apply() error, the
				// ArgumentError comes from the "new clazz(...)" call, and not from the underlying
				// constructor body...
				var st:Array = e.getStackTrace().split(/\n/, 2);
				if (st.length < 2 || st[1] == "\tat Function/http://adobe.com/AS3/2006/builtin::apply()")	
					throw e;
				
				error = e;
			}
			catch (e:Error) {
				error = e;
			}
			
			if (error != null)
				throw new InvocationTargetError(error, "Constructor has thrown an error");
		}
		
		/**
		 * @inheritDoc
		 */
		override public function equals(o:*):Boolean {
			if (o === this)
				return true;
			if (!(o is Constructor))
				return false;
			return declaredBy.equals((o as Constructor).declaredBy);
		}
	}
}