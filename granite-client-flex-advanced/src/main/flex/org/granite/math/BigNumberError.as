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

package org.granite.math {

	import flash.utils.getQualifiedClassName;

    /**
	 * This error is the base class of all GraniteDS math errors. It may be thrown
	 * itself when a low level problem is detected (assertion failed).
	 * 
     * @author Franck WOLFF
     */
	public class BigNumberError extends Error {

		/**
		 * Constructs a new <code>BigNumberError</code>
		 * 
		 * @param message the message of this error.
		 * @param id the numeric code of this error.
		 */
		function BigNumberError(message:* = "", id:* = 0) {
			super(message, id);
			
			name = getQualifiedClassName(this);
		}
	}
}