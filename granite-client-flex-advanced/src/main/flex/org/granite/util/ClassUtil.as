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
package org.granite.util {

	import flash.system.ApplicationDomain;
	import flash.utils.Proxy;
	import flash.utils.describeType;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	
	import org.granite.reflect.ClassNotFoundError;

	/**
	 * Static utility methods for <code>Class</code> manipulation.
	 * 
	 * @author Franck WOLFF 
	 */
	public class ClassUtil {
		
		/**
		 * @private
		 */
		private static const TOPLEVEL_CLASSES:Array = [
			ArgumentError,
			Array,
			Boolean,
			Class,
			Date,
			DefinitionError,
			Error,
			EvalError,
			Function,
			int,
			Math,
			Namespace,
			Number,
			Object,
			QName,
			RangeError,
			ReferenceError,
			RegExp,
			SecurityError,
			String,
			SyntaxError,
			TypeError,
			uint,
			URIError,
			VerifyError,
			XML,
			XMLList
		];

		/**
		 * @private
		 */
		private static const SYSTEM_DOMAIN:ApplicationDomain = function():ApplicationDomain {
			var systemDomain:ApplicationDomain = ApplicationDomain.currentDomain;
			while (systemDomain.parentDomain != null)
				systemDomain = systemDomain.parentDomain;
			return systemDomain;
		}();
		
		/**
		 * @private
		 */
		private static const UPPER0:RegExp = /^[A-Z]/;
		
		/**
		 * The Flash VM system application domain.
		 */
		public static function get systemDomain():ApplicationDomain {
			return SYSTEM_DOMAIN;
		}

		/**
		 * Returns the fully qualified name of the supplied <code>Class</code> parameter
		 * in the form of <code>"path.to::MyClass"</code>.
		 * 
		 * @param the class whose name is to be returned.
		 * @return the fully qualified name of the supplied <code>Class</code>.
		 */
		public static function getName(clazz:Class):String {
			return getQualifiedClassName(clazz);
		}
		
		/**
		 * Returns the simple (without the package part) name of the supplied
		 * <code>Class</code> parameter.
		 * 
		 * @param the class whose name is to be returned.
		 * @return the simple name of the supplied <code>Class</code>.
		 */
		public static function getSimpleName(clazz:Class):String {
			return extractSimpleName(getName(clazz));
		}
    	
		/**
		 * Get the unqualified (simple) class name of the object passed as
		 * a parameter.
		 * 
		 * @param the object whose class name is to be returned.
		 * @return the unqualified class name of the supplied object.
		 */
    	public static function getUnqualifiedClassName(o:Object):String {
    		if (o == null)
    			return "null";
    		
    		var name:String = o is String ? String(o) : getQualifiedClassName(o);

	        var index:int = name.indexOf("::");
	        if (index != -1)
	            name = name.substr(index + 2);
	
	        return name;
    	}
		
		/**
		 * Returns the package name of the supplied <code>Class</code> parameter.
		 * 
		 * @param the class whose package name is to be returned.
		 * @return the package name of the supplied <code>Class</code>.
		 */
		public static function getPackageName(clazz:Class):String {
			return extractPackageName(getName(clazz));
		}
		
		/**
		 * Returns the simple (without the package part) name of the supplied
		 * class name parameter. The class name must be in the form of
		 * <code>"path.to::MyClass"</code>.
		 * 
		 * @param the class name used for extraction.
		 * @return the simple name of the supplied class name.
		 */
		public static function extractSimpleName(name:String):String {
			var simpleName:String = name;
			var index:int = simpleName.indexOf("::");
			if (index != -1)
				simpleName = simpleName.substr(index + 2);
			return simpleName;
		}
		
		/**
		 * Returns the package name of the supplied class name parameter. The class
		 * name must be in the form of <code>"path.to::MyClass"</code>.
		 * 
		 * @param the class name used for extraction.
		 * @return the package name of the supplied class name.
		 */
		public static function extractPackageName(name:String):String {
			var className:String = name;
			var packageName:String = '';
			var index:int = className.indexOf("::");
			if (index != -1)
				packageName = className.substr(0, index);
			return packageName;
		}
		
		/**
		 * Returns the <code>Class</code> denoted by the name parameter, in the optional application
		 * domain parameter. If domain parameter is null, this method will try to use the Flash system
		 * domain.
		 * 
		 * @param name the class name.
		 * @param domain the optional application domain to be used for getting the class definition.
		 * @return the <code>Class</code> object denoted by the name parameter (or <code>null</code>
		 * 		if the name parameter is <code>null</code>).
		 * @throws org.granite.reflect.ClassNotFoundError if no <code>Class</code> can be found in
		 * 		the supplied or system domain with this name.
		 */
		public static function forName(name:String, domain:ApplicationDomain = null):Class {
			if (name == null)
				return null;
			
			if (VectorUtil.isUnqualifiedVectorClassName(name))
				return VectorUtil.unqualifiedVectorClass;
			
			var clazz:Class = null;
			try {
				if (domain != null)
					clazz = Class(domain.getDefinition(name));
				else
					clazz = Class(SYSTEM_DOMAIN.getDefinition(name));
			}
			catch (e:ReferenceError) {
				throw new ClassNotFoundError("Class " + name + " does not exist in supplied or system domains");
			}
			return clazz;
		}

		/**
		 * Test if a class name exists in the supplied domain or in the global system domain.
		 * 
		 * @param name the class name.
		 * @param domain the optional application domain to be used for testing the class definition.
		 * @return <code>true</code> if the class name exists in the supplied domain or, if domain
		 * 		is null, in the system domain; <code>false</code> otherwise.
		 */
		public static function hasDefinition(name:String, domain:ApplicationDomain = null):Boolean {
			if (name == null)
				return false;
			
			if (VectorUtil.isUnqualifiedVectorClassName(name))
				name = "__AS3__.vec::Vector.<Object>";

			if (domain != null)
				return domain.hasDefinition(name);

			return SYSTEM_DOMAIN.hasDefinition(name);
		}

		/**
		 * Returns the <code>Class</code> of the supplied instance parameter.
		 * 
		 * <p>
		 * Notes:
		 * <ul>if the o parameter is a <code>flash.utils.Proxy</code> instance, this method will try to
		 * get its class by using the qualified class name of the instance and the supplied application
		 * domain.</ul>
		 * <ul>if the o parameter is <code>null</code>, this method returns <code>null</code>.</ul>
		 * <ul>if o is an <code>uint</code> instance, this method returns the class
		 * <code>int</code> (Flash VM bug)</ul>
		 * </p>
		 * 
		 * @param o the instance from which the <code>Class</code> is to be returned.
		 * @param domain the optional application domain to be used for getting the class definition.
		 * @return the <code>Class</code> of the supplied instance (or <code>null</code>
		 * 		if the o parameter is <code>null</code>).
		 * @throws ArgumentError if o is a function or a class.
		 * @throws org.granite.reflect.ClassNotFoundError if no <code>Class</code> can be found in
		 * 		the supplied or system domain with this the given object class name.
		 */
		public static function forInstance(o:*, domain:ApplicationDomain = null):Class {
			if (o == null || o == undefined)
				return null;
			
			if (o is Function || o is Class)
				throw new ArgumentError("Cannot get the class of a function or class instance");
			
			var clazz:Class;

			if (o is int) // With o:int or o:uint, (o is int) and (o is uint) are both true...
				clazz = int;
			else if (o is Proxy)
				clazz = forName(getQualifiedClassName(o), domain);
			else
				clazz = o.constructor;
			
			return clazz;
		}
		
		/**
		 * This method returns the XML description of the supplied class parameter.
		 * 
		 * <p>
		 * It actually just calls the <code>flash.utils.describeType</code> builtin
		 * method (<code>mx.utils.DescribeTypeCache</code> is buggy and its caching
		 * startegy is unsuitable for multiple application domain support).
		 * </p>
		 * 
		 * @param clazz the class from which the XML description is to be returned.
		 * @return the XML description of the class.
		 * 
		 * @see flash.utils.describeType
		 */
		public static function describeClass(clazz:Class):XML {
			/*
			DescribeTypeCache not only has an annoying bug with instance/class descriptions
			but is also caching descriptions with class name keys: this is not suitable for
			different classes with the same name loaded from different ApplicationDomains.
			
			var record:DescribeTypeCacheRecord = DescribeTypeCache.describeType(clazz);
			if (record.typeDescription.@isStatic == "true")
				return record.typeDescription;
			*/
			return describeType(clazz);
		}
		
		/**
		 * Tests if the supplied object class is annotated with a given annotation name.
		 * 
		 * @param o the object (may be a class) to test.
		 * @param annotationName the annotation name to look for.
		 * @return <code>true</code> if the object is annotated with the given annotation
		 * 		name, <code>false</code> otherwise.
		 */
		public static function isAnnotatedWith(o:*, annotationName:String):Boolean {
			var desc:XML = describeType(o);
			if (o is Class)
				return desc.factory.metadata.(@name == annotationName).length() > 0;
			return desc.metadata.(@name == annotationName).length() > 0;
		}
		
		/**
		 * Split a name into a namespace prefix and a value. Returns an array of two
		 * Strings, the first one containing the prefix (possibly <code>null</code>
		 * or empty), the second one containing the associated value.
		 * 
		 * <listing>
		 * ClassUtil.parseQName("path.to.MyClass"); // [null, "path.to.MyClass"]
		 * ClassUtil.parseQName("path.to::MyClass"); // [null, "path.to::MyClass"]
		 * ClassUtil.parseQName("::MyClass"); // [null, "::MyClass"]
		 * ClassUtil.parseQName(":MyClass"); // ["", "MyClass"]
		 * ClassUtil.parseQName("MyClass"); // ["", "MyClass"]
		 * ClassUtil.parseQName("MyClass.Inner"); // ["", "MyClass.Inner"]
		 * ClassUtil.parseQName("myClass.Inner"); // [null, "myClass.Inner"]
		 * ClassUtil.parseQName("MyClass", false)); // ["", "MyClass"]
		 * ClassUtil.parseQName("MyClass.Inner", false); // [null, "MyClass.Inner"]
		 * ClassUtil.parseQName("myClass.Inner", false); // [null, "myClass.Inner"]
		 * ClassUtil.parseQName("abc:MyClass"); // ["abc", "MyClass"]
		 * ClassUtil.parseQName("ABC:MyClass"); // ["ABC", "MyClass"]</listing>
		 * 
		 * @param name the name to be parsed for namespace prefix and value.
		 * @param upperPolicy should we consider a String without any ':' character but
		 * 		at least one '.' character and beginning by an uppercase letter as a
		 * 		qualified name in the default namespace?
		 * @return an array of two Strings, the first one containing the prefix (possibly
		 * 		<code>null</code> or empty), the second one containing the associated value.
		 */
		public static function parseQName(name:String, upperPolicy:Boolean = true):Array {

			if (name != null && name.length > 0 && name.indexOf("::") == -1) {
				const iColon:int = name.indexOf(':');
				
				if (iColon != -1)
					return [name.substring(0, iColon), name.substring(iColon + 1)];
				
				if (name.indexOf('.') == -1 || (upperPolicy && UPPER0.test(name)))
					return ["", name];
			}
			
			return [null, name];
		}
		
		/**
		 * Tests if the supplied o parameter is an instance of the Flash 10 Vector class. This
		 * method doesn't use directly the Vector class, so it is compatible with earlier
		 * Flash versions.
		 * 
		 * @param o the instance to test.
		 * @return <code>true</code> if o is a Vector instance, <code>false</code> otherwise.
		 */
		public static function isVector(o:*):Boolean {
			return (getQualifiedClassName(o).indexOf("__AS3__.vec::Vector.<") == 0);
		}
		
		/**
		 * Tests if the supplied clazz parameter is a <i>Top Level</i> class defined in the
		 * Flash 10 VM (including <code>Vector</code>).
		 * 
		 * @param clazz the classe to be tested.
		 * @return <code>true</code> if clazz is a top level class, <code>false</code> otherwise.
		 */
		public static function isTopLevel(clazz:Class):Boolean {
			return TOPLEVEL_CLASSES.indexOf(clazz) != -1 || isVector(clazz);
		}
		
		/**
		 * Tests if the supplied clazz parameter is a <i>Flash</i> class, ie. defined in the
		 * <code>flash</code> or <code>air</code> package or any of their subpackages.
		 * 
		 * @param clazz the classe to be tested.
		 * @return <code>true</code> if clazz is a Flash class, <code>false</code> otherwise.
		 */
		public static function isFlashClass(clazz:Class):Boolean {
			var name:String = getQualifiedClassName(clazz);
			return name.indexOf("flash.") == 0 || name.indexOf("air.") == 0;
		}
		
		/**
		 * Tests if the supplied clazz parameter is a <i>Flex</i> class, ie. defined in the
		 * <code>mx</code> package or any of its subpackages.
		 * 
		 * @param clazz the classe to be tested.
		 * @return <code>true</code> if clazz is a Flex class, <code>false</code> otherwise.
		 */
		public static function isMxClass(clazz:Class):Boolean {
			return getQualifiedClassName(clazz).indexOf("mx.") == 0;
		}
		
		/**
		 * Tests if the supplied clazz parameter is a GraniteDS <i> Math</i> class, ie. in the
		 * <code>org.granite.math</code> package or any of its subpackages.
		 * 
		 * @param clazz the classe to be tested.
		 * @return <code>true</code> if clazz is a big number class, <code>false</code> otherwise.
		 */
		public static function isMathClass(clazz:Class):Boolean {
			return getQualifiedClassName(clazz).indexOf("org.granite.math.") == 0;
		}
	}
}