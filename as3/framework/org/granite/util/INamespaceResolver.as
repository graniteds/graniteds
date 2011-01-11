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