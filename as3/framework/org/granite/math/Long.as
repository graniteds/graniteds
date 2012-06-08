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

	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	
	[RemoteClass(alias="java.lang.Long")]
    /**
	 * An ActionScript3 equivalent to the signed 64bits <code>long</code> type
	 * in Java. All arithmetic operations are provided, as well as bitwise, bit
	 * shift and comparison operator equivalents.
	 * <br>
	 * <br>
	 * <i>This class provides externalization methods that are meant to be used
	 * with specific GraniteDS serialization mechanisms. Its main purpose is to
	 * give a reliable serialization behavior between ActionScript3 and Java
	 * <code>long</code> type: standard serialization uses the ActionScript3
	 * <code>Number</code> type, with possible lose of precision.</i>
	 * 
     * @author Franck WOLFF
	 * 
	 * @see BigInteger
	 * @see BigDecimal
     */
	public final class Long implements BigNumber {
		
		///////////////////////////////////////////////////////////////////////
		// Constants.

		public static const MAX_VALUE:Long = newLong(0x7fffffff, 0xffffffff, false);
		public static const MIN_VALUE:Long = newLong(0x80000000, 0x00000000, false);

		/**
		 * The <code>Long</code> constant zero.
		 */
		public static const ZERO:Long = newLong(0x00000000, 0x00000000, false);

		/**
		 * The <code>Long</code> constant one.
		 */
		public static const ONE:Long = newLong(0x00000000, 0x00000001, false);

		/**
		 * The <code>Long</code> constant ten.
		 */
		public static const TEN:Long = newLong(0x00000000, 0x0000000a, false);
		
		///////////////////////////////////////////////////////////////////////
		// Fields.

		private var _u0:uint = 0;
		private var _u1:uint = 0;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.

		/**
		 * Constructs a new <code>Long</code> instance according to the
		 * supplied parameters.
		 * <br>
		 * <br>
		 * The <code>value</code> parameter may be a String representation of
		 * an integer, a <code>int</code> primitive value, a <code>Number</code>
		 * or even another <code>Long</code>:
		 * <br>
		 * <ul>
		 * <li><code>String</code>: it must be a not empty String in the form of
		 * 		<i>-?[0-9a-zA-Z]+</i> (ie: an optional '-' character followed by
		 * 		a not empty sequence of digits (with radix >= 2 and <= 36)). When
		 * 		a String value is used, the second parameter <code>radix</code>
		 * 		is also used, and the supplied digits must be in accordance with
		 * 		the specified radix.</li>
		 * <li><code>int</code>: a primitive integer value (radix ignored).</li>
		 * <li><code>Number</code>: a primitive number value (radix ignored). Only
		 * 		the fixed part of the number will be used (decimal part is
		 *      ignored).</li>
		 * <li><code>Long</code>: the new Long will be an exact copy of
		 * 		the specified parameter.</li>
		 * <li><code>null</code>: the new Long will be an exact copy of
		 * 		the constant <code>Long.ZERO</code>.</li>
		 * </ul>
		 * 
		 * @param value the value to be assigned to the new <code>Long</code>.
		 * @param radix the radix (2 <= radix <= 36) to be used for string conversion
		 * 		(ignored if the <code>value</code> parameter isn't a string).
		 * @throws org.granite.math.NumberFormatError if the <code>value</code>
		 * 		parameter is an invalid String representation or it doesn't fit into
		 * 		a signed 64bits type. 
		 * @throws org.granite.math.IllegalArgumentError if the <code>value</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 */
		function Long(value:* = null, radix:int = 10) {
			if (value != null) {
				if (value is Long) {
					_u0 = (value as Long)._u0;
					_u1 = (value as Long)._u1;
				}
				else if (value is String)
					forString(this, value as String, radix);
				else if (value is int)
					forInt(this, value as int);
				else if (value is Number)
					forNumber(this, value as Number);
				else
					throw new IllegalArgumentError("Cannot construct a Long from: " + value);
			}
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static initializers.
		
		private static function forString(a:Long, value:String, radix:int = 10):void {
			if (value == null || value.length == 0)
				throw new NumberFormatError("Zero length Long");

			var first:String = value.charAt(0);
			if (first === "-")
				value = value.substr(1);
			else
				first = "";
			
			var mbi:MutableBigInteger = MutableBigInteger.forString(value, radix),
				uints:Uints = mbi.uints,
				length:int = uints.length;
			
			if (length > 2)
				throw new NumberFormatError("Too large value for a Long: " + first + value);
			
			if (length > 0)
				a._u0 = uints[0];
			if (length > 1)
				a._u1 = uints[1];

			if (first === "-") {
				if (a._u1 == 0x80000000 && a._u0 == 0x00000000)
					return;
				
				if ((a._u1 & 0x80000000) != 0)
					throw new NumberFormatError("Too large value for a Long: -" + value);

				if (a._u0 == 0)
					a._u1 = (0x80000000 | ((~(a._u1)) + 1));
				else {
					a._u0 = (~(a._u0)) + 1;
					a._u1 = (0x80000000 | (~(a._u1)));
				}
			}
			else if ((a._u1 & 0x80000000) != 0)
				throw new NumberFormatError("Too large value for a Long: " + value);
		} 
		
		private static function forInt(a:Long, value:int):void {
			if (value < 0) {
				a._u1 = 0xffffffff;
				a._u0 = (~(-value)) + 1;
			}
			else {
				a._u1 = 0;
				a._u0 = uint(value);
			}
		} 
		
		private static function forNumber(a:Long, value:Number):void {
			if (isNaN(value))
				throw new IllegalArgumentError("Illegal NaN parameter");
			if (!isFinite(value))
				throw new IllegalArgumentError("Illegal infinite parameter");
			
			forString(a, value.toFixed(0));
		}

		private static function newLong(u1:uint, u0:uint, constants:Boolean = true):Long {
			if (constants) {
				if (u1 == 0) {
					if (u0 == 0)
						return ZERO;
					if (u0 == 1)
						return ONE;
					if (u0 == 10)
						return TEN;
				}
				else if (u1 == 0x80000000) {
					if (u0 == 0x00000000)
						return MIN_VALUE;
				}
				else if (u1 == 0x7fffffff && u0 == 0xffffffff)
					return MAX_VALUE;
			}
			
			var l:Long = new Long();
			l._u0 = u0;
			l._u1 = u1;
			return l;
		}
		
		private static function asLong(value:*):Long {
			if (value is Long)
				return value as Long;
			
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
			
			return new Long(value);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.

	    /**
	     * The sign of this <code>Long</code> as an <code>int</code>, ie:
		 * -1, 0 or 1 as the value of this <code>Long</code> is negative,
		 * zero or positive.
	     */
		[Transient]
		public function get sign():int {
			if (_u0 == 0 && _u1 == 0)
				return 0;
			return ((_u1 & 0x80000000) != 0 ? -1 : 1);
		}

	    /**
	     * The low 32bits of this <code>Long</code> as an <code>uint</code>.
	     */
		[Transient]
		public function get lowBits():uint {
			return _u0;
		}

	    /**
	     * The high 32bits of this <code>Long</code> as an <code>uint</code>.
	     */
		[Transient]
		public function get highBits():uint {
			return _u1;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Bitwise and bit shift operators.
		
		/**
		 * Returns <code>(~this)</code> (bitwise NOT).
		 * 
		 * @return <code>(~this)</code>.
		 */
		public function not():Long {
			return newLong(~_u1, ~_u0);
		}
		
		/**
		 * Returns <code>(this &amp; b)</code> (bitwise AND).
		 * 
		 * @param b the other operand used in the operation (may be of any type
		 * 		accepted by the <code>Long</code> constructor).
		 * @return <code>(this &amp; b)</code>.
		 */
		public function and(b:*):Long {
			var l:Long = asLong(b);
			return newLong((_u1 & l._u1), (_u0 & l._u0));
		}
		
		/**
		 * Returns <code>(this | b)</code> (bitwise OR).
		 * 
		 * @param b the other operand used in the operation (may be of any type
		 * 		accepted by the <code>Long</code> constructor).
		 * @return <code>(this | b)</code>.
		 */
		public function or(b:*):Long {
			var l:Long = asLong(b);
			return newLong((_u1 | l._u1), (_u0 | l._u0));
		}
		
		/**
		 * Returns <code>(this ^ b)</code> (bitwise XOR).
		 * 
		 * @param b the other operand used in the operation (may be of any type
		 * 		accepted by the <code>Long</code> constructor).
		 * @return <code>(this ^ b)</code>.
		 */
		public function xor(b:*):Long {
			var l:Long = asLong(b);
			return newLong((_u1 ^ l._u1), (_u0 ^ l._u0));
		}
		
		/**
		 * Returns <code>(this &lt;&lt; b)</code> (bitwise left shift).
		 * 
		 * @value the number of bits to be left shifted.
		 * @return <code>(this &lt;&lt; b)</code>.
		 */
		public function leftShift(value:uint):Long {
			value %= 64;
			
			if (value == 0)
				return this;
			
			if (value >= 32)
				return newLong(_u0 << (value % 32), 0);

			// value < 32.
			return newLong(
				(_u1 << value) | (_u0 >>> (32 - value)),
				(_u0 << value)
			);
		}
		
		/**
		 * Returns <code>(this &gt;&gt; b)</code> (bitwise right shift).
		 * 
		 * @value the number of bits to be left shifted.
		 * @unsigned if <code>true</code>, the operation will behave as a
		 * 		bitwise <i>unsigned</i> right shift operator (&gt;&gt;&gt;).
		 * @return <code>(this &gt;&gt; b)</code>.
		 */
		public function rightShift(value:uint, unsigned:Boolean = false):Long {
			value %= 64;
			
			if (value == 0)
				return this;
			
			if (value >= 32) {
				
				if (unsigned)
					return newLong(0, _u1 >>> (value % 32));

				return newLong((
					(_u1 & 0x80000000) != 0 ? uint.MAX_VALUE : 0),
					(_u1 >> (value % 32))
				);
			}

			// value < 32.
			if (unsigned) {
				return newLong(
					(_u1 >>> value),
					(_u0 >>> value) | (_u1 << (32 - value))
				);
			}
				
			return newLong(
				(_u1 >> value),
				(_u0 >> value) | (_u1 << (32 - value))
			);
		}
		
	    /**
	     * Returns <code>true</code> if and only if the designated bit is set:
	     * computes <code>((this &amp; (1 &lt;&lt; n)) != 0)</code>.
	     *
	     * @param index the index of bit to test.
	     * @return <code>true</code> if and only if the designated bit is set.
	     * @throws org.granite.math.ArithmeticError if <code>index</code> is negative
		 * 		or greater than 63.
	     */
		public function testBit(index:int):Boolean {
			if (index < 0 || index > 63)
				throw new IllegalArgumentError("Index out of range: " + index);
			if (index <= 31)
				return ((_u0 & (uint(1) << index)) != 0);
			return ((_u1 & (uint(1) << (index % 32))) != 0);
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is equivalent to this <code>Long</code>
	     * with the designated bit set: computes <code>(this | (1 &lt;&lt; n))</code>.
	     *
	     * @param index the index of bit to set.
	     * @return <code>this | (1 &lt;&lt; n)</code>
	     * @throws org.granite.math.ArithmeticError if <code>index</code> is negative
		 * 		or greater than 63.
	     */
		public function setBit(index:int):Long {
			if (testBit(index))
				return this;
			
			var u0:uint = _u0, u1:uint = _u1;
			if (index <= 31)
				u0 |= (uint(1) << index);
			else
				u1 |= (uint(1) << (index % 32));
			return newLong(u1, u0);
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is equivalent to this <code>Long</code>
	     * with the designated bit cleared: computes <code>(this &amp; ~(1 &lt;&lt; n))</code>.
	     *
	     * @param index the index of bit to clear.
	     * @return <code>this &amp; ~(1 &lt;&lt; n)</code>
	     * @throws org.granite.math.ArithmeticError if <code>index</code> is negative
		 * 		or greater than 63.
	     */
		public function clearBit(index:int):Long {
			if (!testBit(index))
				return this;
			
			var u0:uint = _u0, u1:uint = _u1;
			if (index <= 31)
				u0 &= ~(uint(1) << index);
			else
				u1 &= ~(uint(1) << (index % 32));
			return newLong(u1, u0);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Unary operations.
		
	    /**
	     * Returns a <code>Long</code> whose value is the absolute value
		 * of this <code>Long</code>.
	     *
	     * @return the absolute value of this <code>Long</code>.
	     */
		public function abs():Long {
			if (sign >= 0)
				return this;
			return negate();
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(-this)</code>.
	     *
	     * @return <code>(-this)</code>.
	     */
		public function negate():Long {
			var u0:uint = ((~_u0) + 1),
				u1:uint = (~_u1);
			
			// overflown...
			if (_u0 == 0)
				u1++;
			
			return newLong(u1, u0);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Binary operations.
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(this + b)</code>,
		 * with a possible overflow (silently ignored, as in primitive operations).
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value to be added to this <code>Long</code>.
	     * @return <code>(this + b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function add(b:*):Long {
			var l:Long = asLong(b),
				s0:uint = _u0 + l._u0,
				s1:uint = _u1 + l._u1;

			// overflown...
			if (s0 < _u0 || s0 < l._u0)
				s1++;

			return newLong(s1, s0);
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(this - b)</code>,
		 * with a possible overflow (silently ignored, as in primitive operations).
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value to be subtracted from this <code>Long</code>.
	     * @return <code>(this - b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function subtract(b:*):Long {
			var l:Long = asLong(b),
				s0:uint = _u0 - l._u0,
				s1:uint = _u1 - l._u1;

			// overflown...
			if (_u0 < l._u0)
				s1--;

			return newLong(s1, s0);
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(this ~~ b)</code>,
		 * with a possible overflow (silently ignored, as in primitive operations).
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value to be multiplied by this <code>Long</code>.
	     * @return <code>(this ~~ b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function multiply(b:*):Long {
			var l:Long = asLong(b),
				p:Array = [0, 0],
				p0:uint = 0,
				p1:uint = 0;
			
			if (_u0 == 1)
				p0 = l._u0;
			else if (_u0 != 0) {
				if (l._u0 == 1)
					p0 = _u0;
				else if (l._u0 != 0) {
					MutableBigInteger.multiplyUints(_u0, l._u0, p);
					p0 = p[0];
					p1 = p[1];
				}
			}
			
			// forget about ((_u1 * l._u1) << 32): no room for the result.
			p1 += (_u0 * l._u1) + (l._u0 * _u1); 

			return newLong(p1, p0);
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(this / b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value by which this <code>Long</code> is to be divided.
	     * @return <code>(this / b)</code>.
		 * @throws org.granite.math.ArithmeticError if the <code>b</code>
		 * 		parameter is equals to <code>0</code>. 
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function divide(b:*):Long {
			return divideAndRemainder(b)[0];
		}
		
	    /**
	     * Returns a <code>Long</code> whose value is <code>(this % b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value by which this <code>Long</code> is to be divided,
		 * 		and the remainder computed.
	     * @return <code>(this % b)</code>.
		 * @throws org.granite.math.ArithmeticError if the <code>b</code>
		 * 		parameter is equals to <code>0</code>. 
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function remainder(b:*):Long {
			return divideAndRemainder(b)[1];
		}
		
	    /**
	     * Returns an array of two <code>Long</code> containing
		 * <code>(this / b)</code> followed by <code>(this % b)</code>. 
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value by which this <code>Long</code> is to be divided,
		 * 		and the remainder computed.
	     * @return an array of two Long: the quotient <code>(this / val)</code>
		 * 		is the initial element, and the remainder <code>(this % val)</code>
		 * 		is the final element.
		 * @throws org.granite.math.ArithmeticError if the <code>b</code>
		 * 		parameter is equals to <code>0</code>. 
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
	     */
		public function divideAndRemainder(b:*):Array {
			var l:Long = asLong(b),
				lsign:int = l.sign,
				sign:int = this.sign;

			// b == 0 --> error.
			if (lsign == 0)
				throw new ArithmeticError("Cannot divide by zero");

			// this == 0 --> [q = 0, r = 0].
			if (sign == 0)
				return [ZERO, ZERO];
			
			// b == 1 || b == -1
			if ((l._u1 == 0 && l._u0 == 1) || (l._u1 == uint.MAX_VALUE && l._u0 == uint.MAX_VALUE)) {
				// b == 1 --> [q = this, r = 0].
				if (lsign == 1)
					return [this, ZERO];
				// b == -1 --> [q = -this, r = 0].
				return [negate(), ZERO];
			}
			
			// this == Long.MIN_VALUE. 
			if (_u0 == 0 && _u1 == uint(0x80000000)) {
				
			}
			
			// compare the absolute values of this and b.
			var left:Long = (sign < 0 ? this.negate() : this),
				right:Long = (lsign < 0 ? l.negate() : l),
				comp:int = left.compareTo(right);

			// |this| < |b| && this != Long.MIN_VALUE --> 0.
			if (comp < 0 && !(_u0 == 0 && _u1 == uint(0x80000000)))
				return [ZERO, this];

			// |this| == |b| --> +/-1.
			if (comp == 0)
				return [(sign == lsign ? ONE : ONE.negate()), ZERO];
			
			// |this| > |b|, calculate [q, r].
			var q:Long,
				r:Long,
				ma:MutableBigInteger,
				mb:MutableBigInteger,
				mr:MutableBigInteger;
			
			// left._u1 == 0 ==> right._u1 == 0 && left._u0 > right._u0.
			if (left._u1 == 0) {
				q = newLong(0, left._u0 / right._u0);
				r = newLong(0, left._u0 % right._u0);
			}
			// left._u1 > 0 && right._u1 == 0.
			else if (right._u1 == 0) {
				ma = new MutableBigInteger(new Uints([left._u0, left._u1]));
				r = newLong(0, ma.divideByUint(right._u0));
				q = newLong(ma.uints[1], ma.uints[0]);
			}
			// left._u1 > 0 && right._u1 > 0.
			else {
				ma = new MutableBigInteger(new Uints([left._u0, left._u1])),
				mb = new MutableBigInteger(new Uints([right._u0, right._u1])),
				mr = ma.divide(mb);
	
				q = newLong(ma.uints[1], ma.uints[0]);
				r = newLong(mr.uints[1], mr.uints[0]);
			}
			
			// remainder must have the same sign as the dividend.
			if (sign == -1)
				r = r.negate();
			
			return (sign == lsign ? [q, r] : [q.negate(), r]);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Comparisons.
		
	    /**
	     * Compares this <code>Long</code> with the specified
		 * <code>Long</code>. This method is provided in preference to
		 * individual methods for each of the six boolean comparison operators
		 * (&lt;, ==, &gt;, &lt;=, !=, &gt;=).  The suggested idiom for performing
		 * these comparisons is: <code>(x.compareTo(y) &lt;<i>op</i>&gt; 0)</code>,
		 * where &lt;<i>op</i>&gt; is one of the six comparison operators.
	     *
	     * @param b the <code>Long</code> to which this <code>Long</code>
		 * 		is to be compared.
	     * @return -1, 0 or 1 as this <code>Long</code> is numerically less than,
		 * 		equal to, or greater than <code>b</code>.
	     */
		public function compareTo(b:Long):int {
			if (this === b)
				return 0;
			
			const sign:int = this.sign,
				  bSign:int = b.sign;
			
			// this == 0:
			// | b == 0 --> -0 == 0;
			// | b <  0 --> -(-1) == 1;
			// | b >  0 --> -1.
			if (sign == 0)
				return -bSign;
			
			// sign (!= 0) == bSign.
			if (sign == bSign) {
				// this == b --> 0.
				if (_u1 == b._u1 && _u0 == b._u0)
					return 0;
				// works with positive and negative numbers.
				return (_u1 > b._u1 || _u0 > b._u0 ? 1 : -1);
			}
			
			// sign (!= 0) != bSign:
			// | sign == -1 --> bSign == 0|1 --> -1;
			// | sign == 1 --> bSign == -1|0 --> 1.
			return sign < bSign ? -1 : 1;
		}

	    /**
	     * Compares this <code>Long</code> with the specified object
		 * for equality.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>Long</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b an object to which this <code>Long</code> is to
		 * 		be compared.
	     * @return <code>true</code> if and only if the specified object is
		 * 		a <code>Long</code> (or convertible to a
		 * 		<code>Long</code>) whose value is numerically equal
		 * 		to this <code>Long</code>.
	     */
		public function equals(b:*):Boolean {
			try {
				return (compareTo(asLong(b)) == 0);
			}
			catch (e:BigNumberError) {
			}
			return false;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conversions.
		
	    /**
	     * Converts this <code>Long</code> to an <code>int</code>: it is equivalent
		 * to <code>int(this.lowBits)</code> with a possible lose of precision and
		 * a different sign than the original value.
	     *
	     * @return this <code>Long</code> converted to an <code>int</code>.
	     */
		public function toInt():int {
			return int(_u0);
		}
		
	    /**
	     * Converts this <code>Long</code> to a <code>Number</code> (with a possible
		 * lose of precision).
	     *
	     * @return this <code>Long</code> converted to a <code>Number</code>.
	     */
		public function toNumber():Number {
			var sign:int = this.sign;
			
			if (sign == 0)
				return 0;
			if (sign == 1)
				return ((Number(_u1) * Radix.BASE_UINT) + Number(_u0));
			
			var abs:Long = this.negate();
			
			return Number(-1) * ((Number(abs._u1) * Radix.BASE_UINT) + Number(abs._u0));
		}
		
		/**
		 * Returns a string representation of this <code>Long</code> as signed integer
		 * in the specified radix.
		 * 
		 * @param radix the radix to be used (2 &lt;= radix &lt;= 36).
		 * @return a string representation of this <code>Long</code> as signed integer
		 * 		in the specified radix.
		 * @throws org.granite.math.IllegalArgumentError if the specified radix is out
		 * 		of supported range.
		 */
		public function toString(radix:int = 10):String {

			var radixConstraints:Radix = Radix.getRadix(radix),
				sign:int = this.sign,
				a:Long = (sign < 0 ? this.negate() : this);

			if (sign == 0)
				return "0";
			
			if (a._u1 == 0) {
				if (sign == -1)
					return "-" + a._u0.toString(radix);
				return a._u0.toString(radix);
			}
			
			var ma:MutableBigInteger = new MutableBigInteger(new Uints([a._u0, a._u1]));
			if (sign == -1)
				return "-" + ma.toString(radix);
			return ma.toString(radix);
		}
		
		/**
		 * Returns a string representation of this <code>Long</code> as an unsigned integer
		 * in base 16. 
		 * 
		 * @return a string representation of this <code>Long</code> as an unsigned integer
		 * 		in base 16.
		 */
		public function toHexString():String {
			if (_u1 == 0)
				return _u0.toString(16);
			
			var s0:String = _u0.toString(16);
			
			if (s0.length == 8)
				return _u1.toString(16) + s0;
			
			return _u1.toString(16) + Radix.getZeros(8 - s0.length) + s0;
		}
		
		///////////////////////////////////////////////////////////////////////
		// IExternalizable implementation.
		
		/**
		 * @private
		 */
		public function readExternal(input:IDataInput):void {
			_u1 = uint(input.readObject());
			_u0 = uint(input.readObject());
		}
		
		/**
		 * @private
		 */
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_u1);
			output.writeObject(_u0);
		}
	}
}