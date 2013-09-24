/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
	 * Static utility functions for ActionScript3's <code>Number</code>s.
	 * 
	 * @author Franck WOLFF
	 */
	public class NumberUtil {

		/**
		 * Returns the length of the integer part of the number paremeter, if it
		 * was written in base 10 without any exponent.
		 * 
		 * <p>
		 * <b>Note</b>: this method seems to be only reliable with numbers between -9999999999999998
		 * and +9999999999999998 (inclusive), made from no more than 16 significants digits and with
		 * an optional dot anywhere (only empirical)...
		 * </p>
		 * 
		 * @param number the number from wich to get interger part length.
		 * @return the integer part length or -1 if the supplied number is NaN or infinite.
		 */
		public static function getIntegerPartLength(number:Number):int {
			return getIntegerFractionLengths(number)[0];
		}

		/**
		 * Returns the length of the fraction part of the number paremeter, if it
		 * was written in base 10 without any exponent.
		 * 
		 * <p>
		 * <b>Note</b>: this method seems to be only reliable with numbers between -9999999999999998
		 * and +9999999999999998 (inclusive), made from no more than 16 significants digits and with
		 * an optional dot anywhere (only empirical)...
		 * </p>
		 * 
		 * @param number the number from wich to get fraction part length.
		 * @return the fraction part length or -1 if the supplied number is NaN or infinite.
		 */
		public static function getFractionPartLength(number:Number):int {
			return getIntegerFractionLengths(number)[1];
		}
		

		/**
		 * Returns an array of two <code>int</code> values that represents the lengths of the
		 * integer and fraction parts of the number paremeter, if it was written in base 10
		 * without any exponent.
		 * 
		 * <p>
		 * <b>Note</b>: this method seems to be only reliable with numbers between -9999999999999998
		 * and +9999999999999998 (inclusive), made from no more than 16 significants digits and with
		 * an optional dot anywhere (only empirical)...
		 * </p>
		 * 
		 * @param number the number from wich to get integer and the fraction part lengths.
		 * @return an array that contains the integer and the fraction parts lengths or [-1, -1]
		 * 		if the supplied number is NaN or infinite.
		 */
		public static function getIntegerFractionLengths(number:Number):Array {

				if (isNaN(number) || !isFinite(number))
					return [-1, -1];
				
				// Get the String representation of the number (with an optional leading '-' sign
				// as well as an optional trailing exponent part 'e[+-]xxx').
				var digits:String = number.toString();

				// Make sure we got an expected result.
				if (!(/^(\-?[0-9]+|\-?[0-9]+e[\+\-][0-9]+|\-?[0-9]+\.[0-9]+|\-?[0-9]+\.[0-9]+e[\+\-][0-9]+)$/).test(digits))
					throw new Error("Unexpected Number.toString() result: " + digits);

				// Skip leading sign if any.
				var start:int = 0;
				if (digits.charAt(0) == '-')
					start++;
				
				// Search for exponent (if any) and convert it to a signed integer.
				var exp:Number = 0;
				var end:int = digits.indexOf('e', start);
				if (end == -1)
					end = digits.length;
				else
					exp = Number(digits.substring(end + 1));
				
				// Keep digits without sign and exponent.
				digits = digits.substring(start, end);

				// Append a '.' if necessary.
				if (digits.indexOf('.') == -1)
					digits += '.';
				
				// Trim leading and trailing zeros (only keep significant digits).
				digits = digits.replace(/^0+|0+$/g, '');
				
				// number == 0.
				if (digits == '.')
					return [0, 0];
				
				// Significant digits count before dot (should be 0 or 1).
				var integerLength:int = digits.indexOf('.');
				// Significant digits after dot (0 <= fraction.length <= 20).
				var fraction:String = digits.substr(integerLength + 1);
				
				// Calculate the integer part length (for exponent == 0).
				if (integerLength > 0)
					integerLength += exp;
				else
					integerLength = exp - fraction.search(/[1-9]/);
				if (integerLength < 0)
					integerLength = 0;
				
				// Calculate the fraction part length (for exponent == 0).
				var fractionLength:int = fraction.length - exp;
				if (fractionLength < 0)
					fractionLength = 0;
				
				return [integerLength, fractionLength];
		}
	}
}