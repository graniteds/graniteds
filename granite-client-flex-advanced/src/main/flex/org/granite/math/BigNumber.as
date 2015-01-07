/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.math {

	import flash.utils.IExternalizable;
	
	import org.granite.IValue;

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
	public interface BigNumber extends IExternalizable, IValue {

		/**
		 * The sign of this big number as an integer (-1 for negative, 0
		 * for null or 1 for positive numbers).
		 */
		function get sign():int;
		
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