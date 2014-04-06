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
package org.granite.math {

	import org.granite.util.Enum;

	[RemoteClass(alias="java.math.RoundingMode")]
	/**
	 * Specifies a <i>rounding behavior</i> for numerical operations
	 * capable of discarding precision. Each rounding mode indicates how
	 * the least significant returned digit of a rounded result is to be
	 * calculated.  If fewer digits are returned than the digits needed to
	 * represent the exact numerical result, the discarded digits will be
	 * referred to as the <i>discarded fraction</i> regardless the digits'
	 * contribution to the value of the number. In other words,
	 * considered as a numerical value, the discarded fraction could have
	 * an absolute value greater than one.
	 * <br>
	 * <br>
	 * Each rounding mode description includes a table listing how
	 * different two-digit decimal values would round to a one digit
	 * decimal value under the rounding mode in question. The result
	 * column in the tables could be gotten by creating a
	 * <code>BigDecimal</code> number with the specified value, forming a
	 * <code>MathContext</code> object with the proper settings
	 * (<code>precision</code> set to <code>1</code>, and the
	 * <code>roundingMode</code> set to the rounding mode in question), and
	 * calling <code>BigDecimal.round</code> on this number with the
	 * proper <code>MathContext</code>. A summary table showing the results
	 * of these rounding operations for all rounding modes appears below.
	 * <br>
	 * <br>
	 * <i>This class is basically an ActionScript3 port of the Java
	 * class <code>java.math.RoundingMode</code> originally written by
	 * Josh Bloch, Mike Cowlishaw and Joseph D. Darcy.</i>
	 * 
	 * @author Franck WOLFF
	 *
	 * @see     BigDecimal
	 * @see     MathContext
	 */
	public final class RoundingMode extends Enum {

		///////////////////////////////////////////////////////////////////////
		// Constants.
		
		/**
		 * Rounding mode to round towards positive infinity. If the
		 * result is positive, behaves as for <code>RoundingMode.UP</code>;
		 * if negative, behaves as for <code>RoundingMode.DOWN</code>. Note
		 * that this rounding mode never decreases the calculated value.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>CEILING</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>6</td></tr>
		 * <tr align="right"><td>2.5</td><td>3</td></tr>
		 * <tr align="right"><td>1.6</td><td>2</td></tr>
		 * <tr align="right"><td>1.1</td><td>2</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-1</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-2</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-5</td></tr>
		 * </table>
		 */
		public static const CEILING:RoundingMode = new RoundingMode("CEILING", _);

		/**
		 * Rounding mode to round towards zero. Never increments the digit
		 * prior to a discarded fraction (i.e., truncates).  Note that this
		 * rounding mode never increases the magnitude of the calculated value.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>DOWN</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>5</td></tr>
		 * <tr align="right"><td>2.5</td><td>2</td></tr>
		 * <tr align="right"><td>1.6</td><td>1</td></tr>
		 * <tr align="right"><td>1.1</td><td>1</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-1</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-2</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-5</td></tr>
		 * </table>
		 */
		public static const DOWN:RoundingMode = new RoundingMode("DOWN", _);

		/**
		 * Rounding mode to round towards negative infinity. If the
		 * result is positive, behave as for <code>RoundingMode.DOWN</code>;
		 * if negative, behave as for <code>RoundingMode.UP</code>. Note that
		 * this rounding mode never increases the calculated value.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>FLOOR</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>5</td></tr>
		 * <tr align="right"><td>2.5</td><td>2</td></tr>
		 * <tr align="right"><td>1.6</td><td>1</td></tr>
		 * <tr align="right"><td>1.1</td><td>1</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-2</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-2</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-3</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-6</td></tr>
		 * </table>
		 */
		public static const FLOOR:RoundingMode = new RoundingMode("FLOOR", _);
		
		/**
		 * Rounding mode to round towards "nearest neighbor" unless both neighbors
		 * are equidistant, in which case round down. Behaves as for
		 * <code>RoundingMode.UP</code> if the discarded fraction is &gt; 0.5;
		 * otherwise, behaves as for <code>RoundingMode.DOWN</code>.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>HALF_DOWN</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>5</td></tr>
		 * <tr align="right"><td>2.5</td><td>2</td></tr>
		 * <tr align="right"><td>1.6</td><td>2</td></tr>
		 * <tr align="right"><td>1.1</td><td>1</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-2</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-2</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-5</td></tr>
		 * </table>
		 */
		public static const HALF_DOWN:RoundingMode = new RoundingMode("HALF_DOWN", _);

		/**
		 * Rounding mode to round towards the "nearest neighbor" unless both
		 * neighbors are equidistant, in which case, round towards the even
		 * neighbor. Behaves as for <code>RoundingMode.HALF_UP</code> if the
		 * digit to the left of the discarded fraction is odd; behaves as for
		 * <code>RoundingMode.HALF_DOWN</code> if it's even.  Note that this
		 * is the rounding mode that statistically minimizes cumulative
		 * error when applied repeatedly over a sequence of calculations.
		 * It is sometimes known as &quot;Banker's rounding,&quot; and is
		 * chiefly used in the USA.  This rounding mode is analogous to
		 * the rounding policy used for <code>float</code> and <code>double</code>
		 * arithmetic in Java.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>HALF_EVEN</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>6</td></tr>
		 * <tr align="right"><td>2.5</td><td>2</td></tr>
		 * <tr align="right"><td>1.6</td><td>2</td></tr>
		 * <tr align="right"><td>1.1</td><td>1</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-2</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-2</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-6</td></tr>
		 * </table>
		 */
		public static const HALF_EVEN:RoundingMode = new RoundingMode("HALF_EVEN", _);

		/**
		 * Rounding mode to round towards "nearest neighbor" unless both neighbors
		 * are equidistant, in which case round up. Behaves as for
		 * <code>RoundingMode.UP</code> if the discarded fraction is &gt;= 0.5;
		 * otherwise, behaves as for <code>RoundingMode.DOWN</code>.  Note that this
		 * is the rounding mode commonly taught at school.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>HALF_UP</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>6</td></tr>
		 * <tr align="right"><td>2.5</td><td>3</td></tr>
		 * <tr align="right"><td>1.6</td><td>2</td></tr>
		 * <tr align="right"><td>1.1</td><td>1</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-2</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-3</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-6</td></tr>
		 * </table>
		 */
		public static const HALF_UP:RoundingMode = new RoundingMode("HALF_UP", _);

		/**
		 * Rounding mode to assert that the requested operation has an exact result,
		 * hence no rounding is necessary. If this rounding mode is
		 * specified on an operation that yields an inexact result, an
		 * <code>BigNumberError</code> is thrown.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>UNNECESSARY</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>2.5</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>1.6</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>1.1</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>-1.6</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>-2.5</td><td>throw <code>BigNumberError</code></td></tr>
		 * <tr align="right"><td>-5.5</td><td>throw <code>BigNumberError</code></td></tr>
		 * </table>
		 */
		public static const UNNECESSARY:RoundingMode = new RoundingMode("UNNECESSARY", _);

		/**
		 * Rounding mode to round away from zero. Always increments the
		 * digit prior to a non-zero discarded fraction.  Note that this
		 * rounding mode never decreases the magnitude of the calculated
		 * value.
		 * <br>
		 * <br>
		 * Example:
		 * <table style="border: 1px solid #808080">
		 * <tr valign="top">
		 * <th>Input Number</th>
		 * <th>Input rounded to one digit<br> with <code>UP</code> rounding</th>
		 * </tr>
		 * <tr align="right"><td>5.5</td><td>6</td></tr>
		 * <tr align="right"><td>2.5</td><td>3</td></tr>
		 * <tr align="right"><td>1.6</td><td>2</td></tr>
		 * <tr align="right"><td>1.1</td><td>2</td></tr>
		 * <tr align="right"><td>1.0</td><td>1</td></tr>
		 * <tr align="right"><td>-1.0</td><td>-1</td></tr>
		 * <tr align="right"><td>-1.1</td><td>-2</td></tr>
		 * <tr align="right"><td>-1.6</td><td>-2</td></tr>
		 * <tr align="right"><td>-2.5</td><td>-3</td></tr>
		 * <tr align="right"><td>-5.5</td><td>-6</td></tr>
		 * </table>
		 */
		public static const UP:RoundingMode = new RoundingMode("UP", _);
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * @private
		 */
		function RoundingMode(value:String = null, restrictor:* = null) {
			super((value || UNNECESSARY.name), restrictor);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static accessors.
		
		/**
		 * @private
		 */
		internal static function get constants():Array {
			return [CEILING, DOWN, FLOOR, HALF_DOWN, HALF_EVEN, HALF_UP, UNNECESSARY, UP];
		}
		
		/**
		 * Returns a <code>RoundingMode</code> constant according to the
		 * supplied parameter.
		 * 
		 * @param name a <code>RoundingMode</code> constant name.
		 * @return a <code>RoundingMode</code> constant.
		 * @throws org.granite.math.IllegalArgumentError if the <code>name</code>
		 * 		parameter does not specify a <code>RoundingMode</code> constant
		 * 		name.
		 */
		public static function valueOf(name:String):RoundingMode {
			try {
				return RoundingMode(UNNECESSARY.constantOf(name));
			}
			catch (e:Error) {
				throw new IllegalArgumentError(e.message);
			}
			return null; // makes AS3 compiler happy...
		}
		
		///////////////////////////////////////////////////////////////////////
		// Enum implementation.
		
		/**
		 * @private
		 */
		override protected function getConstants():Array {
			return constants;
		}
	}
}