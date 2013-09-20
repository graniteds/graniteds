/**
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
package org.granite.math {

	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	
	[RemoteClass(alias="java.math.BigDecimal")]
	/**
	 * Immutable, arbitrary-precision signed decimal numbers. A
	 * <code>BigDecimal</code> consists of an arbitrary precision integer
	 * <i>unscaled value</i> and a 32-bit integer <i>scale</i>.  If zero
	 * or positive, the scale is the number of digits to the right of the
	 * decimal point.  If negative, the unscaled value of the number is
	 * multiplied by ten to the power of the negation of the scale.  The
	 * value of the number represented by the <code>BigDecimal</code> is
	 * therefore <code>(unscaledValue &#215; 10<sup>-scale</sup>)</code>.
	 * <br>
	 * <br>
	 * The <code>BigDecimal</code> class provides operations for
	 * arithmetic, scale manipulation, rounding, comparison and
	 * format conversion. The <code>toString()</code> method provides a
	 * canonical representation of a <code>BigDecimal</code>.
	 * <br>
	 * <br>
	 * The <code>BigDecimal</code> class gives its user complete control
	 * over rounding behavior.  If no rounding mode is specified and the
	 * exact result cannot be represented, an exception is thrown;
	 * otherwise, calculations can be carried out to a chosen precision
	 * and rounding mode by supplying an appropriate <code>MathContext</code>
	 * object to the operation.  In either case, eight <b>rounding
	 * modes</b> are provided for the control of rounding. The enumeration
	 * values of the <code>RoundingMode</code> should be used for such
	 * rounding operations.
	 * <br>
	 * <br>
	 * <i>This class is a partial ActionScript3 port of the Java
	 * class <code>java.math.BigDecimal</code> originally written by
	 * Josh Bloch, Mike Cowlishaw and Joseph D. Darcy, but relies on a
	 * completely different <code>BigInteger</code> implementation. It provides
	 * externalization methods that are meant to be used with specific GraniteDS
	 * serialization mechanisms.</i>
	 *
	 * @see Long
	 * @see BigInteger
	 * @see MathContext
	 * @see RoundingMode
	 * 
	 * @author Franck WOLFF
	 */
	public final class BigDecimal implements BigNumber {
		
		///////////////////////////////////////////////////////////////////////
		// Constants.
		
	    /**
	     * The value 0, with a scale of 0.
	     */
		public static const ZERO:BigDecimal = newBigDecimal(BigInteger.ZERO, 0, 1);
		
	    /**
	     * The value 1, with a scale of 0.
	     */
		public static const ONE:BigDecimal = newBigDecimal(BigInteger.ONE, 0, 1);
		
	    /**
	     * The value 10, with a scale of 0.
	     */
		public static const TEN:BigDecimal = newBigDecimal(BigInteger.TEN, 0, 2);
		
		///////////////////////////////////////////////////////////////////////
		// Fields.

		private var _integer:BigInteger;
		private var _scale:int;
		private var _precision:int;
		private var _string:String;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * Constructs a new <code>BigDecimal</code> instance according to the
		 * supplied parameter.
		 * <br>
		 * <br>
		 * The <code>value</code> parameter may be a String representation of
		 * a decimal value, an <code>int</code> primitive value, a <code>Number</code>,
		 * a <code>Long</code>, a <code>BigInteger</code> or even another
		 * <code>BigDecimal</code>:
		 * <br>
		 * <ul>
		 * <li><code>String</code>: the string representation consists
	     * of an optional sign, <code>'+'</code> or <code>'-'</code>, followed by
		 * a sequence of zero or more decimal digits ("the integer"), optionally
	     * followed by a fraction, optionally followed by an exponent.
		 * <br>
		 * The fraction consists of a decimal point followed by zero
	     * or more decimal digits.  The string must contain at least one
	     * digit in either the integer or the fraction.  The number formed
	     * by the sign, the integer and the fraction is referred to as the
	     * <i>significand</i>.
	     * <br>
	     * The exponent consists of the character <code>'e'</code> or <code>'E'</code>
	     * followed by one or more decimal digits. The value of the
	     * exponent must lie between <code>-int.MAX_VALUE</code> and
		 * <code>int.MAX_VALUE</code>, inclusive.</li>
		 * <li><code>int</code>: a primitive integer value.</li>
		 * <li><code>Number</code>: a primitive number value. Note that the provided
		 * number is first translated to its String representation in order to parsed
		 * as if it was a String parameter.</li>
		 * <li><code>BigDecimal</code>: the new BigInteger will be an exact copy of
		 * 		the specified parameter.</li>
		 * <li><code>null</code>: the new BigDecimal will be an exact copy of
		 * 		the constant <code>BigDecimal.ZERO</code>.</li>
		 * </ul>
		 * 
		 * @param value the value to be assigned to the new <code>BigDecimal</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>value</code>
		 * 		parameter is an invalid String representation. 
		 * @throws org.granite.math.IllegalArgumentError if the <code>value</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 */
		function BigDecimal(value:* = null) {
			if (value == null) {
				_integer = BigInteger.ZERO;
				_scale = 0;
				_precision = 1;
				_string = null;
			}
			else if (value is String)
				forString(this, value as String);
			else if (value is Number)
				forNumber(this, value as Number);
			else if (value is BigInteger) {
				_integer = value as BigInteger;
				_scale = 0;
				_precision = 0;
				_string = null;
			}
			else if (value is Long) {
				_integer = new BigInteger(value);
				_scale = 0;
				_precision = 0;
				_string = null;
			}
			else if (value is BigDecimal) {
				_integer = (value as BigDecimal)._integer;
				_scale = (value as BigDecimal)._scale;
				_precision = (value as BigDecimal)._precision;
				_string = (value as BigDecimal)._string;
			}
			else
				throw new IllegalArgumentError("Cannot construct a BigDecimal from: " + value);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Private static initializers.
		
		private static function forString(a:BigDecimal, value:String):void {
			
			function parseExp(dec:String, start:int):int {
				
				const value:String = dec.substring(start),
					length:int = value.length,
					zero:Number = "0".charCodeAt(0),
					nine:Number = "9".charCodeAt(0),
					max:Number = Number(int.MAX_VALUE);
				
				if (value.length == 0)
					throw new NumberFormatError("Zero length BigDecimal exponent: " + dec);
				
				var i:int = 0,
					sign:String = value.charAt(0),
					exp:Number = 0,
					c:Number;
				
				if (sign == "-" || sign == "+")
					i = 1;
				
				for ( ; i < length; i++) {
					c = value.charCodeAt(i);
					
					if (c < zero || c > nine)
						throw new NumberFormatError("Invalid exponent digit: " + dec);
					
					exp = (exp * 10) + (c - zero);
					
					if (exp > max)
						throw new NumberFormatError("Exponent out of range: " + dec);
				}
				
				return int(sign == "-" ? -exp : exp);
			}
			
			if (value == null || value.length == 0)
				throw new NumberFormatError("Zero length BigDecimal");
			
			const length:int = value.length,
				  idot:int = value.indexOf("."),
				  iexp:int = value.search(/e/i);
			
			if (iexp == 0 || (idot != -1 && iexp != -1 && idot > iexp))
				throw new NumberFormatError("Illegal BigDecimal format: " + value);
			
			var start:int = 0,
				end:int = length,
				sign:String = value.charAt(0),
				integer:String,
				scale:int = 0,
				exp:int = 0;
			
			// skip sign if any.
			if (sign == "-" || sign == "+")
				start = 1;
			
			// parse exponent if any.
			if (iexp != -1) {
				exp = parseExp(value, iexp + 1);
				end = iexp;
			}
			
			// get the unscaled value and initialize scale (right of the decimal point).
			if (idot == -1) {
				if (start == 0 && end == length)
					integer = value;
				else
					integer = value.substring(start, end);
			}
			else {
				integer = value.substring(start, idot).concat(value.substring(idot + 1, end));
				scale = end - idot - 1;
			}
			
			// compute scale (make sure scale - exp is in [int.MIN_VALUE, int.MAX_VALUE]).
			assert(scale >= 0);
			if (exp < 0 && scale > int.MAX_VALUE + exp)
				throw new NumberFormatError("Scale out of range: " + value);
			scale -= exp;
			
			// compute the unscaled value as a BigInteger.
			try {
				a._integer = BigInteger.asBigInteger(integer);
			}
			catch (e:BigNumberError) {
				throw new NumberFormatError("Illegal BigDecimal format: " + value);
			}
			if (sign == "-")
				a._integer = a._integer.negate();
			
			// store scale.
			a._scale = scale;
			
			// compute and store precision.
			a._precision = integer.length;
			if (integer.charAt(0) == "0") {
				start = integer.search(/[^0]/);
				if (start != -1)
					a._precision -= start;
				else
					a._precision = 1;
			}
		}
		
		private static function forNumber(a:BigDecimal, value:Number):void {
			if (isNaN(value))
				throw new IllegalArgumentError("Illegal NaN parameter");
			if (!isFinite(value))
				throw new IllegalArgumentError("Illegal infinite parameter");
			
			switch (value) {
				case 0:
					a._integer = BigInteger.ZERO;
					a._scale = 0;
					a._precision = 1;
					break;
				case 1:
					a._integer = BigInteger.ONE;
					a._scale = 0;
					a._precision = 1;
					break;
				case 10:
					a._integer = BigInteger.TEN;
					a._scale = 0;
					a._precision = 2;
					break;
				default:
					forString(a, value.toString(10));
					break;
			}
		}
		
		/**
		 * @private
		 */
		internal static function asBigDecimal(value:*):BigDecimal {
			if (value is BigDecimal)
				return value as BigDecimal;
			
			if (value is int) {
				switch (value as int) {
					case 0: return ZERO;
					case 1: return ONE;
					case 10: return TEN;
				} 
			}
			else if (value is String) {
				switch (value as String) {
					case "0": return ZERO;
					case "1": return ONE;
					case "10": return TEN;
				} 
			}
			
			return new BigDecimal(value);
		}
		
		private static function newBigDecimal(bi:BigInteger, scale:int, precision:int = 0):BigDecimal {
			assert(bi != null && precision >= 0);

			var a:BigDecimal = new BigDecimal();
			a._integer = bi;
			a._scale = scale;
			a._precision = precision;
			return a;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
	    /**
	     * The sign of this <code>BigDecimal</code> as an <code>int</code>, ie:
		 * -1, 0 or 1 as the value of this <code>BigDecimal</code> is negative,
		 * zero or positive.
	     */
		[Transient]
		public function get sign():int {
			return _integer.sign;
		}
		
	    /**
	     * The <i>scale</i> of this <code>BigDecimal</code>.  If zero
	     * or positive, the scale is the number of digits to the right of
	     * the decimal point.  If negative, the unscaled value of the
	     * number is multiplied by ten to the power of the negation of the
	     * scale.  For example, a scale of <code>-3</code> means the unscaled
	     * value is multiplied by 1000.
	     */
		[Transient]
		public function get scale():int {
			return _scale;
		}
		
	    /**
	     * The <i>precision</i> of this <code>BigDecimal</code> (the
	     * precision is the number of digits in the unscaled value.)
	     * <br>
	     * <br>
	     * The precision of a zero value is 1.
	     */
		[Transient]
		public function get precision():int {
			if (_precision == 0)
				_precision = bigDigitLength(_integer);
			return _precision;
		}
		
	    /**
	     * A <code>BigInteger</code> whose value is the <i>unscaled
	     * value</i> of this <code>BigDecimal</code>: computes
		 * <code>(this ~~ 10<sup>this.scale</sup>)</code>.
	     */
		[Transient]
		public function get unscaledValue():BigInteger {
			return _integer;
		}
		
	    /**
	     * The size of an ulp, a unit in the last place, of this
	     * <code>BigDecimal</code>. An ulp of a nonzero <code>BigDecimal</code>
	     * value is the positive distance between this value and the
	     * <code>BigDecimal</code> value next larger in magnitude with the
	     * same number of digits. An ulp of a zero value is numerically
	     * equal to 1 with the scale of <code>this</code>. The result is
	     * stored with the same scale as <code>this</code> so the result
	     * for zero and nonzero values is equal to <code>[1, this.scale]</code>.
	     */
		[Transient]
		public function get ulp():BigDecimal {
			return newBigDecimal(BigInteger.ONE, _scale, 1);
		}

		///////////////////////////////////////////////////////////////////////
		// Scaling.
		
	    /**
	     * Returns a <code>BigDecimal</code> whose scale is the specified
	     * value, and whose unscaled value is determined by multiplying or
	     * dividing this <code>BigDecimal</code>'s unscaled value by the
	     * appropriate power of ten to maintain its overall value. If the
	     * scale is reduced by the operation, the unscaled value must be
	     * divided (rather than multiplied), and the value may be changed;
	     * in this case, the specified rounding mode is applied to the
	     * division.
	     * 
	     * @param scale the scale of the <code>BigDecimal</code> value to be
		 * 		returned.
	     * @param round the rounding mode to apply (if <code>null</code>, the
		 * 		default value of <code>RoundingMode.UNNECESSARY</code> is used).
	     * @return a <code>BigDecimal</code> whose scale is the specified value, 
	     *      and whose unscaled value is determined by multiplying or 
	     *      dividing this <code>BigDecimal</code>'s unscaled value by the 
	     *      appropriate power of ten to maintain its overall value.
	     * @throws org.granite.math.ArithmeticError if
		 * 		<code>round == RoundingMode.UNNECESSARY</code> and the specified
		 * 		scaling operation would require rounding.
	     */
		public function setScale(scale:int, round:RoundingMode = null):BigDecimal {
			if (round == null)
				round = RoundingMode.UNNECESSARY;
			
			if (_scale == scale)
				return this;
			
			if (sign == 0)
				return newBigDecimal(BigInteger.ZERO, scale, 1);
			
			var pow:int;
			if (scale > _scale) {
				pow = checkScale(Number(scale) - Number(_scale));

				return newBigDecimal(
					_integer.multiplyByBigPowerOfTen(pow),
					scale,
					(_precision > 0 ? _precision + pow : 0)
				);
			}
			
			// scale < _scale.
			pow = checkScale(Number(_scale) - Number(scale));
			return divideAndRound(_integer, BigInteger.bigPowerOfTen(pow), scale, round, scale);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Unary operations.
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is the absolute value
	     * of this <code>BigDecimal</code>, with rounding according to the
	     * context settings. If context is omitted or null, the returned value
		 * is the absolute value of this <code>BigDecimal</code> with a scale
		 * of <code>this.scale</code>.
	     *
	     * @param mc the context to use.
	     * @return <code>abs(this)</code>, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but
		 * 		the rounding mode is <code>RoundingMode.UNNECESSARY</code>.
	     */
		public function abs(mc:MathContext = null):BigDecimal {
			if (mc == null)
				return (_integer.sign < 0 ? negate() : this);
			return (_integer.sign < 0 ? negate(mc) : plus(mc));
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(-this)</code>,
	     * with rounding according to the context settings. If context is omitted
		 * or null, no rounding is performed.
	     *
	     * @param mc the context to use.
	     * @return <code>-this</code>, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but
		 * 		the rounding mode is <code>RoundingMode.UNNECESSARY</code>.
	     */
		public function negate(mc:MathContext = null):BigDecimal {
			var b:BigDecimal = newBigDecimal(_integer.negate(), _scale, _precision);
			return (mc == null ? b : b.plus(mc));
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(+this)</code>,
	     * with rounding according to the context settings.
	     * <br>
	     * <br>
	     * The effect of this method is identical to that of the <code>round()</code>
		 * method.
	     *
	     * @param mc the context to use.
	     * @return <code>this</code>, rounded as necessary.  A zero result will
	     * 		have a scale of 0.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but
		 * 		the rounding mode is <code>RoundingMode.UNNECESSARY</code>.
		 * 
	     * @see #round()
	     */
		public function plus(mc:MathContext = null):BigDecimal {
			if (mc == null || mc.precision == 0)
				return this;
			return doRound(this, mc);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> which is equivalent to this one
	     * with the decimal point moved <code>n</code> places to the left.  If
	     * <code>n</code> is non-negative, the call merely adds <code>n</code>
		 * to the scale.  If <code>n</code> is negative, the call is equivalent
	     * to <code>movePointRight(-n)</code>.  The <code>BigDecimal</code>
	     * returned by this call has value <code>(this ~~ 10<sup>-n</sup>)</code>
		 * and scale <code>max(this.scale + n, 0)</code>.
	     *
	     * @param  n number of places to move the decimal point to the left.
	     * @return a <code>BigDecimal</code> which is equivalent to this one with
		 * 		the decimal point moved <code>n</code> places to the left.
	     * @throws org.granite.math.ArithmeticError if scale overflows.
	     */
		public function movePointLeft(n:int):BigDecimal {
			var scale:int = checkScale(Number(_scale) + Number(n)),
				moved:BigDecimal = newBigDecimal(_integer, scale, 0);
        	return (scale < 0 ? moved.setScale(0, RoundingMode.UNNECESSARY) : moved);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> which is equivalent to this one
	     * with the decimal point moved <code>n</code> places to the right.
	     * If <code>n</code> is non-negative, the call merely subtracts
	     * <code>n</code> from the scale.  If <code>n</code> is negative, the call
	     * is equivalent to <code>movePointLeft(-n)</code>.  The <code>BigDecimal</code>
		 * returned by this call has value <code>(this ~~ 10<sup>n</sup>)</code> and
		 * scale <code>max(this.scale - n, 0)</code>.
	     *
	     * @param  n number of places to move the decimal point to the right.
	     * @return a <code>BigDecimal</code> which is equivalent to this one
	     * 		with the decimal point moved <code>n</code> places to the right.
	     * @throws org.granite.math.ArithmeticError if scale overflows.
	     */
		public function movePointRight(n:int):BigDecimal {
			var scale:int = checkScale(Number(_scale) - Number(n)),
				moved:BigDecimal = newBigDecimal(_integer, scale, 0);
        	return (scale < 0 ? moved.setScale(0, RoundingMode.UNNECESSARY) : moved);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> rounded according to the
	     * <code>MathContext</code> settings. If the precision setting is
		 * 0 then no rounding takes place.
	     *
	     * @param mc the context to use.
	     * @return a <code>BigDecimal</code> rounded according to the 
	     * 		<code>MathContext</code> settings.
	     * @throws org.granite.math.ArithmeticError if the rounding mode is
	     * 		<code>RoundingMode.UNNECESSARY</code> and the
		 * 		<code>BigDecimal</code> operation would require rounding.
		 * 
	     * @see #plus()
	     */
		public function round(mc:MathContext):BigDecimal {
			return plus(mc);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose numerical value is equal to
	     * <code>(this ~~ 10<sup>n</sup>)</code>. The scale of the result is
		 * <code>(this.scale - n)</code>.
	     *
	     * @throws org.granite.math.ArithmeticError if the scale would be
	     * 		outside the range of a 32-bit integer.
	     */
		public function scaleByPowerOfTen(n:int):BigDecimal {
        	return newBigDecimal(_integer, checkScale(Number(_scale) - Number(n)), precision);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> which is numerically equal to
	     * this one but with any trailing zeros removed from the
	     * representation.  For example, stripping the trailing zeros from
	     * the <code>BigDecimal</code> value <code>600.0</code>, which has
	     * [<code>BigInteger</code>, <code>scale</code>] components equals to
	     * [6000, 1], yields <code>6E2</code> with [<code>BigInteger</code>,
		 * <code>scale</code>] components equals to [6, -2].
	     *
	     * @return a numerically equal <code>BigDecimal</code> with any
	     * 		trailing zeros removed.
	     */
		public function stripTrailingZeros():BigDecimal {
			var result:BigDecimal = newBigDecimal(_integer.clone(), _scale);
			// Number(Long.MIN_VALUE) -> -9223372036854776000.
	        result.stripZerosToMatchScale(Number("-9223372036854775808"));
	        return result;
		} 

		///////////////////////////////////////////////////////////////////////
		// Binary operations.
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(this + b)</code>,
	     * with rounding according to the context settings. If context is omitted,
		 * no rounding operation is performed.
	     * <br>
	     * <br>
	     * If either number is zero and the precision setting is nonzero then
	     * the other number, rounded if necessary, is used as the result.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value to be added to this <code>BigDecimal</code>.
	     * @param  mc the context to use.
	     * @return <code>this + b</code>, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but the
	     * 		rounding mode is <code>RoundingMode.UNNECESSARY</code>.
		 * 
		 * @see #BigDecimal()
	     */
		public function add(b:*, mc:MathContext = null):BigDecimal {
			var bd:BigDecimal = asBigDecimal(b);
			
			if (mc == null || mc.precision == 0)
				return _add(bd);

			var aIsZero:Boolean = (sign == 0),
				bIsZero:Boolean = (bd.sign == 0),
				scale:int,
				sum:BigDecimal;
			
			if (aIsZero || bIsZero) {
				scale = Math.max(_scale, bd._scale);
				
				if (aIsZero && bIsZero)
					return newBigDecimal(BigInteger.ZERO, scale);
				
				sum = (aIsZero ? doRound(bd, mc) : doRound(this, mc));
				
				if (sum._scale == scale)
					return sum;
				
				if (sum._scale > scale) {
					sum = newBigDecimal(sum._integer, sum._scale);
					sum.stripZerosToMatchScale(scale);
					return sum;
				}
				
				var precisionDiff:int = (mc.precision - sum.precision);
				
				if (precisionDiff >= (scale - sum._scale))
					return sum.setScale(scale);
				
				return sum.setScale(sum._scale + precisionDiff);
			}

			var padding:Number = Number(_scale) - Number(bd._scale),
				left:BigDecimal = this,
				right:BigDecimal = bd,
				aligned:Array;
			
			if (padding != 0) {
				aligned = preAlign(this, bd, padding, mc);
				matchScale(aligned);
				left = (aligned[0] as BigDecimal);
				right = (aligned[1] as BigDecimal);
			}
			
			sum = newBigDecimal(left._integer.add(right._integer), left._scale);

			return doRound(sum, mc);
		}
		
		private function _add(b:BigDecimal):BigDecimal {
			var scale:int = _scale,
				ds:Number = scale - b._scale,
				left:BigInteger = _integer,
				right:BigInteger = b._integer,
				raise:int;
			
			if (ds != 0) {
				if (ds < 0) {
					raise = checkScale(-ds);
					scale = b._scale;
					left = _integer.multiplyByBigPowerOfTen(raise);
				}
				else {
					raise = b.checkScale(ds);
					right = b._integer.multiplyByBigPowerOfTen(raise);
				}
			}

			return newBigDecimal(left.add(right), scale);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(this - b)</code>,
	     * with rounding according to the context settings. If context is omitted,
		 * no rounding operation is performed.
	     * <br>
	     * <br>
	     * If <code>b</code> is zero then this, rounded if necessary, is used as the
	     * result. If this is zero then the result is <code>b.negate(mc)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value to be subtracted from this <code>BigDecimal</code>.
	     * @param mc the context to use.
	     * @return <code>this - b</code>, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but the
	     * 		rounding mode is <code>RoundingMode.UNNECESSARY</code>.
		 * 
		 * @see #BigDecimal()
	     */
		public function subtract(b:*, mc:MathContext = null):BigDecimal {
			var bd:BigDecimal = asBigDecimal(b);
			if (mc == null || mc.precision == 0)
				return add(bd.negate());
			return add(bd.negate(), mc);
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(this ~~ b)</code>,
		 * with rounding according to the context settings. If context is omitted,
		 * no rounding operation is performed.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value to be multiplied by this <code>BigDecimal</code>.
	     * @param mc the context to use.
	     * @return <code>this ~~ b</code>, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if the result is inexact but the
	     * 		rounding mode is <code>RoundingMode.UNNECESSARY</code>.
		 * 
		 * @see #BigDecimal()
	     */
		public function multiply(b:*, mc:MathContext = null):BigDecimal {
			var bd:BigDecimal = asBigDecimal(b),
				scale:int = checkScale(Number(_scale) + Number(bd._scale)),
				product:BigInteger = _integer.multiply(bd._integer),
				result:BigDecimal = newBigDecimal(product, scale);
			
			return (mc == null || mc.precision == 0 ? result : doRound(result, mc));
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(this / b)</code>.
		 * <br>
		 * <br>
		 * Optional parameters are:
		 * <ul>
		 * <li><i>none</i>: the quotient will have a preferred scale of
		 * <code>(this.scale - b.scale)</code>: if the exact quotient cannot be
	     * represented (because it has a non-terminating decimal expansion) an
		 * <code>ArithmeticError</code> is thrown.</li>
		 * <li><code>MathContext</code>: rouding is performed rounding according to
		 * the context settings.</li>
		 * <li><code>RoundingMode</code>: rouding is performed rounding according to
		 * the supplied rounding mode.</li>
		 * <li><code>scale, RoundingMode</code>: scale of the result will be as
		 * specified. If rounding must be performed to generate a result with the
		 * specified scale, the specified rounding mode is applied.</li>
		 * </ul>
		 * 
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
		 * <br>
		 * <br>
	     * @param b value by which this <code>BigDecimal</code> is to be divided.
	     * @param args zero, one or two optional parameters as specified above.
	     * @return <code>this / b</code>.
	     * @throws org.granite.math.ArithmeticError if <code>b</code> is zero, or if 
		 * 		rounding mode is omitted or equals to <code>RoundingMode.UNNECESSARY</code>
		 * 		and the exact quotient does not have a terminating decimal expansion or
		 * 		cannot be scaled according to the scale parameter.
		 * 
		 * @see #BigDecimal()
	     */
		public function divide(b:*, ...args):BigDecimal {
			
			var bd:BigDecimal = asBigDecimal(b);

			if (args == null || args.length == 0)
				return _divide(bd);
			
			if (args.length == 1) {
				if (args[0] is MathContext)
					return _divideMc(bd, (args[0] as MathContext));
				if (args[0] is RoundingMode)
					return _divideRs(bd, _scale, (args[0] as RoundingMode));
				throw new IllegalArgumentError("Invalid parameter: " + args[0]);
			}
			
			if (args.length == 2) {
				if ((args[0] is int) && (args[1] is RoundingMode))
					return _divideRs(bd, (args[0] as int), (args[1] as RoundingMode));
				throw new IllegalArgumentError("Invalid parameters: " + args[0] + ", " +  + args[1]);
			}
			
			throw new IllegalArgumentError("Too many parameters: " + args.length);
		}
		
		private function _divide(b:BigDecimal):BigDecimal {
			
			if (b.sign == 0) {
				if (sign == 0)
					throw new ArithmeticError("Division undefined");
				throw new ArithmeticError("Division by zero");
			}

			var preferredScale:int = saturateNumber(Number(_scale) - Number(b._scale));
			if (sign == 0)
				return newBigDecimal(BigInteger.ZERO, preferredScale);

			var mc:MathContext, q:BigDecimal;
			
			mc = new MathContext(
				int(Math.min(precision + Math.ceil(10.0 * Number(b.precision) / 3.0), int.MAX_VALUE)),
				RoundingMode.UNNECESSARY
			);
			
			try {
				q = _divideMc(b, mc);
		    } catch (e:BigNumberError) {
				throw new ArithmeticError("Non-terminating decimal expansion; no exact representable decimal result.");
		    }
			
			if (q._scale < preferredScale)
				return q.setScale(preferredScale, RoundingMode.UNNECESSARY);
			
			return q;
		}
		
		private function _divideMc(b:BigDecimal, mc:MathContext):BigDecimal {
			
			if (mc.precision == 0)
				return _divide(b);
			
			if (b.sign == 0) {
				if (sign == 0)
					throw new ArithmeticError("Division undefined");
				throw new ArithmeticError("Division by zero");
			}
			
			var preferredScale:Number = Number(_scale) - Number(b._scale);
			if (sign == 0)
				return newBigDecimal(BigInteger.ZERO, saturateNumber(preferredScale), 1);
			
			var mcp:Number = Number(mc.precision),
				ascale:int = precision,
				bscale:int = b.precision,
				a:BigDecimal,
				q:BigDecimal,
				scale:int;
			
			a = newBigDecimal(_integer, ascale, ascale);
			b = newBigDecimal(b._integer, bscale, bscale);
			
			if (a.compareMagnitude(b) > 0) {
				b._scale--;
            	bscale = b._scale;
			}
			
			scale = checkScale(preferredScale + Number(bscale) - Number(ascale) + mcp);
	        
			if (checkScale(mcp + Number(bscale)) > Number(ascale))
	            a = a.setScale(int(mcp) + bscale, RoundingMode.UNNECESSARY);
	        else
	            b = b.setScale(checkScale(Number(ascale) - mcp), RoundingMode.UNNECESSARY);
	        
			q = divideAndRound(a._integer, b._integer, scale, mc.roundingMode, checkScale(preferredScale));

	        return doRound(q, mc);
		}
		
		private function _divideRs(b:BigDecimal, scale:int, round:RoundingMode):BigDecimal {
			var a:BigDecimal = this;
			
			if (checkScale(Number(scale) + Number(b._scale)) > Number(_scale)) 
	            a = setScale(scale + b._scale, RoundingMode.UNNECESSARY);
	        else
	            b = b.setScale(checkScale(Number(_scale) - scale), RoundingMode.UNNECESSARY);
			
			return divideAndRound(a._integer, b._integer, scale, round, scale);
		}
		
	    /**
	     * Returns a two-element <code>BigDecimal</code> array containing the
	     * result of <code>divideToIntegralValue</code> followed by the result of
	     * <code>remainder</code> on the two operands calculated with rounding
	     * according to the context settings.
	     * <br>
	     * <br>
	     * Note that if both the integer quotient and remainder are needed, this
		 * method is faster than using the <code>divideToIntegralValue</code> and
		 * <code>remainder</code> methods separately because the division need only
		 * be carried out once.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value by which this <code>BigDecimal</code> is to be divided, 
	     *         and the remainder computed.
	     * @param mc the context to use.
	     * @return a two element <code>BigDecimal</code> array: the quotient 
	     *         (the result of <code>divideToIntegralValue</code>) is the 
	     *         initial element and the remainder is the final element.
	     * @throws org.granite.math.ArithmeticError if <code>b</code> is zero or if
		 * 		the result is inexact but the rounding mode is
		 * 		<code>RoundingMode.UNNECESSARY</code>, or
		 * 		<code>mc.precision &gt; 0</code> and the result of
		 * 		<code>this.divideToIntgralValue(b)</code> would require a precision
		 * 		of more than <code>mc.precision</code> digits.
		 * 
	     * @see #divideToIntegralValue()
	     * @see #remainder()
	     */
		public function divideAndRemainder(b:*, mc:MathContext = null):Array {
			var bd:BigDecimal = asBigDecimal(b),
				q:BigDecimal = divideToIntegralValue(bd, mc);
			return [q, subtract(q.multiply(bd))];
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is <code>(this % b)</code>,
		 * with rounding according to the context settings. The <code>MathContext</code>
		 * settings affect the implicit divide used to compute the remainder. The
		 * remainder computation itself is by definition exact. Therefore, the remainder
		 * may contain more than <code>mc.precision</code> digits.
	     * <br>
	     * <br>
	     * The remainder is given by
	     * <code>this.subtract(this.divideToIntegralValue(b, mc).multiply(b))</code>. Note
		 * that this is not the modulo operation (the result can be negative).
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value by which this {@code BD} is to be divided.
	     * @param  mc the context to use.
	     * @return {@code this % divisor}, rounded as necessary.
	     * @throws org.granite.math.ArithmeticError if <code>b</code> is zero or if rounding
		 * 		mode is <code>RoundingMode.UNNECESSARY</code>, or <code>mc.precision &gt; 0</code> 
	     *      and the result of <code>this.divideToIntgralValue(b)</code> would require a
		 * 		precision of more than <code>mc.precision</code> digits.
		 * 
	     * @see #divideToIntegralValue()
	     */
		public function remainder(b:*, mc:MathContext = null):BigDecimal {
			return divideAndRemainder(asBigDecimal(b), mc)[1];
		}
		
	    /**
	     * Returns a <code>BigDecimal</code> whose value is the integer part
	     * of <code>(this / b)</code>. Since the integer part of the exact
		 * quotient does not depend on the rounding mode, the rounding mode
		 * does not affect the values returned by this method. The preferred
		 * scale of the result is <code>(this.scale - b.scale())</code>. An
	     * <code>ArithmeticError</code> is thrown if the integer part of the
		 * exact quotient needs more than <code>mc.precision</code> digits.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param b value by which this <code>BigDecimal</code> is to be divided.
	     * @param mc the context to use.
	     * @return The integer part of <code>this / b</code>.
	     * @throws org.granite.math.ArithmeticError if <code>b</code> is zero
		 * 		or if <code>mc.precision &gt; 0</code> and the result requires
		 * 		a precision of more than <code>mc.precision</code> digits.
	     */
		public function divideToIntegralValue(b:*, mc:MathContext = null):BigDecimal {
			var bd:BigDecimal = asBigDecimal(b);
			
			if (mc == null || mc.precision == 0 || compareMagnitude(bd) < 0)
				return _divideToIntegralValue(bd);
			
			var preferredScale:int = saturateNumber(Number(_scale) - Number(bd._scale)),
				result:BigDecimal = divide(bd, new MathContext(mc.precision, RoundingMode.DOWN)),
				dp:int;

			if (result._scale < 0) {
				if (subtract(result.multiply(bd)).compareMagnitude(bd) >= 0)
					throw new ArithmeticError("Division impossible");
			}
			else if (result._scale > 0)
			    result = result.setScale(0, RoundingMode.DOWN);
		
			if (preferredScale > result._scale && (dp = mc.precision - result.precision) > 0)
			    return result.setScale(result._scale + Math.min(dp, preferredScale - result._scale));

			result.stripZerosToMatchScale(preferredScale);
		    return result;
		}
		
		private function _divideToIntegralValue(b:BigDecimal):BigDecimal {

			var preferredScale:int = saturateNumber(Number(_scale) - Number(b._scale));

			if (compareMagnitude(b) < 0)
		        return newBigDecimal(BigInteger.ZERO, preferredScale);
		
			if (sign == 0 && b.sign != 0)
			    return setScale(preferredScale, RoundingMode.UNNECESSARY);
			
			var maxDigits:int,
				quotient:BigDecimal;
			
			maxDigits = int(Math.min(
				Number(precision) +
				Math.ceil(10.0 * Number(b.precision) / 3.0) +
				Math.abs(Number(_scale) - Number(b._scale)) +
				2,
				int.MAX_VALUE
			));
			
			quotient = divide(b, new MathContext(maxDigits, RoundingMode.DOWN));

			if (quotient._scale > 0) {
			    quotient = quotient.setScale(0, RoundingMode.DOWN);
		        quotient.stripZerosToMatchScale(preferredScale);
			}
			
			if (quotient._scale < preferredScale)
			    quotient = quotient.setScale(preferredScale, RoundingMode.UNNECESSARY);
		
			return quotient;
		}

		///////////////////////////////////////////////////////////////////////
		// Comparisons.

	    /**
	     * Compares this <code>BigDecimal</code> with the specified
	     * <code>BigDecimal</code>. Two <code>BigDecimal</code> objects that are
	     * equal in value but have a different scale (like 2.0 and 2.00)
	     * are considered equal by this method. This method is provided
	     * in preference to individual methods for each of the six boolean
	     * comparison operators (&lt;, ==, &gt;, &gt;=, !=, &lt;=).  The
	     * suggested idiom for performing these comparisons is:
	     * <code>(x.compareTo(y) &lt;<i>op</i>&gt; 0)</code>, where
	     * &lt;<i>op</i>&gt; is one of the six comparison operators.
	     *
	     * @param b <code>BigDecimal</code> to which this <code>BigDecimal</code>
		 * 		is to be compared.
	     * @param unsigned if <code>true</code>, the comparison will be between
		 * 		absolute values of <code>this</code> and <code>b</code>.
	     * @return -1, 0, or 1 as this <code>BigDecimal</code> is numerically 
	     *          less than, equal to, or greater than <code>b</code>.
	     */
		public function compareTo(b:BigDecimal, unsigned:Boolean = false):int {
			if (unsigned)
				return compareMagnitude(b);
			
	        var asign:int = sign,
				bsign:int = b.sign;

	        if (asign != bsign)
	            return (asign > bsign) ? 1 : -1;

			if (asign == 0)
	            return 0;

			var cmp:int = compareMagnitude(b);
	        return (asign > 0) ? cmp : -cmp;
	    }
		
	    /**
	     * Compares this <code>BigDecimal</code> with the specified object for
		 * equality.  Unlike <code>compareTo</code>, this method considers two
	     * <code>BigDecimal</code> objects equal only if they are equal in
	     * value and scale (thus 2.0 is not equal to 2.00 when compared by
	     * this method).
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigDecimal</code> constructor documentation.
	     *
	     * @param  b the bject to which this <code>BigDecimal</code> is 
	     *         to be compared.
	     * @return <code>true</code> if and only if the specified object is a
	     *         <code>BigDecimal</code> whose value and scale are equal to this 
	     *         <code>BigDecimal</code>'s.
		 * 
	     * @see #compareTo()
	     */
		public function equals(b:*):Boolean {
			try {
				var bd:BigDecimal = asBigDecimal(b);
				return (_scale == bd._scale && compareTo(bd) == 0);
			}
			catch (e:BigNumberError) {
			}
			return false;
		}
		
	    /**
	     * Returns the maximum of this <code>BigDecimal</code> and <code>b</code>.
	     *
	     * @param b value with which the maximum is to be computed.
	     * @return the <code>BigDecimal</code> whose value is the greater of this 
	     *      <code>BigDecimal</code> and <code>b</code>. If they are equal, 
	     *      as defined by the <code>compareTo</code> method, <code>this</code>
		 * 		is returned.
		 * 
	     * @see #compareTo()
	     */
		public function max(b:BigDecimal):BigDecimal {
			return (compareTo(b) >= 0 ? this : b);
		}
		
	    /**
	     * Returns the minimum of this <code>BigDecimal</code> and <code>b</code>.
	     *
	     * @param b value with which the minimum is to be computed.
	     * @return the <code>BigDecimal</code> whose value is the lesser of this 
	     * 		<code>BigDecimal</code> and <code>b</code>. If they are equal, 
	     *      as defined by the <code>compareTo</code> method, <code>this</code>
		 * 		is returned.
		 * 
	     * @see #compareTo()
	     */
		public function min(b:BigDecimal):BigDecimal {
			return (compareTo(b) <= 0 ? this : b);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conversions.
		

	    /**
	     * Converts this <code>BigDecimal</code> to a <code>Number</code>. This
	     * conversion is done by converting this <code>BigInteger</code> to signed
		 * String representation and constructing a new <code>Number</code> from
		 * this representation: if this <code>BigDecimal</code> has a too great a
		 * magnitude to be represented as a <code>Number</code>, it will be
	     * converted to <code>Number.NEGATIVE_INFINITY</code> or
		 * <code>Number.POSITIVE_INFINITY</code> as appropriate. Note that even when
	     * the return value is finite, this conversion can lose information about
		 * the precision of the <code>BigDecimal</code> value.
	     * 
	     * @return this <code>BigDecimal</code> converted to a <code>Number</code>.
	     */
		public function toNumber():Number {
			return Number(toString());
		}
		
	    /**
	     * Converts this <code>BigDecimal</code> to an <code>int</code>: any
		 * fractional part of this <code>BigDecimal</code> will be discarded,
		 * and if the resulting <code>BigInteger</code> is too big to fit in an
	     * <code>int</code>, only the low-order 31 bits are returned, signed as
		 * this <code>BigDecimal</code> was.
	     * 
	     * @return this <code>BigDecimal</code> converted to an <code>int</code>.
		 * 
		 * @see BigInteger#toInt()
		 * @see #toIntExact()
	     */
		public function toInt():int {
			return toBigInteger().toInt();
		}

	    /**
	     * Converts this <code>BigDecimal</code> to an <code>int</code>, checking
	     * for lost information. If this <code>BigDecimal</code> has a nonzero
		 * fractional part or is out of the possible range for an <code>int</code>
		 * result then an <code>ArithmeticError</code> is thrown.
	     *
	     * @return this <code>BigDecimal</code> converted to an <code>int</code>.
	     * @throws org.granite.math.ArithmeticError if <code>this</code> has a nonzero
	     *         fractional part, or will not fit in an <code>int</code>.
	     */
		public function toIntExact():int {
			var b:BigInteger = toBigIntegerExact(),
				i:int = b.toInt();
			if (!b.equals(new BigInteger(i)))
				throw new ArithmeticError("Overflow");
			return i;
		}
		
	    /**
	     * Converts this <code>BigDecimal</code> to a <code>BigInteger</code>: any
		 * fractional part of this <code>BigDecimal</code> will be discarded. Note
		 * that this conversion can lose information about the precision of the
	     * <code>BigDecimal</code> value.
	     * <br>
	     * <br>
	     * To have an exception thrown if the conversion is inexact (in
	     * other words if a nonzero fractional part is discarded), use the
	     * <code>toBigIntegerExact</code> method.
	     *
	     * @return this {@code BD} converted to a {@code BI}.
		 * 
		 * @see #toBigIntegerExact()
	     */
		public function toBigInteger():BigInteger {
			return setScale(0, RoundingMode.DOWN).unscaledValue;
		}
		
	    /**
	     * Converts this <code>BigDecimal</code> to a <code>BigInteger</code>,
	     * checking for lost information. An exception is thrown if this
	     * <code>BigDecimal</code> has a nonzero fractional part.
	     *
	     * @return this <code>BigDecimal</code> converted to a <code>BigInteger</code>.
	     * @throws org.granite.math.ArithmeticError if <code>this</code> has a nonzero
	     * 		fractional part.
	     */
		public function toBigIntegerExact():BigInteger {
			return setScale(0, RoundingMode.UNNECESSARY).unscaledValue;
		}
		
	    /**
	     * Returns a string representation of this <code>BigDecimal</code>
	     * without an exponent field. For values with a positive scale,
	     * the number of digits to the right of the decimal point is used
	     * to indicate scale. For values with a zero or negative scale,
	     * the resulting string is generated as if the value were
	     * converted to a numerically equal value with zero scale and as
	     * if all the trailing zeros of the zero scale value were present
	     * in the result.
	     * <br>
	     * <br>
	     * The entire string is prefixed by a minus sign character '-'
	     * if the unscaled value is less than zero. No sign character is
		 * prefixed if the unscaled value is zero or positive.
	     * <br>
	     * <br>
	     * Note that if the result of this method is passed to the
	     * <code>BigDecimal</code> constructor, only the numerical value of
		 * this <code>BigDecimal</code> will necessarily be recovered; the
		 * representation of the new <code>BigDecimal</code> may have a
		 * different scale. In particular, if this <code>BigDecimal</code>
		 * has a negative scale, the string resulting from this method will
		 * have a scale of zero when processed by the string constructor.
	     *
	     * @return a string representation of this <code>BigDecimal</code>
	     * 		without an exponent field.
	     * 
	     * @see #toString()
	     */
		public function toPlainString():String {
			var b:BigDecimal = this;
			
			if (_scale < 0)
				b = b.setScale(0);
			
			if (b.scale == 0)
				return b._integer.toString(10);
			
			var string:String = b._integer.toString(10, true),
				idot:int = string.length - b._scale;
			
			if (idot == 0)
				return (b.sign < 0 ? "-0." : "0.") + string;
			
			if (idot > 0)
				return (b.sign < 0 ? "-" : "") + string.substring(0, idot) + "." + string.substring(idot);
			
			// idot < 0, insert zeros.
			return (b.sign < 0 ? "-0." : "0.") + Radix.getZeros(-idot) + string;
		}
		
	    /**
	     * Returns the string representation of this <code>BigDecimal</code>,
	     * using scientific notation if an exponent is needed.
	     * <br>
	     * <br>
	     * A standard canonical string form of the <code>BigDecimal</code>
	     * is created as though by the following steps: first, the
	     * absolute value of the unscaled value of the <code>BigDecimal</code>
	     * is converted to a string in base ten using the characters
	     * '0' through '9' with no leading zeros (except if its value is zero,
		 * in which case a single '0' character is used).
	     * <br>
	     * <br>
	     * Next, an <i>adjusted exponent</i> is calculated; this is the
	     * negated scale, plus the number of characters in the converted
	     * unscaled value, less one. That is,
	     * <code>-scale + (ulength - 1)</code>, where <code>ulength</code> is
		 * the length of the absolute value of the unscaled value in decimal
	     * digits (its <i>precision</i>).
	     * <br>
	     * <br>
	     * If the scale is greater than or equal to zero and the
	     * adjusted exponent is greater than or equal to <code>-6</code>, the
	     * number will be converted to a character form without using
	     * exponential notation. In this case, if the scale is zero then
	     * no decimal point is added and if the scale is positive a
	     * decimal point will be inserted with the scale specifying the
	     * number of characters to the right of the decimal point.
	     * '0' characters are added to the left of the converted
	     * unscaled value as necessary. If no character precedes the
	     * decimal point after this insertion then a conventional
	     * '0' character is prefixed.
	     * <br>
	     * <br>
	     * Otherwise (that is, if the scale is negative, or the
	     * adjusted exponent is less than <code>-6</code>), the number will be
	     * converted to a character form using exponential notation.  In
	     * this case, if the converted <code>BigInteger</code> has more than
	     * one digit a decimal point is inserted after the first digit.
	     * An exponent in character form is then suffixed to the converted
	     * unscaled value (perhaps with inserted decimal point); this
	     * comprises the letter 'E' followed immediately by the
	     * adjusted exponent converted to a character form.  The latter is
	     * in base ten, using the characters '0' through '9' with no leading
		 * zeros, and is always prefixed by a sign character '-' if the
	     * adjusted exponent is negative, '+' otherwise.
	     * <br>
	     * <br>
	     * Finally, the entire string is prefixed by a minus sign character '-'
		 * if the unscaled value is less than zero. No sign character is prefixed
		 * if the unscaled value is zero or positive.
	     * <br>
	     * <br>
	     * <b>Examples:</b>
		 * <br>
		 * <br>
	     * For each representation [<i>unscaled value</i>, <i>scale</i>]
	     * on the left, the resulting string is shown on the right.
	     * <pre>
	     * [123,0]      "123"
	     * [-123,0]     "-123"
	     * [123,-1]     "1.23E+3"
	     * [123,-3]     "1.23E+5"
	     * [123,1]      "12.3"
	     * [123,5]      "0.00123"
	     * [123,10]     "1.23E-8"
	     * [-123,12]    "-1.23E-10"
	     * </pre>
	     *
	     * <b>Notes:</b>
	     * <ul>
	     *
	     * <li>There is a one-to-one mapping between the distinguishable
	     * <code>BigDecimal</code> values and the result of this conversion.
	     * That is, every distinguishable <code>BigDecimal</code> value
	     * (unscaled value and scale) has a unique string representation
	     * as a result of using <code>toString(false)</code>. If that string
	     * representation is converted back to a <code>BigDecimal</code> using
	     * the <code>BigDecimal</code> constructor, then the original
	     * value will be recovered.</li>
	     * 
	     * <li>The string produced for a given number is always the same;
	     * it is not affected by locale.</li>
	     * 
	     * <li>The <code>engineering</code> parameter may be used for
	     * presenting numbers with exponents in engineering notation: if
	     * exponential notation is used, the power of ten is adjusted to
	     * be a multiple of three (engineering notation) such that the
	     * integer part of nonzero values will be in the range 1 through
	     * 999.</li>
		 * 
		 * <li>The <code>setScale</code> method may be used for rounding a
		 * <code>BigDecimal</code> so it has a known number of digits after
	     * the decimal point.</li>
	     *
	     * </ul>
	     *
	     * @return the string representation of this <code>BigDecimal</code>.
		 * 
	     * @see #BigDecimal()
	     */
		public function toString(engineering:Boolean = false):String {

			if (_string != null)
				return _string;

			// cache the string representation of this BigDecimal if it doesn't
			// use a specific engeenering format.
			var cache:Boolean = true;
			
			if (_scale == 0)
				_string = _integer.toString(10);
			else {
				_string = _integer.toString(10, true);
				
				// use number (int could overflow)...
				var adjusted:Number = -Number(_scale) + Number(_string.length - 1);

				if (_scale > 0 && adjusted >= -6) {
					// _scale (> 0) - string.length (> 0) --> cannot overflow.
					const padding:int = _scale - _string.length;

					if (padding >= 0)
						_string = (sign < 0 ? "-" : "") + "0." + Radix.getZeros(padding) + _string;
					else
						_string = (sign < 0 ? "-" : "") + _string.substr(0, -padding) + "." + _string.substr(-padding);
				}
				else {
					if (!engineering) {
						if (_string.length > 1)
							_string = _string.charAt(0) + "." + _string.substring(1);
					}
					else {
						cache = false;
						
						var sig:int = int(adjusted % 3);
						if (sig < 0)
							sig += 3;
						adjusted -= sig;
						
						if (sign == 0) {
							switch (sig) {
								case 1:
									_string = "0";
									break;
								case 2:
									_string = "0.00";
									adjusted += 3;
									break;
								case 3:
									_string = "0.0";
									adjusted += 3;
									break;
								default:
									assert(false, "Unexpected sig value: " + sig);
							}
						}
						else if (sig >= _string.length)
							_string += Radix.getZeros(sig - _string.length);
						else
							_string = _string.substr(0, sig) + "." + _string.substr(sig);
					}

					if (sign < 0)
						_string = "-" + _string;

					if (adjusted != 0) {
						_string += "E";
						if (adjusted > 0)
							_string += "+";
						_string += adjusted.toFixed(0);
					}
				}
			}

			if (cache)
				return _string;
			
			// reset string cache if a specific engeenering format was used.
			var string:String = _string;
			_string = null;
			return string;
		}
		
		///////////////////////////////////////////////////////////////////////
		// IExternalizable implementation.
		
		/**
		 * @private
		 */
		public function readExternal(input:IDataInput):void {
			forString(this, String(input.readObject()));
		}
		
		/**
		 * @private
		 */
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(toString());
		}
		
		///////////////////////////////////////////////////////////////////////
		// Instance utilities.
		
		private function checkScale(scale:Number):int {
			if (scale < int.MIN_VALUE || scale > int.MAX_VALUE) {
				if (sign == 0)
					return (scale < 0 ? int.MIN_VALUE : int.MAX_VALUE);
				throw new ArithmeticError(scale < 0 ? "Scale underflow" : "Scale overflow");
			}
			return scale;
		}
		
		private function preAlign(a:BigDecimal, b:BigDecimal, padding:Number, mc:MathContext):Array {
			
			assert(padding != 0);
			
			var big:BigDecimal,
				small:BigDecimal,
				scale:Number,
				position:Number;
			
			if (padding < 0) {
				big = a;
				small = b;
			}
			else {
				big = b;
				small = a;
			}
			
			scale = Number(big._scale) - Number(big.precision) + Number(mc.precision);
			position = Number(small._scale) - Number(small.precision) + Number(1);
			if (position > (Number(big._scale) + Number(2)) && position > scale + Number(2))
	    		small = newBigDecimal(new BigInteger(small.sign), checkScale(Math.max(big.scale, scale) + Number(3)));
			
			return [big, small];
		}
		
		private function stripZerosToMatchScale(preferredScale:Number):BigDecimal {

			var remainder:uint,
				copy:MutableBigInteger;
			
			while (_integer.compareTo(BigInteger.TEN, true) >= 0 && _scale > preferredScale) {
				if (_integer.testBit(0))
					break;

				copy = _integer.mbi.clone();
				remainder = _integer.mbi.divideByUint(10);

				if (remainder != 0) {
					_integer.mbi = copy;
					break;
				}

				_scale = checkScale(Number(_scale) - 1);

				if (_precision > 0)
					_precision--;
			}
			
			return this;
		}
		
		private function compareMagnitude(b:BigDecimal):int {
			var ds:int = _scale - b._scale;
	        
			if (ds != 0) {
				
				var aps:int = precision - _scale,
					bps:int = b.precision - b._scale;
				
				if (aps < bps)
					return -1;
				if (aps > bps)
					return 1;
				
				var rb:BigInteger = null;
				
				if (ds < 0) {
					rb = _integer.multiplyByBigPowerOfTen(-ds);
					return rb.compareTo(b._integer, true);
				}
				
				rb = b._integer.multiplyByBigPowerOfTen(ds);
				return _integer.compareTo(rb, true);
	        }
	        
			return _integer.compareTo(b._integer, true);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static utilities.

		private static function matchScale(values:Array):void {
			const a:BigDecimal = values[0],
				  b:BigDecimal = values[1];

			if (a._scale == b._scale)
	            return;
	        
			if (a._scale < b._scale)
	            values[0] = a.setScale(b._scale, RoundingMode.UNNECESSARY);
	        else
	            values[1] = b.setScale(a._scale, RoundingMode.UNNECESSARY);
	    }
		
		private static function doRound(d:BigDecimal, mc:MathContext):BigDecimal {
			var precision:int = mc.precision,
				drop:int,
				scale:int;
			
			while ((drop = d.precision - precision) > 0) {
				scale = d.checkScale(Number(d._scale) - Number(drop));
				d = divideAndRound(
					d._integer,
					BigInteger.bigPowerOfTen(drop),
					scale,
					mc.roundingMode,
					scale
				);
			}
			
			return d;
		}
		
		private static function divideAndRound(a:BigInteger,
											   b:BigInteger,
											   scale:int,
											   round:RoundingMode,
											   preferredScale:int):BigDecimal {
			
			var remainder0:Boolean = false,
				increment:Boolean = false,
				sign:int = 0,
				quotient:BigInteger,
				qr:Array,
				result:BigDecimal;
				
			qr = a.divideAndRemainder(b);
			quotient = (qr[0] as BigInteger);
			remainder0 = ((qr[1] as BigInteger).sign == 0);
			sign = (a.sign != b.sign ? -1 : 1);
			
			if (!remainder0) {
				switch (round) {
					case RoundingMode.UNNECESSARY:
						throw new ArithmeticError("Rounding necessary");
					case RoundingMode.UP:
						increment = true;
						break;
					case RoundingMode.DOWN:
						increment = false;
						break;
					case RoundingMode.CEILING:
						increment = (sign > 0);
						break;
					case RoundingMode.FLOOR:
						increment = (sign < 0);
						break;
					default:
						var cmp:int = (qr[1] as BigInteger).multiply(2).compareTo(b, true);
		                if (cmp < 0)
		                    increment = false;
		                else if (cmp > 0)
		                    increment = true;
		                else if (round == RoundingMode.HALF_UP)
		                    increment = true;
		                else if (round == RoundingMode.HALF_DOWN)
		                    increment = false;
		                else  // round == RoundingMode.HALF_EVEN
		                    increment = quotient.mbi.isOdd();
						break;
				}
				if (increment)
					quotient = (quotient.sign == -1 ? quotient.subtract(BigInteger.ONE) : quotient.add(BigInteger.ONE));
			}
			
			if (quotient.sign * sign == -1)
				quotient = quotient.negate();
			
			result = newBigDecimal(quotient, scale);
			
			if (remainder0 && preferredScale != scale)
				result.stripZerosToMatchScale(preferredScale);
			
			return result;
		}

		private static function saturateNumber(n:Number):int {
	        var i:int = int(n);
	        return (n == i) ? i : (n < 0 ? int.MIN_VALUE : int.MAX_VALUE); 
	    }
		
		private static function bigDigitLength(b:BigInteger):int {
	        if (b.sign == 0)
	            return 1;
			// TODO: improve performances...
			return b.toString(10, true).length;
	    }
	}
}