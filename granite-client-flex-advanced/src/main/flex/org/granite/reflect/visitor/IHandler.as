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
package org.granite.reflect.visitor {

	/**
	 * <code>IHandler</code> implementations are responsible of cascading
	 * specific data properties (properties of a bean, items of a collection,
	 * etc.)
	 * 
	 * @author Franck WOLFF
	 */
	public interface IHandler {
		
		/**
		 * Tells if this <code>IHandler</code> implementation can handle the
		 * supplied <code>Visitable</code> parameter.
		 * 
		 * @param visitable the <code>Visitable</code> instance that could be
		 * 		handled by this <code>IHandler</code> implementation.
		 * @return <code>true</code> if this <code>IHandler</code> can handle
		 * 		the visitable parameter, <code>false</code> otherwise.
		 */
		function canHandle(visitable:Visitable):Boolean;
		
		/**
		 * Handles the supplied <code>Visitable</code> parameter, returning a
		 * filtered array of its properties.
		 * 
		 * @param visitable the <code>Visitable</code> instance to be introspected.
		 * @param filter a filter function on the form of
		 * 		<code>filter(visitable:Visitable):Boolean</code>, that must be call
		 * 		before appending a new property to the returned array.
		 * @return an array of <code>Visitable</code> instances (one for each
		 * 		filtered property of the visitable parameter).
		 */
		function handle(visitable:Visitable, filter:Function):Array;
	}
}