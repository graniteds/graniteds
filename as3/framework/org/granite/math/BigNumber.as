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

	import flash.utils.IExternalizable;

    /**
	 * Common interface for the <code>Long</code>, <code>BigInteger</code> and
	 * <code>BigDecimal</code> classes.
	 * 
     * @author Franck WOLFF
	 * 
	 * @see Long
	 * @see BigInteger
	 * @see BigDecimal
     */
	public interface BigNumber extends IExternalizable {

		/**
		 * The sign of this big number as an integer (-1 for negative, 0
		 * for null or 1 for positive numbers).
		 */
		function get sign():int;

		/**
		 * Tells if this big number is equals to the supplied parameter.
		 * 
		 * @param o the <code>Object</code> to which this big number
		 * 		is to be compared.
		 * @return <code>true</code> if and only if the specified
		 * 		<code>Object</code> is equals to this object.
		 */
		function equals(o:*):Boolean;
		
		/**
		 * Returns this big number as an integer (with possible loose of precision).
		 * 
		 * @return this big number as an integer.
		 */
		function toInt():int;
		
		/**
		 * Returns this big number as a <code>Number</code> (with possible loose of
		 * precision).
		 * 
		 * @return this big number as a <code>Number</code>.
		 */
		function toNumber():Number;
	}
}