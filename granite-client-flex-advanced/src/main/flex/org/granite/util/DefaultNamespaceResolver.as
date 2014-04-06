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
package org.granite.util {

	import flash.utils.Dictionary;

	/**
	 * A default implementation of the <code>INamespaceResolver</code>
	 * interface.<br/>
	 * 
	 * Sample usage:
	 * <listing>
	 * var resolver:INamespaceResolver = new DefaultNamespaceResolver("path.to.defaultNamespace.~~");
	 * resolver.resolve("MyClass"); // "path.to.defaultNamespace.MyClass"
	 * resolver.registerNamespace("a", "path.to.a.~~");
	 * resolver.resolve("a:MyClass"); // "path.to.a.MyClass"</listing>
	 * 
	 * @author Franck WOLFF
	 */
	public class DefaultNamespaceResolver implements INamespaceResolver {

		///////////////////////////////////////////////////////////////////////
		// Fields.
		
		private var _namespaces:Dictionary = new Dictionary();
		private var _defaultNamespaceValue:String = null;
		private var _allowOverrideDefault:Boolean = true;
		private var _upperPolicy:Boolean = true;

		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * Constructs a new <code>DefaultNamespaceResolver</code> instance.
		 * 
		 * @param defaultNamespaceValue see corresponding property.
		 * @param allowOverrideDefault see corresponding property.
		 * @param upperPolicy see corresponding property.
		 */
		function DefaultNamespaceResolver(defaultNamespaceValue:String = null,
										  allowOverrideDefault:Boolean = true,
										  upperPolicy:Boolean = true) {
			_defaultNamespaceValue = defaultNamespaceValue;
			if (_defaultNamespaceValue != null)
				registerNamespace("", _defaultNamespaceValue);

			_allowOverrideDefault = allowOverrideDefault;
			_upperPolicy = upperPolicy;
		}

		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The package name of the default (or empty) namespace. 
		 */
		public function get defaultNamespaceValue():String {
			return _defaultNamespaceValue;
		}
		
		/**
		 * Tells if the default namespace may be overridden (ie: if you can
		 * call <code>resolver.registerNamespace("~~", "path.to.something.~~")</code>).
		 */
		public function get allowOverrideDefault():Boolean {
			return _allowOverrideDefault;
		}
		
		/**
		 * Tells if a string such as "MyClass.Inner" should be considered as a
		 * class name because it starts with a uppercase letter (so it is bound
		 * to the default namespace) or not (so it is considered as a fully
		 * qualified class name in the package "MyClass"). 
		 */
		public function get upperPolicy():Boolean {
			return _upperPolicy;
		}

		///////////////////////////////////////////////////////////////////////
		// INamespaceResolver implementation.
		
		/**
		 * Register a namespace prefix bound to a given package name. The package
		 * name must be of the form: <code>path.to.~~</code>. You may pass
		 * <code>null</code>, <code>""</code>, <code>"~~"</code> or
		 * <code>".~~"</code> for an empty package name. 
		 * 
		 * @param prefix the prefix to be registered.
		 * @param packageName the package name bound to the prefix.
		 * @return the previous package name bound to the prefix, or
		 * 		<code>null</code> if the prefix wasn't registered.
		 * @throws ArgumentError if the prefix is <code>null</code> or if it is
		 * 		empty and not allowed to redefine the default namespace, or if the
		 * 		package name isn't of the required form.
		 */
		public function registerNamespace(prefix:String, packageName:String):String {
			if (prefix == null)
				throw new ArgumentError("Namespace prefix cannot be null");
			
			if (prefix == "" && !_allowOverrideDefault)
				throw new ArgumentError("Redefining default namespace is not allowed");
			
			if (packageName == null || packageName == "" || packageName == "*" || packageName == ".*")
				packageName = "";
			else  {
				if (!(/^(\w+\.)*(\w+\.\*)$/g).test(packageName))
					throw new ArgumentError("Illegal namespace package name: " + packageName);
				// remove trailing '*' character.
				packageName = packageName.substr(0, packageName.length - 1);
			}
			
			var previousValue:String = (_namespaces.hasOwnProperty(prefix) ? _namespaces[prefix] : null);
			_namespaces[prefix] = packageName;
			return previousValue;
		}
		
		/**
		 * @inheritDoc
		 */
		public function unregisterNamespace(prefix:String):String {
			if (prefix == null)
				throw new ArgumentError("Namespace prefix cannot be empty");
			
			if (prefix == "" && !_allowOverrideDefault)
				throw new ArgumentError("Redefining default namespace is not allowed");

			var previousValue:String = null;
			if (_namespaces.hasOwnProperty(prefix))
				delete _namespaces[prefix];
			return previousValue;
		}
		
		/**
		 * @inheritDoc
		 */
		public function getNamespaceValue(prefix:String):String {
			if (prefix == null)
				throw new ArgumentError("Namespace prefix cannot be empty");
			return (_namespaces.hasOwnProperty(prefix) ? _namespaces[prefix] : null);
		}
		
		/**
		 * @inheritDoc
		 */
		public function resolve(className:String):String {
			var qName:Array = ClassUtil.parseQName(className, _upperPolicy);
			if (qName[0] != null && _namespaces.hasOwnProperty(qName[0]))
				className = _namespaces[qName[0]] + qName[1];
			return className;
		}
	}
}