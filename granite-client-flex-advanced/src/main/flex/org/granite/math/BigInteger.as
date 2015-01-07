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

	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;

	[RemoteClass(alias="java.math.BigInteger")]
    /**
	 * Immutable arbitrary-precision integers.  All operations behave as if
	 * BigIntegers were represented in two's-complement notation (like ActionScript3's
	 * primitive <code>int</code> type). <code>BigInteger</code> provides analogues
	 * to all of ActionScript3's primitive integer operators (+, -, ~~, /).
	 * <br>
	 * <br>
	 * Semantics of arithmetic operations exactly mimic those of ActionScript3's integer
	 * arithmetic operators. For example, division by zero throws a
	 * <code>BigNumberError</code>, and division of a negative by a positive yields a
	 * negative (or zero) remainder. All of the details concerning overflow are ignored, as
	 * <code>BigInteger</code>s are made as large as necessary to accommodate the results
	 * of an operation.
	 * <br>
	 * <br>
	 * Comparison operations perform signed integer comparisons, analogous to
 	 * those performed by ActionScript3's relational and equality operators.
	 * <br>
	 * <br>
	 * <i>This class is a partial ActionScript3 port of the Java
	 * class <code>java.math.BigInteger</code> originally written by
	 * Josh Bloch and Michael McCloskey, but relies on a completely different
	 * implementation. It provides externalization methods that are
	 * meant to be used with specific GraniteDS serialization mechanisms.</i>
	 * 
     * @author Franck WOLFF
	 * 
	 * @see Long
	 * @see BigDecimal
     */
	public final class BigInteger implements BigNumber {
		
		///////////////////////////////////////////////////////////////////////
		// Constants.

		private static const _MZERO:MutableBigInteger = MutableBigInteger.ZERO;
		private static const _MONE:MutableBigInteger = MutableBigInteger.ONE;
		private static const _MTEN:MutableBigInteger = MutableBigInteger.TEN;
		
		/**
		 * The <code>BigInteger</code> constant zero.
		 */
		public static const ZERO:BigInteger = newBigInteger(0, _MZERO, false);
		
		/**
		 * The <code>BigInteger</code> constant one.
		 */
		public static const ONE:BigInteger = newBigInteger(1, MutableBigInteger.ONE, false);
		
		/**
		 * The <code>BigInteger</code> constant ten.
		 */
		public static const TEN:BigInteger = newBigInteger(1, MutableBigInteger.TEN, false);
		
		///////////////////////////////////////////////////////////////////////
		// Fields.

		private var _sign:int;
		private var _mbi:MutableBigInteger;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.

		/**
		 * Constructs a new <code>BigInteger</code> instance according to the
		 * supplied parameters.
		 * <br>
		 * <br>
		 * The <code>value</code> parameter may be a String representation of
		 * an integer, a <code>int</code> primitive value, a <code>Number</code>
		 * or even another <code>BigInteger</code>:
		 * <br>
		 * <ul>
		 * <li><code>String</code>: it must be a not empty String in the form of
		 * 		<i>-?[0-9a-zA-Z]+</i> (ie: an optional '-' character followed by
		 * 		a not empty sequence of digits (with radix &gt;= 2 and &lt;= 36)). When
		 * 		a String value is used, the second parameter <code>radix</code>
		 * 		is also used, and the supplied digits must be in accordance with
		 * 		the specified radix.</li>
		 * <li><code>int</code>: a primitive integer value (radix ignored).</li>
		 * <li><code>Number</code>: a primitive number value (radix ignored). Only
		 * 		the fixed part of the number will be used (decimal part is
		 *      ignored).</li>
		 * <li><code>Long</code>: the new BigInteger will be equals to
		 * 		the specified parameter.</li>
		 * <li><code>BigInteger</code>: the new BigInteger will be an exact copy of
		 * 		the specified parameter.</li>
		 * <li><code>null</code>: the new BigInteger will be an exact copy of
		 * 		the constant <code>BigInteger.ZERO</code>.</li>
		 * </ul>
		 * 
		 * @param value the value to be assigned to the new <code>BigInteger</code>.
		 * @param radix the radix (2 &lt;= radix &lt;= 36) to be used for string conversion
		 * 		(ignored if the <code>value</code> parameter isn't a string).
		 * @throws org.granite.math.NumberFormatError if the <code>value</code>
		 * 		parameter is an invalid String representation. 
		 * @throws org.granite.math.IllegalArgumentError if the <code>value</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 */
		function BigInteger(value:* = null, radix:int = 10) {
			if (value == null) {
				_sign = 0;
				_mbi = _MZERO;
			}
			else if (value is String)
				forString(this, value as String, radix);
			else if (value is int)
				forInt(this, value as int);
			else if (value is Number)
				forNumber(this, value as Number);
			else if (value is Long) {
				_sign = (value as Long).sign;
				var abs:Long = (value as Long).abs();
				if (abs.highBits == 0) {
					if (abs.lowBits == 0)
						_mbi = MutableBigInteger.ZERO;
					else
						_mbi = new MutableBigInteger(new Uints([abs.lowBits]));
				}
				else
					_mbi = new MutableBigInteger(new Uints([abs.lowBits, abs.highBits]));
			}
			else if (value is BigInteger) {
				_sign = (value as BigInteger)._sign;
				_mbi = (value as BigInteger)._mbi;
			}
			else
				throw new IllegalArgumentError("Cannot construct a BigInteger from: " + value);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static initializers.
		
		private static function forString(a:BigInteger, value:String, radix:int = 10):void {
			if (value == null || value.length == 0)
				throw new NumberFormatError("Zero length BigInteger");

			var first:String = value.charAt(0);
			if (first == '-')
				value = value.substr(1);
			
			a._mbi = MutableBigInteger.forString(value, radix);
			a._sign = (a._mbi.isZero() ? 0 : (first == '-' ? -1 : 1)); 
		} 
		
		private static function forInt(a:BigInteger, value:int):void {
			switch (value) {
				case 0:
					a._sign = 0;
					a._mbi = _MZERO;
					break;
				case 1:
					a._sign = 1;
					a._mbi = _MONE;
					break;
				case 10:
					a._sign = 1;
					a._mbi = _MTEN;
					break;
				default:
					if (value > 0)
						a._sign = 1;
					else {
						a._sign = -1;
						value = -value;
					}
					a._mbi = new MutableBigInteger(new Uints([uint(value)]));
					break;
			}
		} 
		
		private static function forNumber(a:BigInteger, value:Number):void {
			if (isNaN(value))
				throw new IllegalArgumentError("Illegal NaN parameter");
			if (!isFinite(value))
				throw new IllegalArgumentError("Illegal infinite parameter");
			
			switch (value) {
				case 0:
					a._sign = 0;
					a._mbi = _MZERO;
					break;
				case 1:
					a._sign = 1;
					a._mbi = _MONE;
					break;
				case 10:
					a._sign = 1;
					a._mbi = _MTEN;
					break;
				default:
					if (value > 0)
						a._sign = 1;
					else {
						a._sign = -1;
						value = -value;
					}
					a._mbi = MutableBigInteger.forNumber(value);
					break;
			}
		}
		
		/**
		 * @private
		 */
		internal static function asBigInteger(value:*):BigInteger {
			if (value is BigInteger)
				return value as BigInteger;
			
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
			else if (value is Long) {
				switch (value as Long) {
					case Long.ZERO: return ZERO;
					case Long.ONE: return ONE;
					case Long.TEN: return TEN;
				} 
			}
			
			return new BigInteger(value);
		}
		
		private static function newBigInteger(sign:int,
											  mbi:MutableBigInteger,
											  constants:Boolean = true):BigInteger {

			if (constants) {
				if (sign == 0 || mbi == null || mbi.isZero())
					return ZERO;
				if (sign == 1) {
					if (mbi.isOne())
						return ONE;
					if (mbi.isTen())
						return TEN;
				}
			}
			
			var a:BigInteger = new BigInteger();
			if (sign != 0 && mbi != null && !mbi.isZero()) {
				if (sign < -1)
					sign = -1;
				else if (sign > 1)
					sign = 1;
				a._sign = sign;
				a._mbi = mbi;
			}
			return a;
		}
		
		/**
		 * @private
		 */
		internal static function bigPowerOfTen(pow:int):BigInteger {
			if (pow < 0)
				return ZERO;
			if (pow == 0)
				return ONE;
			if (pow == 1)
				return TEN;
			
			var a:MutableBigInteger = MutableBigInteger.forUint(100);
			for ( ; pow > 2; pow--)
				a.multiplyByUint(10);
			
			return newBigInteger(1, a, false);
		}
		
		/**
		 * @private
		 */
		internal function multiplyByBigPowerOfTen(pow:int):BigInteger {
			if (pow < 0 || _sign == 0)
				return ZERO;
			if (pow == 0)
				return this;
			if (pow == 1)
				return multiply(10);
			
			var a:MutableBigInteger = MutableBigInteger.forUint(100);
			for ( ; pow > 2; pow--)
				a.multiplyByUint(10);
			a.multiply(_mbi);
			
			return newBigInteger(_sign, a, false);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * @private
		 */
		internal function get mbi():MutableBigInteger {
			return _mbi;
		}
		
		/**
		 * @private
		 */
		internal function set mbi(value:MutableBigInteger):void {
			_mbi = value;
		}
		
	    /**
	     * The sign of this <code>BigInteger</code> as an <code>int</code>, ie:
		 * -1, 0 or 1 as the value of this <code>BigInteger</code> is negative,
		 * zero or positive.
	     */
		[Transient]
		public function get sign():int {
			return _sign;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Unary operations.
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is the absolute value
		 * of this <code>BigInteger</code>.
	     *
	     * @return the absolute value of this <code>BigInteger</code>.
	     */
		public function abs():BigInteger {
			if (_sign >= 0)
				return this;
			return newBigInteger(1, _mbi);
		}
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(-this)</code>.
	     *
	     * @return <code>(-this)</code>.
	     */
		public function negate():BigInteger {
			if (_sign == 0)
				return this;
			return newBigInteger(-_sign, _mbi);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Binary operations.
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(this + b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value to be added to this <code>BigInteger</code>.
	     * @return <code>(this + b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function add(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);

			if (_sign == 0)
				return bi;
			if (bi._sign == 0)
				return this;
			
			var mbi:MutableBigInteger = null;
			
			if (_sign == bi._sign) {
				mbi = _mbi.clone();
				mbi.add(bi._mbi);
				return newBigInteger(_sign, mbi);
			}
			
			var comp:int = _mbi.compareTo(bi._mbi);
			
			if (comp == 0)
				return ZERO;
			
			if (comp > 0) {
				mbi = _mbi.clone();
				mbi.subtract(bi._mbi);
			}
			else {
				mbi = bi._mbi.clone();
				mbi.subtract(_mbi);
			}
			
			return newBigInteger((comp == _sign ? 1 : -1), mbi);
		}
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(this - b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value to be subtracted from this <code>BigInteger</code>.
	     * @return <code>(this - b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function subtract(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);

			if (_sign == 0)
				return bi.negate();
			if (bi._sign == 0)
				return this;
			
			var mbi:MutableBigInteger = null;
			
			if (_sign != bi._sign) {
				mbi = _mbi.clone();
				mbi.add(bi._mbi);
				return newBigInteger(_sign, mbi);
			}
			
			var comp:int = _mbi.compareTo(bi._mbi);
			
			if (comp == 0)
				return ZERO;
			
			if (comp > 0) {
				mbi = _mbi.clone();
				mbi.subtract(bi._mbi);
			}
			else {
				mbi = bi._mbi.clone();
				mbi.subtract(_mbi);
			}
			
			return newBigInteger((comp == _sign ? 1 : -1), mbi);
		}
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(this ~~ b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value to be multiplied by this <code>BigInteger</code>.
	     * @return <code>(this ~~ b)</code>.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function multiply(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);

			if (_sign == 0 || bi._sign == 0)
				return ZERO;
			
			if (_mbi.isOne()) {
				if (_sign == 1)
					return bi;
				return bi.negate();
			}
			else if (bi._mbi.isOne()) {
				if (bi._sign == 1)
					return this;
				return negate();
			}
			
			var mbi:MutableBigInteger = _mbi.clone();
			mbi.multiply(bi._mbi);
			return newBigInteger((_sign == bi._sign ? 1 : -1), mbi);
		}
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(this / b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value by which this <code>BigInteger</code> is to be divided.
	     * @return <code>(this / b)</code>.
		 * @throws org.granite.math.ArithmeticError if the <code>b</code>
		 * 		parameter is equals to <code>0</code>. 
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function divide(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);

			if (bi._sign == 0)
				throw new ArithmeticError("Cannot divide by zero");

			if (_sign == 0)
				return ZERO;
			
			if (bi._mbi.isOne()) {
				if (bi._sign == 1)
					return this;
				return negate();
			}
			
			var mbi:MutableBigInteger = _mbi.clone();
			mbi.divide(bi._mbi);
			return newBigInteger((_sign == bi._sign ? 1 : -1), mbi);
		}
		
	    /**
	     * Returns a <code>BigInteger</code> whose value is <code>(this % b)</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value by which this <code>BigInteger</code> is to be divided,
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
		 * 
		 * @see #BigInteger()
	     */
		public function remainder(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);

			if (bi._sign == 0)
				throw new ArithmeticError("Cannot divide by zero");

			if (_sign == 0)
				return ZERO;
			
			if (bi._mbi.isOne())
				return ZERO;
			
			var mbi:MutableBigInteger = _mbi.clone();
			var rem:MutableBigInteger = mbi.divide(bi._mbi);
			return newBigInteger(_sign, rem);
		}
				
	    /**
	     * Returns an array of two <code>BigInteger</code> containing
		 * <code>(this / b)</code> followed by <code>(this % b)</code>. 
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param  b the value by which this <code>BigInteger</code> is to be divided,
		 * 		and the remainder computed.
	     * @return an array of two BigIntegers: the quotient <code>(this / val)</code>
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
		 * 
		 * @see #BigInteger()
	     */
		public function divideAndRemainder(b:*):Array {
			var bi:BigInteger = asBigInteger(b);

			if (bi._sign == 0)
				throw new ArithmeticError("Cannot divide by zero");

			if (_sign == 0)
				return [ZERO, ZERO];
			
			if (bi._mbi.isOne()) {
				if (bi._sign == 1)
					return [this, ZERO];
				return [negate(), ZERO];
			}
			
			var mbi:MutableBigInteger = _mbi.clone();
			var rem:MutableBigInteger = mbi.divide(bi._mbi);
			return [
				newBigInteger((_sign == bi._sign ? 1 : -1), mbi),
				newBigInteger(rem.isZero() ? 0 : _sign, rem)
			];
		}
		
		///////////////////////////////////////////////////////////////////////
		// Single bit operations.
		
		/**
		 * @private
		 */
		internal function testBit(index:int):Boolean {
			if (index < 0)
				throw new IllegalArgumentError("Invalid negative index: " + index);
			
			var idiv32:int = (index / 32),
				imod32:int = (index % 32),
				u:uint = _mbi.getUintAt(idiv32);
			
			return (u & (1 << imod32)) != 0;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Comparisons.
		
	    /**
	     * Compares this <code>BigInteger</code> with the specified
		 * <code>BigInteger</code>.  This method is provided in preference to
		 * individual methods for each of the six boolean comparison operators
		 * (&lt;, ==, &gt;, &lt;=, !=, &gt;=).  The suggested idiom for performing
		 * these comparisons is: <code>(x.compareTo(y) &lt;<i>op</i>&gt; 0)</code>,
		 * where &lt;<i>op</i>&gt; is one of the six comparison operators.
	     *
	     * @param b the <code>BigInteger</code> to which this <code>BigInteger</code>
		 * 		is to be compared.
	     * @param unsigned if <code>true</code>, the comparison is made between absolute
		 * 		values (ie: <code>this.abs().compareTo(b.abs())</code>).
	     * @return -1, 0 or 1 as this <code>BigInteger</code> is numerically less than,
		 * 		equal to, or greater than <code>b</code>.
	     */
		public function compareTo(b:BigInteger, unsigned:Boolean = false):int {
			if (this === b)
				return 0;
			
			if (unsigned)
				return _mbi.compareTo(b._mbi);
			
			if (_sign == b._sign) {
				if (_sign == 0)
					return 0;
				return _mbi.compareTo(b._mbi);
			}
			
			return _sign > b._sign ? 1 : -1;
		}

	    /**
	     * Compares this <code>BigInteger</code> with the specified object
		 * for equality.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b an object to which this <code>BigInteger</code> is to
		 * 		be compared.
	     * @return <code>true</code> if and only if the specified object is
		 * 		a <code>BigInteger</code> (or convertible to a
		 * 		<code>BigInteger</code>) whose value is numerically equal
		 * 		to this <code>BigInteger</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function equals(b:*):Boolean {
			try {
				return (compareTo(asBigInteger(b)) == 0);
			}
			catch (e:IllegalArgumentError) {
			}
			return false;
		}
		
	    /**
	     * Returns the minimum of this <code>BigInteger</code> and
		 * <code>b</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value with which the minimum is to be computed.
	     * @return the <code>BigInteger</code> whose value is the lesser of
		 * 		this <code>BigInteger</code> and <code>b</code>. If they are
		 * 		equal, either may be returned.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function min(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);
			return (compareTo(bi) < 0 ? this : bi);
		}
		
	    /**
	     * Returns the maximum of this <code>BigInteger</code> and
		 * <code>b</code>.
		 * <br>
		 * <br>
		 * The <code>b</code> parameter may be of any of the supported types as
		 * specified in the <code>BigInteger</code> constructor documentation
		 * (a radix of 10 is assumed for String representations).
	     *
	     * @param b the value with which the maximum is to be computed.
	     * @return the <code>BigInteger</code> whose value is the greater of
		 * 		this <code>BigInteger</code> and <code>b</code>. If they are
		 * 		equal, either may be returned.
		 * @throws org.granite.math.NumberFormatError if the <code>b</code>
		 * 		parameter is an invalid String representation (for radix 10). 
		 * @throws org.granite.math.IllegalArgumentError if the <code>b</code>
		 * 		parameter is not one of the supported types or if it is
		 * 		<code>Number.NaN</code>, <code>Number.POSITIVE_INFINITY</code> or
		 * 		<code>Number.NEGATIVE_INFINITY</code>.
		 * 
		 * @see #BigInteger()
	     */
		public function max(b:*):BigInteger {
			var bi:BigInteger = asBigInteger(b);
			return (compareTo(bi) > 0 ? this : bi);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conversions.
		
	    /**
	     * Converts this <code>BigInteger</code> to an <code>int</code>: if this
		 * <code>BigInteger</code> is too big to fit in an <code>int</code>,
		 * only the low-order 31 bits are returned and the sign of the result is
		 * preserved as it was in the orginal <code>BigInteger</code>.
		 * <br>
		 * <br>
		 * Note this conversion is slightly different than the Java one: it
		 * always preserves the sign and conforms to following rules:
		 * <ul>
		 * <li>If <code>this &lt; int.MIN_VALUE</code>, then <code>int.MIN_VALUE</code>
		 * 		is returned.</li>
		 * <li>If <code>int.MIN_VALUE &lt;= this &lt;= int.MAX_VALUE</code>,
		 * 		then <i><code>int(this)</code></i> is returned (exact result).</li>
		 * <li>If <code>this &gt; int.MAX_VALUE</code>, then <code>int.MAX_VALUE</code>
		 * 		is returned.</li>
		 * </ul>
	     *
	     * @return this <code>BigInteger</code> converted to an <code>int</code>.
	     */
		public function toInt():int {
			if (_sign == 0)
				return 0;
			
			const u0:uint = _mbi.toUint();
			
			if (_sign == -1 && _mbi.length == 1 && u0 == 0x80000000)
				return int.MIN_VALUE;
			
			return _sign * int(u0 & 0x7fffffff);
		}
		
	    /**
	     * Converts this <code>BigInteger</code> to a <code>Number</code>. This
	     * conversion is done by converting this <code>BigInteger</code> to signed
		 * String representation and constructing a new <code>Number</code> from
		 * this representation: if this <code>BigInteger</code> has atoo great
		 * magnitude to be represented as a <code>Number</code>, it will be converted to
	     * <code>Number.NEGATIVE_INFINITY</code> or <code>Number.POSITIVE_INFINITY</code>
		 * as appropriate.  Note that even when the return value is finite, this
		 * conversion can lose information about the precision of the
		 * <code>BigInteger</code> value.
	     *
	     * @return this <code>BigInteger</code> converted to a <code>Number</code>.
	     */
		public function toNumber():Number {
			if (_sign == 0)
				return Number(0);
			if (_sign == 1)
				return _mbi.toNumber();
			return Number("-" + _mbi.toString());
		}
		
	    /**
	     * Returns the String representation of this <code>BigInteger</code> in the
	     * given radix. If the radix is outside the range from 2 to 36 inclusive,
	     * an error is thrown.  The digit-to-character mapping uses "0...9, a...z"
		 * characters, and a minus sign is prepended if appropriate.
	     *
	     * @param radix the radix of the String representation.
	     * @param abs if <code>true</code>, the minus sign is skipped even if this
		 * 		<code>BigInteger</code> is negative.
	     * @return String representation of this <code>BigInteger</code> in the given
		 * 		radix.
		 * 
	     * @see #BigInteger()
	     */
		public function toString(radix:int = 10, abs:Boolean = false):String {
			if (_sign == 0 || _mbi.isZero())
				return "0";
			if (_sign == -1 && !abs)
				return "-" + _mbi.toString(radix);
			return _mbi.toString(radix);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		internal function clone():BigInteger {
			var clone:BigInteger = new BigInteger();
			clone._mbi = _mbi.clone();
			clone._sign = _sign;
			return clone;
		}
		
		///////////////////////////////////////////////////////////////////////
		// IExternalizable implementation.
		
		/**
		 * @private
		 */
		public function readExternal(input:IDataInput):void {
			forString(this, String(input.readObject()), Radix.MAX);
		}
		
		/**
		 * @private
		 */
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(toString(Radix.MAX));
		}
	}
}
