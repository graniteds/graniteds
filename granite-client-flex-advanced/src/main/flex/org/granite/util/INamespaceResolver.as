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

	/**
	 * An interface used for namespace resolutions (such as
	 * <code>ns:MyClass</code>).
	 * 
	 * @author Franck WOLFF
	 */
	public interface INamespaceResolver {
		
		/**
		 * Register a namespace prefix bound to a given package name.
		 * 
		 * @param prefix the prefix to be registered.
		 * @param packageName the package name bound to the prefix.
		 * @return the previous package name bound to the prefix, or
		 * 		<code>null</code> if the prefix wasn't registered.
		 */
		function registerNamespace(prefix:String, packageName:String):String;
		
		/**
		 * Unregister a namespace prefix.
		 * 
		 * @param the prefix to be unregistered.
		 * @return the package name bound to the prefix, or <code>null</code>
		 * 		if the prefix wasn't registered.
		 */
		function unregisterNamespace(prefix:String):String;
		
		/**
		 * Returns the package name bound to a given prefix.
		 * 
		 * @param the prefix to look for.
		 * @return the package name bound to the prefix, or <code>null</code>
		 * 		if the prefix wasn't registered.
		 */
		function getNamespaceValue(prefix:String):String;
		
		/**
		 * Returns a fully qualified class name, resolving namespace
		 * prefix (such as <code>ns:MyClass</code> resolved to
		 * <code>path.to.MyClass</code>).
		 * 
		 * @param the class name to be resolved.
		 * @return the resolved class name.
		 * 
		 * @see org.granite.util.ClassUtil#parseQName.
		 */
		function resolve(className:String):String;
	}
}