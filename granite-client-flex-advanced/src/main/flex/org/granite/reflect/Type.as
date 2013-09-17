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

package org.granite.reflect {

	import flash.system.ApplicationDomain;
	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;
	
	import org.granite.util.ClassUtil;

	/**
	 * The <code>Type</code> class is the entry point to the GraniteDS reflection API for
	 * ActionScript 3 classes. <code>Type</code> instances (with their underlying XML
	 * descriptions) are cached, so subsequent calls for retrieving a known type are
	 * faster. 
	 * 
	 * <p>
	 * From any <code>Type</code> instance, you may retrieve:
	 * 
	 * <ul>superclasses and implemented interfaces (or superinterfaces for interface),</ul>
	 * <ul>constructor,</ul>
	 * <ul>constants,</ul>
	 * <ul>variables,</ul>
	 * <ul>accessors (get/set),</ul>
	 * <ul>methods,</ul>
	 * <ul>annotations (metadata).</ul>
	 * 
	 * Constructors, constants, variables, accessors and methods are represented by three
	 * <code>Member</code> child classes: <code>Constructor</code>, <code>Field</code>
	 * (for constants, variables and accessors) and <code>Method</code>. The overall API
	 * tries to look much alike the Java reflection API.
	 * </p>
	 * 
	 * <p>
	 * For returned members (constructor, methods, fields), the following rules apply:
	 * 
	 * <ol><b>Constructor</b>: only one constructor is returned, the one declared (explicitly or or)
	 * by this <code>Type</code> class.</ol>
	 * 
	 * <ol><b>Methods</b>: all <i>instance</i> methods are returned, either declared by this
	 * <code>Type</code> class or by any of its superclasses (overridden methods only appear once).
	 * Only <i>static</i> methods directly declared by this <code>Type</code> class are returned.</ol>
	 * 
	 * <ol><b>Fields</b>: all <i>instance</i> fields (constants, variables and accessors) are returned,
	 * either declared by this <code>Type</code> class or by any of its superclasses (overridden
	 * accessors only appear once, variables and constants cannot be overridden). Only
	 * <i>static</i> fields directly declared by this <code>Type</code> class are returned.</ol>
	 * </p>
	 * 
	 * <p>
	 * This API brings also <code>Namespace</code> and <code>ApplicationDomain</code> support: all types
	 * are bound to an <code>ApplicationDomain</code>, that can be the Flash system domain or a dedicated
	 * domain created when loading SWF module classes. All type's members are also bound to a namespace
	 * (default is the public namespace) and may be searched according to their namespace.
	 * </p>
	 * 
	 * <p>
	 * To get a <code>Type</code> instance, use one of the following static methods:
	 * </p>
	 * 
	 * <listing>
	 * var type:Type = Type.forName("path.to::MyClass"); // or Type.forName("path.to.MyClass");
	 * var type:Type = Type.forInstance(new MyClass());
	 * var type:Type = Type.forClass(MyClass);</listing>
	 * 
	 * @see flash.system.ApplicationDomain
	 * @see Member
	 * @see Constructor
	 * @see Field
	 * @see Method
	 * 
	 * @author Franck WOLFF
	 */
	public class Type extends DescribedElement implements IAnnotatedElement, IVisitableElement {
		
		///////////////////////////////////////////////////////////////////////
		// Static
		
		/**
		 * @private
		 */
		private static const _typeCache:Dictionary = new Dictionary(true);
		
		/**
		 * @private
		 */
		private static const _registeredDomains:Dictionary = new Dictionary(true);
		
		/**
		 * Builtin type for the pseudo-class "void".
		 */
		public static const VOID:Type = new Type(
			new XML("<type name='void'/>"),
			null,
			ClassUtil.systemDomain
		);

		/**
		 * Builtin type for the pseudo-class "~~" (wildcard).
		 */
		public static const WILD:Type = new Type(
			new XML("<type name='*'/>"),
			null,
			ClassUtil.systemDomain
		);
		
		/**
		 * Returns a <code>Type</code> instance for the class denoted by the name parameter, in the optional
		 * application domain. If domain parameter is null, this method will try to find a registered domain
		 * where the class is defined (including the Flash system domain). If several classes with this name
		 * are defined in different application domain, this method will throw a
		 * <code>AmbiguousClassNameError</code>.
		 * 
		 * @param name the class name.
		 * @param domain the optional application domain to be used for getting the class definition.
		 * @return a Type instance describing the class denoted by the name parameter (or <code>null</code>
		 * 		if the name parameter is <code>null</code>).
		 * @throws org.granite.reflect.ClassNotFoundError if no <code>Class</code> can be found in the
		 * 		supplied or system domain.
		 * @throws org.granite.reflect.AmbiguousClassNameError if the class is defined in more than one
		 * 		application domain.
		 * 
		 * @see #forClass
		 * @see org.granite.util.ClassUtil#parseQName
		 */
		public static function forName(name:String, domain:ApplicationDomain = null):Type {
			if (name == null)
				return null;
			
			if (name == "void")
				return VOID;
			if (name == "*")
				return WILD;

			var clazz:Class = null;
			
			if (domain != null)
				clazz = ClassUtil.forName(name, domain);
			else {
				if (ClassUtil.hasDefinition(name)) {
					clazz = ClassUtil.forName(name);
					domain = ClassUtil.systemDomain;
				}
				for (var key:Object in _registeredDomains) {
					var registeredDomain:ApplicationDomain = ApplicationDomain(key);
					if (ClassUtil.hasDefinition(name, registeredDomain)) {
						if (clazz != null)
							throw new AmbiguousClassNameError("Class " + name + " exists in several application domains");
						clazz = ClassUtil.forName(name, registeredDomain);
						domain = registeredDomain;
					}
				}
				if (clazz == null)
					throw new ClassNotFoundError("Class " + name + " does not exist in system or registered domains");
			}
			
			return forClass(clazz, domain);
		}
		
		/**
		 * Returns <code>Type</code> instance for the supplied o instance parameter, in the optional
		 * application domain. If domain parameter is null and if o is a proxy, this method will try
		 * to find a registered domain where the class is defined (including the Flash system domain),
		 * by using the <code>forName</code> method.
		 * 
		 * <p>
		 * Known bug: if o is a uint instance, the returned Type will be a descriptor
		 * of a signed interger (int).
		 * </p>
		 * 
		 * @param o the instance to introspect.
		 * @param domain the optional application domain to which this object <code>Class</code>
		 * 		belongs (useful for <code>flash.utils.Proxy</code> instances).
		 * @return a Type instance describing the class of the o parameter (or <code>null</code>
		 * 		if the o parameter is <code>null</code>).
		 * @throws org.granite.reflect.ClassNotFoundError if the <code>Class</code> cannot be found
		 * 		in the supplied domain or, if domain parameter is <code>null</code>, in the system
		 * 		domain or in any registered domain.
		 * @throws org.granite.reflect.AmbiguousClassNameError if the class is defined in more than one
		 * 		application domain (<code>flash.utils.Proxy</code> instances only).
		 * 
		 * @see #forName
		 * @see #forClass
		 * @see #registerDomain
		 */
		public static function forInstance(o:*, domain:ApplicationDomain = null):Type {
			if (o == null || o == undefined)
				return null;
			
			try {
				return forClass(ClassUtil.forInstance(o, domain), domain);
			}
			catch (e:Error) {
				// Fallback...
			}
			return forName(getQualifiedClassName(o), domain);
		}
		
		
		/**
		 * Returns a <code>Type</code> instance for the supplied clazz parameter, in the optional
		 * application domain. The returned <code>Type</code> instance, if not already cached, is
		 * put in a static cache so subsequent calls to this method with the same class parameter
		 * may return a cached instance.
		 * 
		 * <p>
		 * If the domain parameter is not <code>null</code>, this method will verify if the supplied
		 * class actually belongs to this domain. Otherwise, if domain is <code>null</code>, it will
		 * try to find the class domain among all registered application domain and the Flash system
		 * domain.
		 * </p>
		 * 
		 * <p>
		 * When a domain for the <code>Class</code> parameter is found, this method tries to figure
		 * out if any parent domain of the found domain contains the class definition. The new
		 * <code>Type</code> is then associated with the top-most parent application domain that
		 * defines the class (not necessarily the supplied domain) and this domain is used in order
		 * to load any dependant <code>Type</code> (superclasses, interfaces, members classes). This
		 * domain is then automatically registered (see the <code>Type.registerDomain()</code>
		 * method) if it wasn't already known. The domain of the returned <code>Type</code> may be
		 * retreived by the <code>Type.domain</code> property.
		 * </p>
		 * 
		 * @param clazz the <code>Class</code> to introspect.
		 * @param domain the optional application domain to which this <code>Class</code>
		 * 		belongs.
		 * @return a Type instance describing the clazz Class parameter (or <code>null</code>
		 * 		if the clazz parameter is <code>null</code>).
		 * @throws org.granite.reflect.ClassNotFoundError if the <code>Class</code> does not belong
		 * 		to the supplied application domain or to any registered or system domain.
		 * 
		 * @see #registerDomain
		 * @see #domain
		 */
		public static function forClass(clazz:Class, domain:ApplicationDomain = null):Type {
			
			if (clazz == null)
				return null;
			
			// Try to find an already cached Type for the class. 
			var type:Type = _typeCache[clazz];

			// If this is an unknown class...
			if (type == null) {
				
				var name:String = ClassUtil.getName(clazz);
				
				// If the domain parameter is null, we try to find the class domain among the system
				// domain and all registered domains. 
				if (domain == null) {
					if (ClassUtil.hasDefinition(name) && ClassUtil.forName(name) === clazz)
						domain = ClassUtil.systemDomain;
					else {
						for (var key:Object in _registeredDomains) {
							var registeredDomain:ApplicationDomain = ApplicationDomain(key);
							if (ClassUtil.hasDefinition(name, registeredDomain) && ClassUtil.forName(name, registeredDomain) === clazz) {
								domain = registeredDomain;
								break;
							}
						}
					}
					if (domain == null)
						throw new ClassNotFoundError("Could not find class " + name + " in any application domain (system or registered)");
				}
				// If not, we verify if the domain actually contains the class definition.
				else if (!ClassUtil.hasDefinition(name, domain) || ClassUtil.forName(name, domain) !== clazz)
					throw new ClassNotFoundError("Class " + name + " does not exist in supplied domain");

				// We then try to find the top-most parent application domain that defines the class.
				while (domain.parentDomain != null && ClassUtil.hasDefinition(name, domain.parentDomain))
					domain = domain.parentDomain;
				
				// If the domain is not the system application domain, we try to register it and
				// we associate the class with the this application domain.
				if (domain !== ClassUtil.systemDomain) {
					registerDomain(domain);
					var domainClasses:Array = _registeredDomains[domain];
					domainClasses.push(clazz);
				}
				
				// We finally create the new Type instance and cache it.
				type = new Type(ClassUtil.describeClass(clazz), clazz, domain);
				_typeCache[clazz] = type;
			}

			return type;
		}
		
		/**
		 * Registers an application domain so objects or classes that belong to this
		 * domain, when passed to the <code>Type.forInstance(...)</code> or the
		 * <code>Type.forClass(...)</code> methods without specifying a domain, may
		 * be associated this domain.
		 * 
		 * @param domain the <code>ApplicationDomain</code> to register. If the domain
		 * 		is the Flash system domain or if it is already registered, this method
		 * 		has no effect and returns <code>false</code>.
		 * @return <code>true</code> if the domain has been registered, <code>false</code>
		 * 		otherwise.
		 */
		public static function registerDomain(domain:ApplicationDomain):Boolean {
			if (domain !== ClassUtil.systemDomain && _registeredDomains[domain] == null) {
				_registeredDomains[domain] = [];
				return true;
			}
			return false;
		}
		
		/**
		 * Unregisters an application domain and all its children domains. Additionally,
		 * all cached <code>Type</code> instances associated with the domain parameter
		 * will be evicted from the cache.
		 * 
		 * @param domain the <code>ApplicationDomain</code> to unregister. If the domain
		 * 		is the Flash system domain or if it was not registered, this method
		 * 		has no effect and returns <code>false</code>.
		 * @return <code>true</code> if the domain has been unregistered, <code>false</code>
		 * 		otherwise (system domain or already registered).
		 */
		public static function unregisterDomain(domain:ApplicationDomain):Boolean {

			if (domain === ClassUtil.systemDomain || !(domain in _registeredDomains))
				return false;

			// Get all classes associated with the domain to be unregistered.
			var domainsClasses:Array = _registeredDomains[domain];
			
			// Remove the reference to the domain.
			delete _registeredDomains[domain];
			
			// Find all children domain of the unregistered domain.
			var childDomains:Array = new Array();
			for (var key:Object in _registeredDomains) {
				var registeredDomain:ApplicationDomain = ApplicationDomain(key);
				for (var parentDomain:ApplicationDomain = registeredDomain.parentDomain;
					 parentDomain != null;
					 parentDomain = parentDomain.parentDomain) {
					
					if (parentDomain === domain) {
						childDomains.push(registeredDomain);
						break;
					}
				}
			}
			
			// Append all classes associated with any children domain and remove
			// children domain references.
			for each (var childDomain:ApplicationDomain in childDomains) {
				var domainClasses:Array = _registeredDomains[childDomain];
				for each (var domainClass:Class in domainClasses)
					domainsClasses.push(domainClass);
				delete _registeredDomains[childDomain];
			}
			
			// Remove all references to classes (and corresponding Types) associated
			// with the unregistered domain and its children.
			for each (var clazz:Class in domainsClasses) {
				try {
					delete _typeCache[clazz];
				}
				catch (e:Error) {
				}
			}
			
			return true;
		}
		
		/**
		 * Tells if an <code>ApplicationDomain</code> is registered.
		 * 
		 * @param domain the domain to check for.
		 * @return <code>true</code> if the domain has been registered, <code>false</code>
		 * 		otherwise.
		 */
		public static function isDomainRegistered(domain:ApplicationDomain):Boolean {
			return domain in _registeredDomains;
		}
		
		/**
		 * The Flash system domain (parent of all other application domains).
		 */
		public static function get systemDomain():ApplicationDomain {
			return ClassUtil.systemDomain;
		}
		
		/**
		 * An array that contains all currently registered application domains.
		 */
		public static function get registeredDomains():Array {
			var registeredDomains:Array = new Array();
			for (var registeredDomain:Object in _registeredDomains)
				registeredDomains.push(registeredDomain);
			return registeredDomains;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Instance

        /**
         * @private
         */
        private var _id:int;

        private static var CURRENT_ID:int = 0;

		/**
		 * @private
		 */
		private var _class:Class;
		
		/**
		 * @private
		 */
		private var _domain:ApplicationDomain;
		
		/**
		 * @private
		 */
		private var _superclasses:Array;
		
		/**
		 * @private
		 */
		private var _interfaces:Array;
		
		/**
		 * @private
		 */
		private var _annotations:Array;
		
		/**
		 * @private
		 */
		private var _constructor:Constructor;

		/**
		 * @private
		 */
		private var _fields:Array;

		/**
		 * @private
		 */
		private var _methods:Array;
		
		/**
		 * Constructs a new <code>Type</code> instance.
		 * 
		 * <p>
		 * <b>This constructor should not be used directly</b>. Instead, use the static
		 * methods <code>forName(...)</code>, <code>forInstance(...)</code> or
		 * <code>forClass(...)</code>.
		 * </p>
		 * 
		 * @param desc a class description as returned by the
		 * 		<code>flash.utils.describeType</code> method.
		 * @param clazz the underlying <code>Class</code> of this type.
		 * @param domain the application domain of this type.
		 * @throws org.granite.reflect.ReflectionError if domain is <code>null</code>.
		 * 
		 * @see #forName
		 * @see #forInstance
		 * @see #forClass
		 */
		function Type(desc:XML, clazz:Class, domain:ApplicationDomain) {
			super(this, desc);
			
			if (desc == null || domain == null)
				throw new ReflectionError("domain cannot be null");

            _id = CURRENT_ID++;
			_class = clazz;
			_domain = domain;
			
			CONFIG::debugging {
				trace("New Type for class: " + desc.@name + ", stacktrace:\n" + new Error().getStackTrace());
			}
		}
		
		/**
		 * Returns the underlying <code>Class</code> instance associated to this
		 * <code>Type</code>.
		 * 
		 * @return the <code>Class</code> instance (<code>null</code> for <code>void</code>
		 * 		or <code>~~</code> types).
		 */
		public function getClass():Class {
			return _class;
		}

        /**
         * Returns the internal identifier of this <code>Type</code>.
         * Can be used to identify types for the same <code>Class</code> in different application domains.
         *
         * @return the internal identifier of the type.
         */
        public function get id():int {
            return _id;
        }

		/**
		 * The underlying <code>ApplicationDomain</code> instance associated
		 * to this <code>Type</code>.
		 */
		[Transient]
		public function get domain():ApplicationDomain {
			return _domain;
		}
		
		/**
		 * The fully qualified class name of this <code>Type</code> instance, in
		 * the form of <code>path.to::Bean</code>.
		 */
		public function get name():String {
			return desc.@name;
		}
		
		/**
		 * The unqualified class name of this <code>Type</code> instance.
		 */
		public function get simpleName():String {
			return ClassUtil.extractSimpleName(name);
		}
		
		/**
		 * The package name of the class of this <code>Type</code> instance (may be
		 * an empty string).
		 */
		public function get packageName():String {
			return ClassUtil.extractPackageName(name);
		}
		
		/**
		 * The remote class alias of the class of this <code>Type</code> instance (may be
		 * an empty string).
		 * 
		 * <p>
		 * For example, for a class definition such as the following, the returned alias
		 * would be the String "path.to.MyJavaBean":
		 * </p>
		 * 
		 * <listing>
		 * [RemoteClass(alias="path.to.MyJavaBean")]
		 * public class MyAS3Bean {}</listing>
		 */
		public function get alias():String {
			return desc.@alias
		}
		
		/**
		 * Tells if this <code>Type</code> instance represents an interface.
		 * 
		 * @return <code>true</code> if this <code>Type</code> instance represents an
		 * 		interface, <code>false</code> otherwise.
		 */
		public function isInterface():Boolean {
			return ["Object", "void", "*"].indexOf(name) == -1 && desc.factory.extendsClass.length() == 0;
		}
		
		/**
		 * Tells if this <code>Type</code> instance represents a class.
		 * 
		 * @return <code>true</code> if this <code>Type</code> instance represents a
		 * 		class, <code>false</code> otherwise.
		 */
		public function isClass():Boolean {
			return !isInterface() && !isVoid() && !isWild();
		}
		
		/**
		 * Tells if this <code>Type</code> instance represents the pseudo-class "void".
		 * 
		 * @return <code>true</code> if this <code>Type</code> instance represents the
		 * 		pseudo-class "void", <code>false</code> otherwise.
		 */
		public function isVoid():Boolean {
			return this === VOID;
		}
		
		/**
		 * Tells if this <code>Type</code> instance represents the pseudo-class "~~" (wildcard).
		 * 
		 * @return <code>true</code> if this <code>Type</code> instance represents the
		 * 		pseudo-class "~~", <code>false</code> otherwise.
		 */
		public function isWild():Boolean {
			return this === WILD;
		}
		
		/**
		 * The superclass type of this <code>Type</code> instance or <code>null</code>
		 * if this class (or interface) has no superclass (or superinterface).
		 */
		[Transient]
		public function get superclass():Type {
			initSuperclasses();
			
			return (_superclasses.length > 0 ? _superclasses[0] : null);
		}

		/**
		 * An array that contains all superclass types of this <code>Type</code> instance,
		 * or an empty array if this class (or interface) has no superclass (or superinterface).
		 */
		[Transient]
		public function get superclasses():Array {
			initSuperclasses();
			
			return _superclasses.concat();
		}
		
		/**
		 * @private
		 */
		private function initSuperclasses():void {
			if (_superclasses == null) {
				
				CONFIG::debugging {
					trace("Initializing superclasses for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}

				_superclasses = new Array();
				for each (var superclass:XML in desc.factory.extendsClass)
					_superclasses.push(Type.forName(superclass.@type, declaredBy.domain));
			}
		}
		
		/**
		 * An array (possibly empty) that contains all interface types implemented by
		 * this <code>Type</code> instance.
		 */
		[Transient]
		public function get interfaces():Array {
			initInterfaces();
			
			return _interfaces.concat();
		}

		/**
		 * @private
		 */
		private function initInterfaces():void {
			if (_interfaces == null) {
				
				CONFIG::debugging {
					trace("Initializing interfaces for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}

				_interfaces = new Array();
				for each (var interfaze:XML in desc.factory.implementsInterface)
					_interfaces.push(Type.forName(interfaze.@type, declaredBy.domain));
			}
		}
		
		/**
		 * Determines if the class or interface represented by this <code>Type</code> instance is
		 * a subclass or subinterface of the class or interface represented by the
		 * specified <code>Type</code> parameter. 
		 * 
		 * @param type the <code>Type</code> object to be checked.
		 * @return <code>true</code> the supplied <code>type</code> is a superclass or a
		 * 		superinferface of this type, <code>false</code> otherwise. 
		 */
		public function isSubclassOf(type:Type):Boolean {
			initSuperclasses();
			for each (var t:Type in _superclasses) {
				if (t === type)
					return true;
			}
			
			initInterfaces();
			for each (t in _interfaces) {
				if (t === type)
					return true;
			}
			
			return false;
		}
		
		/**
		 * Determines if the class or interface represented by this <code>Type</code> instance is
		 * a superclass or superinterface of the class or interface represented by the
		 * specified <code>Type</code> parameter. 
		 * 
		 * @param type the <code>Type</code> object to be checked.
		 * @return <code>true</code> the supplied <code>type</code> is a subclass or a
		 * 		subinferface of this type, <code>false</code> otherwise. 
		 */
		public function isSuperclassOf(type:Type):Boolean {
			type.initSuperclasses();
			for each (var t:Type in type._superclasses) {
				if (t === this)
					return true;
			}
			
			type.initInterfaces();
			for each (t in type._interfaces) {
				if (t === this)
					return true;
			}
			
			return false;
		}
		
		/**
		 * @private
		 */
		public function fastExtendsClass(typeName:String):Boolean {
			var ex:XMLList = desc.factory.extendsClass.(@type == typeName);
			return ex.length() > 0;
		}
		
		/**
		 * @private
		 */
		public function fastImplementsInterface(typeName:String):Boolean {
			var ex:XMLList = desc.factory.implementsInterface.(@type == typeName);
			return ex.length() > 0;
		}
		
		/**
		 * @private
		 */
		public function fastSupertypes():Array {
			var arr:Array = [];
			for each (var c:XML in desc.factory.extendsClass)
				arr.push(c.@type.toXMLString());
			for each (var i:XML in desc.factory.implementsInterface)
				arr.push(i.@type.toXMLString());
			return arr;
		}
				
		
		///////////////////////////////////////////////////////////////////////
		// IAnnotatedElement implementation.
		
		/**
		 * @inheritDoc
		 */
		[Transient]
		public function get annotations():Array {
			return getAnnotations();
		}
		
		/**
		 * @inheritDoc
		 */
		public function getAnnotations(recursive:Boolean = false, pattern:String = "^_?[^_]"):Array {
			initAnnotations();
			
			if (recursive && pattern == null)
				return _annotations.concat();
			
			const re:RegExp = (pattern == null ? null : new RegExp(pattern));

			return _annotations.filter(
				function(a:Annotation, index:int, array:Array):Boolean {
					if (!recursive && a.declaredBy !== this)
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
		public function getAnnotation(type:String, recursive:Boolean = false):Annotation {
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
		public function isAnnotationPresent(type:String, recursive:Boolean = false):Boolean {
			return getAnnotation(type, recursive) != null;
		}
		
		/**
		 * @private
		 */
		private function initAnnotations():void {
			if (_annotations == null) {
				
				CONFIG::debugging {
					trace("Initializing annotations for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}

				_annotations = new Array();
				
				for each (var meta:XML in desc.factory.metadata)
					_annotations.push(new Annotation(this, meta));

				initSuperclasses();
				for each (var superclass:Type in _superclasses) {
					superclass.initAnnotations();
					for each (var a:Annotation in superclass._annotations) {
						if (a.annotatedElement !== superclass)
							break;
						_annotations.push(a);
					}
				}
				
				initInterfaces();
				for each (var interfaze:Type in _interfaces) {
					interfaze.initAnnotations();
					for each (a in interfaze._annotations) {
						if (a.annotatedElement !== interfaze)
							break;
						_annotations.push(a);
					}
				}
			}
		}
		
		/**
		 * 	@private
		 */
		public function getAnnotationNoCache(type:String):Annotation {
			var anno:XMLList = desc.factory.metadata.(@name == type);
			if (anno.length() == 0)
				return null;
			return new Annotation(this, anno[0]); 
		}
		
		/**
		 * 	@private
		 */
		public function getAnnotationsNoCache(type:String):Array {
			var anno:XMLList = desc.factory.metadata.(@name == type);
			if (anno.length() == 0)
				return [];
			var arr:Array = [];
			for each (var a:XML in anno)
				arr.push(new Annotation(this, a));
			return arr; 
		}
		
		/**
		 * 	@private
		 */
		public function getAnnotationArgValueNoCache(type:String, arg:String):String {
			var anno:XMLList = desc.factory.metadata.(@name == type);
			if (anno.length() == 0 || anno.arg.length() == 0)
				return null;
			return anno.arg.(@key == arg).@value; 
		}


		///////////////////////////////////////////////////////////////////////
		// Type constructor.
		
		/**
		 * Tests if this <code>Type</code> instance has a constructor.
		 * 
		 * @return <code>true</code> if the type is not an interface and not one
		 * 		of the pseudo types <code>VOID</code> and <code>WILD</code>.
		 * 
		 * @see Constructor
		 * @see #constructor
		 * @see #isInterface
		 * @see #isVoid
		 * @see #isWild
		 */
		public function hasConstructor():Boolean {
			return !(isInterface() || isVoid() || isWild());
		}
		
		/**
		 * Tests if this <code>Type</code> instance has a declared constructor.
		 * 
		 * @return <code>true</code> if this type has declared constructor in its XML
		 * 		description.
		 * 
		 * @see Constructor
		 * @see #constructor
		 */
		public function hasDeclaredConstructor():Boolean {
			return desc.factory.constructor.length() > 0;
		}
		
		/**
		 * Returns this <code>Type</code> instance constructor.
		 * 
		 * @return the <code>Constructor</code> object for this <code>Type</code> instance or
		 * 		<code>null</code>if this <code>Type</code> instance has no constructor.
		 * 
		 * @see #hasConstructor
		 */
		[Transient]
		public function get constructor():Constructor {
			if (!hasConstructor())
				return null;

			initConstructor();
			
			return _constructor;
		}
		
		/**
		 * @private
		 */
		private function initConstructor():void {
			if (_constructor == null) {
				
				CONFIG::debugging {
					trace("Initializing constructor for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}

				var constructors:XMLList = desc.factory.constructor;
				if (constructors.length() > 0)
					_constructor = new Constructor(this, constructors[0]);
				else
					_constructor = new Constructor(this, new XML("<constructor/>"));
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////
		// Type members.
		
		/**
		 * An array containing all members of this <code>Type</code> instance. Members are
		 * public constructor, methods, constants, variables and accessors (get/set).
		 * 
		 * @see Type Returned members description above
		 * @see Member
		 * @see Constructor
		 * @see Method
		 * @see Field
		 */
		[Transient]
		public function get members():Array {
			return getMembers();
		}
		
		/**
		 * Returns an array containing all members of this <code>Type</code> instance that match the
		 * supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (m:Member):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get all static methods and constants, you may use:
		 * </p>
		 * 
		 * <listing>
		 * var staticConstantAndMethods:Array = type.getMembers(function (m:Member):Boolean {
		 *     return m.isStatic() &amp;&amp; (m.isConstant() || m.isMethod());
		 * }</listing>
		 * 
		 * @param filter a function that will be used to eliminate members that do not match the
		 * 		function's criteria.
		 * @return an array that contains all <code>Member</code>s that match the supplied
		 * 		filter.
		 * 
		 * @see #members
		 * @see Type Returned members description above
		 * @see Member
		 * @see Constructor
		 * @see Method
		 * @see Field
		 */
		public function getMembers(filter:Function = null):Array {

			if (hasConstructor())
				initConstructor();
			initFields();
			initMethods();
			
			var members:Array = new Array();
			
			if (filter == null) {
				if (_constructor != null)
					members.push(_constructor);
				members.splice(0, 0, _fields);
				members.splice(0, 0, _methods);
			}
			else {
				if (_constructor != null && filter(_constructor))
					members.push(_constructor);
				for each (var field:Field in _fields) {
					if (filter(field))
						members.push(field);
				}
				for each (var method:Method in _methods) {
					if (filter(method))
						members.push(method);
				}
			}

			return members;
		}


		///////////////////////////////////////////////////////////////////////
		// Type fields.

		/**
		 * An array containing all fields of this <code>Type</code> instance. Fields are
		 * public constants, variables and accessors (get/set).
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		[Transient]
		public function get fields():Array {
			return getFields();
		}
		
		/**
		 * Returns an array containing all fields of this <code>Type</code> instance that match the
		 * supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (f:Field):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get all static constants, you may use:
		 * </p>
		 * 
		 * <listing>
		 * var staticConstants:Array = type.getFields(function (f:Field):Boolean {
		 *     return f.isStatic() &amp;&amp; f.isConstant();
		 * }</listing>
		 * 
		 * @param filter a function that will be used to eliminate fields that do not match the
		 * 		function's criteria.
		 * @return an array that contains all <code>Field</code>s that match the supplied
		 * 		filter.
		 * 
		 * @see #fields
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getFields(filter:Function = null):Array {
			initFields();
			
			if (filter == null)
				return _fields.concat();
			
			var fields:Array = new Array();
			for each (var field:Field in _fields) {
				if (filter(field))
					fields.push(field);
			}
			return fields;
		}
		
		/**
		 * Returns an array containing all fields of this <code>Type</code> instance that are
		 * annotated with at least one of the supplied annotation names.
		 * 
		 * @param recursive should we try to look for annotations in the superclasses's or the
		 * 		superinterfaces's overridden accessors?
		 * @param annotatedWiths a list of annotation names to look for.
		 * @return an array that contains all <code>Field</code>s annotated
		 * 		with at least one the supplied annotation names.
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getAnnotatedFields(recursive:Boolean, ... annotatedWiths):Array {
			return getFields(
				function (f:Field):Boolean {
					for each (var annotatedWith:String in annotatedWiths) {
						if (f.isAnnotationPresent(annotatedWith, recursive))
							return true;
					}
					return false;
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getAnnotatedFieldsNoCache(annotationName:String, constants:Boolean = false):Array {
			var fields:Array = new Array();
			
			if (constants) {
				for each (var item:XML in desc..constant.metadata.(@name == annotationName))
					fields.push(new Field(this, item.parent()));
			}
			
			for each (item in desc..variable.metadata.(@name == annotationName))
				fields.push(new Field(this, item.parent()));
			
			for each (item in desc..accessor.metadata.(@name == annotationName)) {
				// Filters 'prototype' accessor.
				if (item.parent().@declaredBy != "Class")
					fields.push(new Field(this, item.parent()));
			}
			
			return fields;
		}
		/**
		 * @private
		 */
		public function getAnnotatedFieldNamesNoCache(annotationName:String, constants:Boolean = false):Array {
			var fields:Array = new Array();
			
			if (constants) {
				for each (var item:XML in desc..constant.metadata.(@name == annotationName))
					fields.push(item.parent().@name.toXMLString());
			}
			
			for each (item in desc..variable.metadata.(@name == annotationName))
				fields.push(item.parent().@name.toXMLString());
			
			for each (item in desc..accessor.metadata.(@name == annotationName)) {
				// Filters 'prototype' accessor.
				if (item.parent().@declaredBy != "Class")
					fields.push(item.parent().@name.toXMLString());
			}
			
			return fields;
		}

		/**
		 * Returns the <i>first</i> field of this <code>Type</code> instance that match the
		 * supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (f:Field):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get the first static field whose name is equal to "abc", you may use:
		 * </p>
		 * 
		 * <listing>
		 * var staticAbcField:Field = type.getField(function (f:Field):Boolean {
		 *     return f.isStatic() &amp;&amp; f.name == "abc";
		 * });</listing>
		 * 
		 * @param filter a function that will be used to eliminate fields that do not match the
		 * 		function's criteria.
		 * @return the first found <code>Field</code> or <code>null</code> if no field matching
		 *		the function's criteria can be found.
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getField(filter:Function):Field {
			var fields:Array = getFields(filter);

			if (fields.length > 0)
				return fields[0];
			
			return null;
		}
		
		/**
		 * Returns the <i>non static</i> field of this <code>Type</code> class whose name is
		 * equals to the supplied parameter and declared in the namespace ns.
		 * 
		 * <p>
		 * A non-static field and a static field with the same name and namespace may coexist
		 * in the same class. That the purpose of the two methods <code>getInstanceField</code>
		 * and <code>getStaticField</code>.
		 * </p>
		 * 
		 * @param name the name of the field to search for.
		 * @param ns the namespace of the field to search for (<code>null</code> for public or
		 * 		builtin AS3 namespace).
		 * @return the found <code>Field</code> or <code>null</code> if no instance field with
		 * 		the supplied name can be found.
		 * 
		 * @see Type Returned members description above
		 * @see #getStaticField
		 * @see Field
		 */
		public function getInstanceField(name:String, ns:Namespace = null):Field {
			return getField(
				function(f:Field):Boolean {
					return (!f.isStatic() && f.name == name && f.isInNamespace(ns));
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getInstanceFieldNoCache(fieldName:String):Field {
			for each (var field:Field in getFieldsByName(fieldName)) {
				if (!field.isStatic())
					return field;
			}
			return null;
		}
		
		/**
		 * Returns the <i>static</i> field of this <code>Type</code> class whose name is
		 * equals to the supplied parameter.
		 * 
		 * <p>
		 * A non-static field and a static field with the same name may coexist in the same
		 * class. That the purpose of the two methods <code>getInstanceField</code> and
		 * <code>getStaticField</code>.
		 * </p>
		 * 
		 * @param name the name of the field to search for.
		 * @param ns the namespace of the field to search for (<code>null</code> for public or
		 * 		builtin AS3 namespace).
		 * @return the found <code>Field</code> or <code>null</code> if no static field with
		 *		the supplied name can be found.
		 * 
		 * @see Type Returned members description above
		 * @see #getInstanceField
		 * @see Field
		 */
		public function getStaticField(name:String, ns:Namespace = null):Field {
			return getField(
				function(f:Field):Boolean {
					return (f.isStatic() && f.name == name && f.isInNamespace(ns));
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getStaticFieldNoCache(fieldName:String):Field {
			for each (var field:Field in getFieldsByName(fieldName)) {
				if (field.isStatic())
					return field;
			}
			return null;
		}
		
		/**
		 * @private
		 */
		private function initFields():void {
			if (_fields == null) {

				CONFIG::debugging {
					trace("Initializing fields for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}
				
				_fields = new Array();
				
				for each (var item:XML in desc..constant)
					_fields.push(new Field(this, item));

				for each (item in desc..variable)
					_fields.push(new Field(this, item));

				for each (item in desc..accessor) {
					// Filters 'prototype' accessor.
					if (item.@declaredBy != "Class")
						_fields.push(new Field(this, item));
				}
			}
		}
		
		/**
		 * @private
		 */
		private function getFieldsByName(fieldName:String):Array {
			var fields:Array = new Array();
			
			for each (var item:XML in desc..constant.(@name == fieldName))
				fields.push(new Field(this, item));
			
			for each (item in desc..variable.(@name == fieldName))
				fields.push(new Field(this, item));
			
			for each (item in desc..accessor.(@name == fieldName)) {
				// Filters 'prototype' accessor.
				if (item.@declaredBy != "Class")
					fields.push(new Field(this, item));
			}
			
			return fields;
		} 


		///////////////////////////////////////////////////////////////////////
		// Type properties.
		
		/**
		 * An array containing all properties of this <code>Type</code> instance. Properties are
		 * non-static variables or read/write accessors (get/set) declared in the public or the
		 * builtin AS3 namespace.
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		[Transient]
		public function get properties():Array {
			return getProperties();
		}
		
		/**
		 * Returns an array containing all properties of this <code>Type</code> instance that
		 * match the supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (f:Field):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get all non-static accessors, you may use:
		 * </p>
		 * 
		 * <listing>
		 * var nonStaticAccessors:Array = type.getProperties(function (f:Field):Boolean {
		 *     return f.isAccessor();
		 * }</listing>
		 * 
		 * @param filter a function that will be used to eliminate properties that do not
		 * 		match the function's criteria.
		 * @return an array that contains all <code>Field</code>s that match the
		 * 		supplied filter.
		 * 
		 * @see #properties
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getProperties(filter:Function = null):Array {
			initFields();
			
			var properties:Array = new Array();
			for each (var field:Field in _fields) {
				if (!field.isConstant() && !field.isStatic() &&
					field.isReadable() && field.isWriteable() &&
					field.isInNamespace(null) && (filter == null || filter(field)))
					properties.push(field);
			}
			return properties;
		}
		
		/**
		 * Returns an array containing all properties of this <code>Type</code> instance that are
		 * annotated with at least one of the supplied annotation names.
		 * 
		 * @param recursive should we try to look for annotations in the superclasses's or the
		 * 		superinterfaces's overridden accessors?
		 * @param annotatedWiths a list of annotation names to look for.
		 * @return an array that contains all <code>Field</code>s annotated
		 * 		with at least one the supplied annotation names.
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getAnnotatedProperties(recursive:Boolean, ... annotatedWiths):Array {
			return getProperties(
				function (f:Field):Boolean {
					for each (var annotatedWith:String in annotatedWiths) {
						if (f.isAnnotationPresent(annotatedWith, recursive))
							return true;
					}
					return false;
				}
			);
		}
		
		/**
		 * Returns the property of this <code>Type</code> instance whose name is
		 * equals to the supplied parameter.
		 * 
		 * @param name the name of the property to search for.
		 * @return the found <code>Field</code> or <code>null</code> if no property with
		 *		the supplied name can be found.
		 * 
		 * @see Type Returned members description above
		 * @see Field
		 */
		public function getProperty(name:String):Field {
			var fields:Array = getProperties(
				function(f:Field):Boolean {
					return (f.name == name);
				}
			);
			
			if (fields.length > 0)
				return fields[0];

			return null;
		}


		///////////////////////////////////////////////////////////////////////
		// Type methods.
		
		/**
		 * An array containing all public methods of this <code>Type</code> class.
		 * 
		 * @see Type Returned members description above
		 * @see Method
		 */
		[Transient]
		public function get methods():Array {
			return getMethods();
		}
		
		/**
		 * Returns an array containing all methods of this <code>Type</code> instance that match the
		 * supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (m:Method):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get all static methods, you may use:
		 * </p>
		 * 
		 * <listing>
		 * var staticMethods:Array = type.getMethods(function (m:Method):Boolean {
		 *     return m.isStatic();
		 * });</listing>
		 * 
		 * @param filter a function that will be used to eliminate methods that do not match the
		 * 		function's criteria.
		 * @return an array that contains all <code>Method</code>s that match the supplied
		 * 		filter.
		 * 
		 * @see #methods
		 * @see Type Returned members description above
		 * @see Method
		 */
		public function getMethods(filter:Function = null):Array {
			initMethods();

			if (filter == null)
				return _methods.concat();
			
			var methods:Array = new Array();
			for each (var method:Method in _methods) {
				if (filter(method))
					methods.push(method);
			}
			return methods;
		}

		/**
		 * Returns an array containing all methods of this <code>Type</code> instance that are
		 * annotated with at least one of the supplied annotation names.
		 * 
		 * @param recursive should we try to look for annotations in the superclasses's or the
		 * 		superinterfaces's overridden methods?
		 * @param annotatedWiths a list of annotation names to look for.
		 * @return an array that contains all <code>Method</code>s annotated
		 * 		with at least one the supplied annotation names.
		 * 
		 * @see Type Returned members description above
		 * @see Method
		 */
		public function getAnnotatedMethods(recursive:Boolean, ... annotatedWiths):Array {
			return getMethods(
				function (m:Method):Boolean {
					for each (var annotatedWith:String in annotatedWiths) {
						if (m.isAnnotationPresent(annotatedWith, recursive))
							return true;
					}
					return false;
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getAnnotatedMethodsNoCache(annotationName:String):Array {
			var methods:Array = new Array();
			
			for each (var item:XML in desc..method.metadata.(@name == annotationName))
				methods.push(new Method(this, item.parent()));
			
			return methods;
		}
		
		/**
		 * Returns the <i>first</i> method of this <code>Type</code> instance that match the
		 * supplied function's criteria.
		 * 
		 * <p>
		 * The filter <code>Function</code> is expected to have the following signature:
		 * </p>
		 * 
		 * <listing>
		 * function (m:Method):Boolean</listing>
		 * 
		 * <p>
		 * For example, to get the first static method whose name is equal to "abc", you may use:
		 * </p>
		 * 
		 * <listing>
		 * var staticAbcMethod:Method = type.getMethod(function (m:Method):Boolean {
		 *     return m.isStatic() &amp;&amp; m.name == "abc";
		 * });</listing>
		 * 
		 * @param filter a function that will be used to eliminate methods that do not match the
		 * 		function's criteria.
		 * @return the first found <code>Method</code> or <code>null</code> if no method matching
		 *		the function's criteria can be found.
		 * 
		 * @see Type Returned members description above
		 * @see Method
		 */
		public function getMethod(filter:Function):Method {
			var methods:Array = getMethods(filter);
			
			if (methods.length > 0)
				return methods[0];
			
			return null;
		}
		
		/**
		 * Returns the <i>non static</i> method of this <code>Type</code> class whose name is
		 * equals to the supplied parameter.
		 * 
		 * <p>
		 * Instead of Java, there is no need to care about parameters. Only one non-static method
		 * with a given name may exist. However, a non-static method and a static method with the
		 * same name may coexist in the same class. That the purpose of the two methods
		 * <code>getInstanceMethod</code> and <code>getStaticMethod</code>.
		 * </p>
		 * 
		 * @param name the name of the method to search for.
		 * @param ns the namespace of the method to search for (<code>null</code> for public or
		 * 		builtin AS3 namespace).
		 * @return the found <code>Method</code> or <code>null</code> if no instance method with
		 *		the supplied name can be found.
		 * 
		 * @see #getStaticMethod
		 * @see Type Returned members description above
		 * @see Method
		 */
		public function getInstanceMethod(name:String, ns:Namespace = null):Method {
			return getMethod(
				function (m:Method):Boolean {
					return (!m.isStatic() && m.name == name && m.isInNamespace(ns));
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getInstanceMethodNoCache(methodName:String):Method {
			for each (var method:Method in getMethodsByName(methodName)) {
				if (!method.isStatic())
					return method;
			}
			return null;
		}
		
		/**
		 * Returns the <i>static</i> method of this <code>Type</code> class whose name is
		 * equals to the supplied parameter.
		 * 
		 * <p>
		 * Instead of Java, there is no need to care about parameters. Only one static method
		 * with a given name may exist. However, a static method and a non-static method with the
		 * same name may coexist in the same class. That the purpose of the two methods
		 * <code>getInstanceMethod</code> and <code>getStaticMethod</code>.
		 * </p>
		 * 
		 * @param name the name of the method to search for.
		 * @param ns the namespace of the method to search for (<code>null</code> for public or
		 * 		builtin AS3 namespace).
		 * @return the found <code>Method</code> or <code>null</code> if no static method with
		 *		the supplied name can be found.
		 * 
		 * @see #getInstanceMethod
		 * @see Type Returned members description above
		 * @see Method
		 */
		public function getStaticMethod(name:String, ns:Namespace = null):Method {
			return getMethod(
				function (m:Method):Boolean {
					return (m.isStatic() && m.name == name && m.isInNamespace(ns));
				}
			);
		}
		
		/**
		 * @private
		 */
		public function getStaticMethodNoCache(methodName:String):Method {
			for each (var method:Method in getMethodsByName(methodName)) {
				if (method.isStatic())
					return method;
			}
			return null;
		}
		
		/**
		 * @private
		 */
		private function initMethods():void {
			
			if (_methods == null) {
				
				CONFIG::debugging {
					trace("Initializing methods for Type: " + name + ", stacktrace:\n" + new Error().getStackTrace());
				}

				_methods = new Array();
				
				for each (var item:XML in desc..method)
				_methods.push(new Method(this, item));
			}
		}
		
		/**
		 * @private
		 */
		private function getMethodsByName(methodName:String):Array {
			var methods:Array = new Array();
			for each (var item:XML in desc..method.(@name == methodName))
				methods.push(new Method(this, item));
			return methods;
		}
	}
}
