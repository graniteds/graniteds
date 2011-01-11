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