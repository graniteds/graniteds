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

	import org.granite.util.ClassUtil;

	/**
	 * Represents a method of an ActionScript 3 class.
	 * 
	 * @author Franck WOLFF
	 */
	public class Method extends Parameterizable {
		
		/**
		 * @private
		 */
		private var _annotations:Array;

		/**
		 * Constructs a new <code>Method</code> instance.
		 * 
		 * <p>
		 * The declaredBy parameter is adjusted in order to match the actual type that
		 * declares this method by using "declaredBy" attribute of its XML description.
		 * Hence, the returned value of the <code>Method.declaredBy</code>
		 * accessor may not return the declaredBy parameter value, but one of its
		 * superclasses.
		 * </p>
		 * 
		 * @param declaredBy the <code>Type</code> instance this method belongs to.
		 * @param desc the XML description of the method.
		 */
		function Method(declaredBy:Type, desc:XML) {
			super(Type.forName(desc.@declaredBy, declaredBy.domain), desc);
		}
		
		/**
		 * The name of this method.
		 */
		override public function get name():String {
			return desc.@name;
		}
		
		/**
		 * The namespace's uri of this method ("" for public methods).
		 */
		override public function get uri():String {
			var uri:String = desc.@uri;
			// Descriptions of interfaces methods have uris set to the qualified
			// name of the interface (with a single ':' instead of the usual double '::').
			// Just ignore them.
			if (uri == "" || declaredBy.isInterface())
				return "";
			return uri;
		}
		
		/**
		 * The returned type of this method (may be <code>Type.VOID</code> or
		 * <code>Type.WILD</code>.
		 * 
		 * @see Type#VOID
		 * @see Type#WILD
		 */
		public function get returnType():Type {
			return Type.forName(desc.@returnType, declaredBy.domain);
		}
		
		/**
		 * @private
		 */
		override protected function initModifiers():uint {
			var modifiers:uint = METHOD;
			
			var parent:* = desc.parent();
			if (parent is XML && XML(parent).name() == "type")
				modifiers |= STATIC;
			
			return modifiers; 
		}
		
		/**
		 * @inheritDoc
		 */
		override public function getAnnotations(recursive:Boolean = false, pattern:String = "^_?[^_]"):Array {
			initAnnotations();
			
			if (recursive && pattern == null)
				return _annotations.concat();
			
			const re:RegExp = (pattern == null ? null : new RegExp(pattern));

			return _annotations.filter(
				function(a:Annotation, index:int, array:Array):Boolean {
					if (!recursive && a.annotatedElement !== this)
						return false;
					if (re != null && a.name.search(re) == -1)
						return false;
					return true;
				},
				this
			);
		}
		
		/**
		 * @private
		 */
		public function getAnnotationNoCache(type:String):Annotation {
			var annotations:XMLList = desc.metadata.(@name == type);
			if (annotations.length() > 0)
				return new Annotation(this, annotations[0]);
			return null;
		}
		
		/**
		 * @private
		 */
		public function getAnnotationsNoCache(type:String):Array {
			var annotations:XMLList = desc.metadata.(@name == type);
			if (annotations.length() == 0)
				return [];
			var arr:Array = [];
			for each (var anno:XML in annotations)
				arr.push(new Annotation(this, anno));
			return arr; 
		}
		
		/**
		 * @inheritDoc
		 */
		override public function getAnnotation(type:String, recursive:Boolean = false):Annotation {
			initAnnotations();
			
			for each (var a:Annotation in _annotations) {
				if (!recursive && a.annotatedElement !== this)
					break;
				if (a.name == type)
					return a;
			}
			
			return null;
		}
		
		/**
		 * @inheritDoc
		 */
		override public function isAnnotationPresent(type:String, recursive:Boolean = false):Boolean {
			return getAnnotation(type, recursive) != null;
		}
		
		/**
		 * @private
		 */
		private function initAnnotations():void {
			if (_annotations == null) {
				_annotations = new Array();
				
				for each (var meta:XML in desc.metadata)
					_annotations.push(new Annotation(this, meta));
				
				if (!isStatic()) {
					for each (var superclass:Type in declaredBy.superclasses) {
						var m:Method = getSuperMethod(superclass);
						if (m != null) {
							m.initAnnotations();
							for each (var a:Annotation in m._annotations) {
								if (a.annotatedElement !== m)
									break;
								_annotations.push(a);
							}
						}
					}
					for each (var interfaze:Type in declaredBy.interfaces) {
						m = getSuperMethod(interfaze);
						if (m != null) {
							m.initAnnotations();
							for each (a in m._annotations) {
								if (a.annotatedElement !== m)
									break;
								_annotations.push(a);
							}
						}
					}
				}
			}
		}
		
		/**
		 * Invokes the underlying method represented by this <code>Method</code> object, on the
		 * specified instance with the specified parameters.
		 * 
		 * @param instance the object the underlying method is invoked from (may be <code>null</code>
		 * 		for static methods).
		 * @param args a variable list of arguments used for the method call.
		 * @return the result of this method call or <code>undefined</code> undefined if the
		 * 		underlying method return type is <code>void</code>.
		 * @throws ArgumentError if the instance parameter is not an instance of the class that
		 * 		declares this method, if the method is not static and the instance parameter is
		 * 		<code>null</code>, and whenever supplied argument count or types are not suitable.
		 * @throws org.granite.reflect.InvocationTargetError if the underlying function throws
		 * 		an error.
		 */
		public function invoke(instance:Object, ... args):* {
			return invokeWithArray(instance, args);
		}
		
		/**
		 * Invokes the underlying method represented by this <code>Method</code> object, on the
		 * specified instance with the specified parameters.
		 * 
		 * @param instance the object the underlying method is invoked from (may be <code>null</code>
		 * 		for static methods).
		 * @param args an array of arguments used for the method call.
		 * @return the result of this method call or <code>undefined</code> undefined if the
		 * 		underlying method return type is <code>void</code>.
		 * @throws ArgumentError if the instance parameter is not an instance of the class that
		 * 		declares this method, if the method is not static and the instance parameter is
		 * 		<code>null</code>, and whenever supplied argument count or types are not suitable.
		 * @throws org.granite.reflect.InvocationTargetError if the underlying function throws
		 * 		an error.
		 */
		public function invokeWithArray(instance:Object, args:Array):* {
			var clazz:Class = Type.forName(desc.@declaredBy.toString(), declaredBy.domain).getClass();
			
			if (instance != null && !(instance is clazz))
				throw new ArgumentError("Instance parameter isn't an instance of " + ClassUtil.getName(clazz));
			
			if (!isStatic() && instance == null) {
				throw new ArgumentError(
					"Cannot invoke the non static method " + qName +
					" without an instance of " + ClassUtil.getName(clazz)
				);
			}

			var func:Function;
			if (instance != null)
				func = (instance[qName] as Function);
			else
				func = (clazz[qName] as Function);

			var result:* = null;
			var error:Error = null;
			
			try {
				result = func.apply(instance, args);
			}
			catch (e:ArgumentError) {
				// If the second line of the stack trace contains a Function.apply() error, the
				// ArgumentError comes from the "func.apply(instance, args)" call, and not from
				// the underlying method body...
				var st:Array = e.getStackTrace().split(/\n/, 2);
				if (st.length < 2 || st[1] == "\tat Function/http://adobe.com/AS3/2006/builtin::apply()")	
					throw e;
				
				error = e;
			}
			catch (e:Error) {
				error = e;
			}
			
			if (error != null)
				throw new InvocationTargetError(error, "Method " + qName + " has thrown an error");
			
			return result;
		}
		
		/**
		 * @private
		 */
		private function getSuperMethod(type:Type):Method {
			if (!isStatic())
				return type.getInstanceMethod(name, ns);
			return null;
		}
	}
}