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

	/**
	 * Base interface for <code>Member</code> and <code>DynamicProperty</code>
	 * classes (mainly a marker).
	 * 
	 * @see Member
	 * @see DynamicProperty
	 * 
	 * @author Franck WOLFF
	 */
	public interface IMember {

		/**
		 * The String representation of this member (field or method qName, key of a
		 * dynamic property or dictionary, index of an array, etc.)
		 */
		function get propertyKey():String;
		
		/**
		 * Checks if this member is static.
		 * 
		 * @return <code>true</code> if this member is static, <code>false</code>
		 * 		otherwise.
		 */
		function isStatic():Boolean;
		
		/**
		 * Checks if this member is a constant field.
		 * 
		 * @return <code>true</code> if this member is a constant, <code>false</code>
		 * 		otherwise.
		 */
		function isConstant():Boolean;
		
		/**
		 * Checks if this member is a variable field.
		 * 
		 * @return <code>true</code> if this member is a variable, <code>false</code>
		 * 		otherwise.
		 */
		function isVariable():Boolean;
		
		/**
		 * Checks if this member is an accessor (get/set) field.
		 * 
		 * @return <code>true</code> if this member is an accessor, <code>false</code>
		 * 		otherwise.
		 */
		function isAccessor():Boolean;
		
		/**
		 * Checks if this member is a dynamic property (always <code>false</code> for
		 * <code>Member</code>, always <code>true</code> for <code>DynamicProperty</code>).
		 * 
		 * @return <code>true</code> if this member is a dynamic property, <code>false</code>
		 * 		otherwise.
		 * 
		 * @see Member
		 * @see DynamicProperty
		 */
		function isDynamicProperty():Boolean;
		
		/**
		 * Checks if this member is a method.
		 * 
		 * @return <code>true</code> if this member is a method, <code>false</code>
		 * 		otherwise.
		 */
		function isMethod():Boolean;
		
		/**
		 * Checks if this member is a constructor.
		 * 
		 * @return <code>true</code> if this member is a constructor, <code>false</code>
		 * 		otherwise.
		 */
		function isConstructor():Boolean;
		
		/**
		 * Checks if this member is readable (fields only).
		 * 
		 * @return <code>true</code> if this member is readable, <code>false</code>
		 * 		otherwise.
		 */
		function isReadable():Boolean;
		
		/**
		 * Checks if this member is writeable (fields only).
		 * 
		 * @return <code>true</code> if this member is writeable, <code>false</code>
		 * 		otherwise.
		 */
		function isWriteable():Boolean;
	}
}