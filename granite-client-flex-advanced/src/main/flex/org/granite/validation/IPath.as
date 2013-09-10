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

package org.granite.validation {

	/**
	 * Represent the navigation path from an object to another
	 * in an object graph. Each path element is represented by a
	 * <code>Node</code>.
	 *
	 * The path corresponds to the succession of nodes in the order
	 * they are returned by the <code>nodes</code> property.
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see ConstraintViolation
	 * @see INode
	 */
	public interface IPath {
		
		/**
		 * The <code>INode</code> array that represent this path,
		 * from the root bean to the leaf.
		 */
		function get nodes():Array;
		
		/**
		 * Returns a string representation of this path.
		 * 
		 * @return a string representation of this path.
		 */
		function toString():String;
	}
}