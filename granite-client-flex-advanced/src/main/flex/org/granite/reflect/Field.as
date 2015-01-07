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
package org.granite.reflect {

	/**
	 * A <code>Field</code> instance represents either a constant, a variable or an
	 * accessor (get/set), static or not, of a given ActionScript 3 class (only accessors
	 * for interfaces).
	 * 
	 * @see Type#fields
	 * 
	 * @author Franck WOLFF 
	 */
	public class Field extends Member implements IVisitableElement {
		
		/**
		 * @private
		 */
		private var _annotations:Array;
		
		/**
		 * Constructs a new <code>Field</code> instance.
		 * 
		 * <p>
		 * The declaredBy parameter is adjusted in order to match the actual type that
		 * declares this field, by introspecting superclasses (or superinterfaces for
		 * interface types). Hence, the returned value of the <code>Field.declaredBy</code>
		 * accessor may not return the declaredBy parameter value, but one of its
		 * superclasses.
		 * </p>
		 * 
		 * @param annotatedElement the <code>Type</code> that declares this field.
		 * @param desc the XML description of this annotation.
		 */
		function Field(declaredBy:Type, desc:XML) {
			super(initDeclaredBy(declaredBy, desc), desc);
		}

		/**
		 * The name of this <code>Field</code> instance, ie. the name of the underlying
		 * constant, variable or accessor.
		 */
		override public function get name():String {
			return desc.@name;
		}
		
		/**
		 * The namespace's uri of this field ("" for public fields).
		 */
		override public function get uri():String {
			var uri:String = desc.@uri;
			// Descriptions of interfaces accessors have uris set to the qualified
			// name of the interface (with a single ':' instead of the usual double '::').
			// Just ignore them.
			if (uri == "" || (isAccessor() && declaredBy.isInterface()))
				return "";
			return uri;
		}
		
		/**
		 * The type of this <code>Field</code> instance, ie. the type of the underlying
		 * constant, variable or accessor.
		 */
		public function get type():Type {
			return Type.forName(desc.@type, declaredBy.domain);
		}
		
		/**
		 * Returns the value of the field represented by this Field, on the specified instance.
		 * 
		 * @param instance object from which the represented field's value is to be extracted. This
		 * 		parameter may be <code>null</code> if this field represents a static member.
		 * @return the value of the represented field in the instance parameter.
		 * @throws org.granite.reflect.IllegalAccessError if this field is not readable.
		 * @throws ArgumentError if this field is an instance field and the supplied instance
		 * 		parameter is <code>null</code>, or if the instance parameter is not an instance of
		 * 		this field declaring class.
		 */
		public function getValue(instance:*):* {
			
			if (!isReadable())
				throw new IllegalAccessError("Field " + qName + " is not readable");
			
			if (!isStatic() && instance == null)
				throw new ArgumentError("Cannot get the non-static field " + qName + " value with a null instance");

			if (instance == null)
				return declaredBy.getClass()[qName];

			if (!(instance is declaredBy.getClass()))
				throw new ArgumentError("Instance parameter is not an instance of the " + qName + " field declaring class");

			return instance[qName];
		}

		/**
		 * Sets the field represented by this Field object on the specified instance argument to
		 * the specified new value.
		 * 
		 * @param instance the object whose field should be modified. This parameter may be
		 * 		<code>null</code> if this field represents a static member.
		 * @param value the new value for the field of instance being modified.
		 * @throws org.granite.reflect.IllegalAccessError if this field is not writeable.
		 * @throws ArgumentError if this field is an instance field and the supplied instance
		 * 		parameter is <code>null</code>, if the instance parameter is not an instance of
		 * 		this field declaring class, or if the supplied value cannot be converted to
		 * 		this field type.
		 */
		public function setValue(instance:*, value:*):void {

			if (!isWriteable())
				throw new IllegalAccessError("Field " + qName + " is not writeable");
			
			if (!isStatic() && instance == null)
				throw new ArgumentError("Cannot set the non-static field " + qName + " value with a null instance");
			
			if (instance == null)
				declaredBy.getClass()[qName] = value;
			else {
				if (!(instance is declaredBy.getClass()))
					throw new ArgumentError("Instance parameter is not an instance of the " + qName + " field declaring class");
				instance[qName] = value;
			}
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
		 * @private
		 */
		public function getAnnotationNoCache(type:String):Annotation {
			var annotations:XMLList = desc.metadata.(@name == type);
			if (annotations.length() > 0)
				return new Annotation(this, annotations[0]);
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
				
				if (isAccessor() && !isStatic()) {
					for each (var superclass:Type in declaredBy.superclasses) {
						var f:Field = getSuperField(superclass);
						if (f != null) {
							f.initAnnotations();
							for each (var a:Annotation in f._annotations) {
								if (a.annotatedElement !== f)
									break;
								_annotations.push(a);
							}
						}
					}
					for each (var interfaze:Type in declaredBy.interfaces) {
						f = getSuperField(interfaze);
						if (f != null) {
							f.initAnnotations();
							for each (a in f._annotations) {
								if (a.annotatedElement !== f)
									break;
								_annotations.push(a);
							}
						}
					}
				}
			}
		}
		
		/**
		 * @private
		 */
		override protected function initModifiers():uint {
			return getModifiers(desc);
		}
		
		/**
		 * @private
		 */
		private function getSuperField(type:Type):Field {
			if (!isStatic())
				return type.getInstanceField(name, ns);
			return null;
		}
		
		/**
		 * @private
		 */
		private static function initDeclaredBy(declaredBy:Type, desc:XML):Type {
			
			// Static or instance accessors: just use the @declaredBy attribute.
			if (desc.@declaredBy != undefined)
				return Type.forName(desc.@declaredBy, declaredBy.domain);
			
			var fname:String = desc.@name;
			var qName:QName = new QName(desc.@uri.toString(), fname);
			var modifiers:uint = getModifiers(desc);
			
			// We should only have constants and variables here (accessors must have a
			// @declaredBy attribute).
			if ((modifiers & (CONSTANT | VARIABLE)) == 0)
				throw new ReflectionError("Could not find declaring class for field: " + fname);
			
			var declaringType:Type = declaredBy;

			// Static constants or variables: returns declaredBy because XML desc doesn't
			// contain any superclass static field.
			if ((modifiers & STATIC) != 0)
				return declaringType;
			
			// Instance constants or variables: returns the first found class (by iterating on
			// declaredBy superclasses) that declares this field. Use the fact that the AS3
			// compiler doesn't allow instance variable or constant overriding.
			for each (var superclass:Type in declaredBy.superclasses) {
				var superDesc:XMLList;
				
				if (desc.name() == "constant")
					superDesc = superclass.desc.factory.constant.(@name == fname);
				else
					superDesc = superclass.desc.factory.variable.(@name == fname);

				// Only one result is possible (cannot have multiple non static constant/variable with
				// the same name).
				if (superDesc.length() > 0)
					declaringType = superclass;
			}
			
			return declaringType;
		}
		
		/**
		 * @private
		 */
		private static function getModifiers(desc:XML):uint {
			var modifiers:uint = 0;
			
			var parent:* = desc.parent();
			if (parent is XML && (parent as XML).name() == "type")
				modifiers |= STATIC;

			switch (desc.name().toString()) {
				case "constant":
					modifiers |= CONSTANT | READABLE;
					break;
				case "variable":
					modifiers |= VARIABLE | READABLE | WRITABLE;
					break;
				case "accessor":
					modifiers |= ACCESSOR;
					switch (desc.@access.toString()) {
						case "readonly":
							modifiers |= READABLE;
							break;
						case "writeonly":
							modifiers |= WRITABLE;
							break;
						case "readwrite":
							modifiers |= READABLE | WRITABLE;
							break;
						default:
							throw new ReflectionError("Unknown field access: " + desc.@access.toString());
					}
					break;
				default:
					throw new ReflectionError("Unknown field name: " + desc.name());
			}
			
			return modifiers;
		}
	}
}