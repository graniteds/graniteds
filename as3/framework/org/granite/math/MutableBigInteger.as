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

	/** 
	 * @author Franck WOLFF
	 * @private
	 */
	public class MutableBigInteger {
		
		///////////////////////////////////////////////////////////////////////
		// Fields.
		
		private var _uints:Uints = null;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		function MutableBigInteger(uints:Uints = null) {
			_uints = (uints != null ? uints : new Uints());
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static constructors.
		
		public static function get ZERO():MutableBigInteger {
			return new MutableBigInteger(new Uints());
		}
		
		public static function get ONE():MutableBigInteger {
			return new MutableBigInteger(new Uints([1]));
		}
		
		public static function get TEN():MutableBigInteger {
			return new MutableBigInteger(new Uints([10]));
		}
		
		public static function forUint(value:uint):MutableBigInteger {
			if (value == 0)
				return ZERO;
			return new MutableBigInteger(new Uints([value]));
		}
		
		public static function forNumber(value:Number):MutableBigInteger {
			if (isNaN(value))
				throw new IllegalArgumentError("Illegal NaN parameter: " + value);
			if (!isFinite(value))
				throw new IllegalArgumentError("Illegal infinite parameter: " + value);
			if (value == 0)
				return ZERO;
			if (value < 0)
				throw new IllegalArgumentError("Illegal negative parameter: " + value);
			return forString(value.toFixed());
		}
		
		public static function forString(digits:String, radix:int = 10):MutableBigInteger {
			
			const radixConstraints:Radix = Radix.getRadix(radix),
				  max:int = digits.length;

			if (max == 0)
				throw new NumberFormatError("Zero length BigInteger");
			
			// validate digits: parseInt(...) stops at the first illegal character but doesn't
			// throw an exception.
			if (!radixConstraints.validate(digits))
				throw new NumberFormatError("Illegal digit(s) for radix " + radix + ": " + digits);
			
			// skip leading zeros.
			var first:int = 0;
			while (first < max && digits.charAt(first) == "0")
				first++;
			
			var length:int = max - first;
			if (length <= 0)
				return ZERO;
			
			// initialize the MutableBigInteger with the most significant
			// digits.
			var step:int = radixConstraints.maxUintExponent,
				shift:uint = radixConstraints.maxUintPower,
				seg:String = digits.substr(first, length % step),
				mbi:MutableBigInteger,
				useg:uint;
			
			if (seg.length == 0)
				mbi = ZERO;
			else {
				first += seg.length;
				useg = uint(parseInt(seg, radix));
				mbi = new MutableBigInteger(new Uints([useg]));
			}
			
			// add subsequent digit segments, shifting current value by the
			// maximum uint power of the radix. 
			while (first < max) {
				seg = digits.substr(first, step);
				useg = uint(parseInt(seg, radix));
				mbi.multiplyByUint(shift);
				mbi.addUint(useg);
				first += step;
			}
			
			return mbi;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		internal function get uints():Uints {
			return _uints;
		}
		
		internal function getUintAt(position:int):uint {
			return _uints.getAt(position);
		}
		
		internal function isEven():Boolean {
			const length:int = _uints.length;
        	return (length == 0 || (_uints[length - 1] & 0x1) == 0);
    	}

	    internal function isOdd():Boolean {
			const length:int = _uints.length;
			return (length != 0 && (_uints[length - 1] & 0x1) == 1);
	    }
		
		public function get length():uint {
			return _uints.length;
		}
		
		public function isZero():Boolean {
			return _uints.length == 0;
		}
		
		public function isOne():Boolean {
			return _uints.length == 1 && _uints[0] == 1;
		}
		
		public function isTen():Boolean {
			return _uints.length == 1 && _uints[0] == 10;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Binary operations.
		
		public function add(b:MutableBigInteger):void {
			
			const length:int = _uints.length,
				  bUints:Uints = b._uints,
				  bLength:int = bUints.length;

			// b == 0, nothing to do.
			if (bLength == 0)
				return;
			
			// if this == 0, duplicates b.
			if (length == 0) {
				if (b !== this)
					_uints = bUints.clone();
				return;
			}
			
			// use optimized addUint function if possible.
			if (bLength == 1) {
				addUint(bUints[0]);
				return;
			}
			
			var i:uint, max:int, ua:uint, ub:uint, sum:uint, carry:uint = 0;
			
			// this.length < b.length, copy the most significant part of b
			// into this (the carry will be propagated using those preset values).
			if (length < bLength) {
				_uints.length = bLength;
				for (i = length; i < bLength; i++)
					_uints[i] = bUints[i];
				max = length;
			}
			else
				max = bLength;
			
			// loop over arrays, stopping when the shortest one is consumed.
			for (i = 0; i < max; i++) {

				ua = _uints[i];
				ub = bUints[i];

				// may overflow.
				sum = ua + ub;

				// (sum < ua || sum < ub) ==> sum overflowed.
				if (sum < ua || sum < ub) {
					// sum = ua + ub (overflowed) ==> sum < uint.MAX_VALUE. 
					if (carry != 0)
						sum++;
					else
						carry = 1;
				}
				else if (carry != 0) {
					if (sum == uint.MAX_VALUE)
						sum = 0;
					else {
						sum++;
						carry = 0;
					}
				}
				
				_uints[i] = sum;
			}
			
			if (carry != 0) {
				// propagate the carry.
				max = _uints.length;
				while (i < max) {
					ua = _uints[i];
					if (ua != uint.MAX_VALUE) {
						_uints[i] = ua + 1;
						return;
					}
					_uints[i++] = 0;
				}
				// add one extra uint for the carry.
				_uints[max] = 1;
			}
		}
		
		public function addUint(b:uint):void {

			// b == 0, nothing to do.
			if (b == 0)
				return;
			
			const length:int = _uints.length;
			
			// if this == 0, set this to b.
			if (length == 0) {
				_uints = new Uints([b]);
				return;
			}
			
			var x:uint, a:uint;
			
			// add b to the least significant uint of this.
			a = _uints[0];
			x = a + b;
			_uints[0] = x;
			
			// propagate the carry if a + b overflowed. carry cannot be > 1,
			// because 0xffffffff + 0xffffffff = 0x[1]fffffffe.
			if (x < a || x < b) {
				// reuse x as an index.
				for (x = 1; x < length; x++) {
					a = _uints[x];
					if (a != uint.MAX_VALUE) {
						_uints[x] = a + 1;
						return;
					}
					_uints[x] = 0;
				}
				// add one extra uint for the carry.
				_uints[length] = 1;
			}
		}
		
		public function subtract(b:MutableBigInteger):void {
			
			const length:int = _uints.length,
				  bUints:Uints = b._uints,
				  bLength:int = bUints.length;

			// b == 0 or this == 0, nothing to do.
			if (bLength == 0 || length == 0)
				return;
			
			// this < b --> reset this to 0.
			if (length < bLength) {
				_uints.length = 0;
				return;
			}
			
			var i:uint, ua:uint, ub:uint, sub:uint, carry:uint = 0, imax:int = -1;
			
			// loop over arrays, stopping when the shortest one is consumed.
			for (i = 0; i < bLength; i++) {

				ua = _uints[i];
				ub = bUints[i];

				// may overflow.
				sub = ua - ub;

				// ua < ub ==> sub overflowed.
				if (ua < ub) {
					if (carry != 0)
						sub--; // ua < ub ==> sub = uint(ua - ub) > 0.
					else
						carry = 1;
				}
				else if (carry != 0) {
					if (sub > 0)
						carry = 0;
					// if sub == 0, then sub-- (overflowed) == 0xffffffff.
					sub--;
				}
				
				// keep trace of the last index of a non null value (will be
				// used to truncate leading zeros in the result). 
				if (sub != 0)
					imax = i;
				
				_uints[i] = sub;
			}
			
			if (carry != 0) {
				// propagate the carry.
				while (i < length) {

					// get and remove 1 from _uints[i] (may overflow if u1 == 0).
					ua = _uints[i];
					_uints[i] = ua - 1;
					
					// (u1 > 0) ==> (_uints[i] < 0xffffffff), exit the loop.
					if (ua > 0) {
						if (_uints[length - 1] == 0)
							_uints.length = imax + 1;
						return;
					}
					
					// (u1 == 0) ==> (_uints[i] == 0xffffffff).
					imax = i;
					i++;
				}
				
				// carry outside of this array capacity (negative number),
				// set this array to zero.
				_uints.length = 0;
				return;
			}

			// remove leading zeros.
			if (_uints[length - 1] == 0)
				_uints.length = imax + 1;
		}

		public function multiply(b:MutableBigInteger):void {
			
			const length:int = _uints.length,
				  bUints:Uints = b._uints,
				  bLength:int = bUints.length;

			// if this == 0 or b == 1, nothing to do.
			if (length == 0 || (bLength == 1 && bUints[0] == 1))
				return;
			
			// if b == 0, set this to 0.
			if (bLength == 0) {
				_uints = new Uints();
				return;
			}
			
			// if this == 1, set this to uints.
			if (length == 1 && _uints[0] == 1) {
				copy(b);
				return;
			}
			
			// if b length == 1, use the optimized multiplyByUint
			// function.
			if (bLength == 1) {
				multiplyByUint(bUints[0]);
				return;
			}
			
			var i:int, j:int, ua:uint, ub:uint,
				a0:uint, a1:uint, b0:uint, b1:uint,
				a0b0:uint, a0b1:uint, a1b0:uint, a1b1:uint,
				c:uint = 0,
				product:Array = Uints.newUintArray(length + bLength);
				
			// for each uint pair (ua, ub), an uint carry c and an uint value p (result
			// of previous partial product), the maximum partial product is:
			//
			// (    ua     *     ub    ) +      c     +      p 
			// (0xffffffff * 0xffffffff) + 0xffffffff + 0xffffffff
			// = 0xffffffffffffffff (64 bits set to 1).
			//
			// because we don't have a 64 bits type in AS3, we must operate partial
			// computations with short (16 bits) values.

			// loop over this array.
			for (i = 0; i < length; i++) {
				
				ua = _uints[i];
				
				if (ua != 0) {
	            	
					a0 = (ua & 0xffff);
	            	a1 = (ua >>> 16);

					j = i;
					c = 0;
					
					// loop over b.
					for each (ub in bUints) {
		                
						b0 = (ub & 0xffff);
		                b1 = (ub >>> 16);
						
		                a0b0 = (a0 * b0);
		                a0b1 = (a0 * b1);
		                a1b0 = (a1 * b0);
		                a1b1 = (a1 * b1);
						
						b1 = product[j];
						b0 = b1 + c;
						c = ((b0 < b1 || b0 < c) ? 1 : 0);
						
						b1 = b0;
			            b0 += a0b0;
			            if (b0 < b1 || b0 < a0b0)
			                c++;

						c += a1b1 + (a0b1 >>> 16) + (a1b0 >>> 16);

			            b1 = b0;
			            a0b1 <<= 16;
			            b0 += a0b1;
			            if (b0 < b1 || b0 < a0b1)
			                c++;
			
			            b1 = b0;
			            a1b0 <<= 16;
			            b0 += a1b0;
			            if (b0 < b1 || b0 < a1b0)
			                c++;
						
						product[j++] = b0;
					}

					product[j] = c;
				}
			}
			
			// product.length > 0 but last carry == 0: remove leading 0.
			if (c == 0)
				product.length--;
			
			_uints = new Uints(product);
		}
		
		public function multiplyByUint(b:uint):void {
			
			const length:int = _uints.length;
			
			// if this == 0 or b == 1, nothing to do.
			if (length == 0 || b == 1)
				return;
			
			// if b == 0, set this to 0.
			if (b == 0) {
				_uints = new Uints();
				return;
			}
			
			// if this == 1, set this to b.
			if (length == 1 && _uints[0] == 1) {
				_uints = new Uints([b]);
				return;
			}
			
			const b0:uint = (b & 0xffff),
				  b1:uint = (b >>> 16),
				  product:Array = Uints.newUintArray(length + 1);
			
			var i:int, a:uint, a0:uint, a1:uint,
				a0b0:uint, a0b1:uint, a1b0:uint, a1b1:uint,
				c:uint = 0;
			
			// loop over this array, starting with the least significant uint.
			for (i = 0; i < length; i++) {

				a = _uints[i];
				
				if (a == 0) {
					product[i] = c;
					c = 0;
				}
				else {
					
					a0 = (a & 0xffff);
					a1 = (a >>> 16);
					
					a0b0 = (a0 * b0);
					a0b1 = (a0 * b1);
					a1b0 = (a1 * b0);
					a1b1 = (a1 * b1);
					
					a0 = c + a0b0;
					
					c = ((a0 < c || a0 < a0b0) ? 1 : 0) + a1b1 + (a0b1 >>> 16) + (a1b0 >>> 16);

					a1 = a0;
					a0b1 <<= 16;
					a0 += a0b1;
					if (a0 < a1 || a0 < a0b1)
						c++;
						
					a1 = a0;
					a1b0 <<= 16;
					a0 += a1b0;
					if (a0 < a1 || a0 < a1b0)
						c++;
					
					product[i] = a0;
				}
			}
			
			if (c != 0)
				product[length] = c;
			else
				product.length--;
			
			_uints = new Uints(product);
		}

		public function divide(b:MutableBigInteger):MutableBigInteger {
			
			const length:int = _uints.length,
				  bUints:Uints = b._uints,
				  bLength:int = bUints.length;
			
			// if b == 0, error.
			if (bLength == 0)
				throw new ArithmeticError("Cannot divide by 0");

			// if this == 0, nothing to do (return 0 as the remainder).
			if (length == 0)
				return ZERO;
			
			// use the optimized divideByUint function when possible.
			if (bLength == 1) {
				var r:uint = divideByUint(bUints[0]);
				return (r == 0 ? ZERO : new MutableBigInteger(new Uints([r])));
			}
			
			var comp:int = compareTo(b);
			
			// if b > this, set this to 0 and return the revious value of this as the remainder.
			if (comp < 0) {
				b = clone();
				copy(ZERO);
				return b;
			}

			// if b == this, set this to 1 and return 0 as the remainder.
			if (comp == 0) {
				copy(ONE);
				return ZERO;
			}
			
			// normalize because we are going to work directly on this _uints.
			_uints.normalize();
			
			// b.length > 1. compute the most significant part of b (ie: (b1 << 32) + b0))
			// as a Number (with a possible lose of precision).
			const b1:uint = bUints[bLength - 1],
				  b10:Number = (Number(b1) * Radix.BASE_UINT) + Number(bUints[bLength - 2]),
				  q:Array = Uints.newUintArray(length - bLength + 1);
			
			var a1:uint, a0:uint, qe:uint, qb:MutableBigInteger;

			// initially, b < this and b.length <= this.length.
			do {
				// only consider the most significant part of this (with
				// length = b.length).
				_uints.start = _uints.end - bLength;
				
				// get the most significant uints of this.
				a1 = _uints[_uints.length - 1];
				a0 = _uints[_uints.length - 2];
				
				// compare the most significant part of this (b length) to
				// b.
				comp = compareTo(b);
				
				// compute an estimated partial quotient qe.
				// if the most significant part of this is >= b.
				if (comp >= 0) {
					// a1 == b1: qe = a1 ? ? ? ... / b1 ? ? ? ... in [1, 2).
					if (a1 == b1)
						qe = 1;
					// qe = ((a1 << 32) + a0) / ((b1 << 32) + b0).
					else
						qe = ((Number(a1) * Radix.BASE_UINT) + Number(a0)) / b10;
				}
				// if the most significant part of this is < b.
				else {
					// no extra digit left in this (--> remainder).
					if (_uints.start == 0)
						break;
					// considere one more uint of this (b.length + 1).
					_uints.start--;
					// qe = ((a1 << 64) + (a0 << 32)) / ((b1 << 32) + b0).
					qe = ((Number(a1) * Radix.BASE_UINT_SQUARE) + (Number(a0) * Radix.BASE_UINT)) / b10;
				}
				
				// compute qb = qe * b.
				if (qe == 1)
					qb = b;
				else {
					qb = b.clone();
					qb.multiplyByUint(qe);
				}
				
				// compare the most significant part of this (b.length [+0/1]) to
				// qb = qe * b. most of the time, the estimated partial quotient
				// is right, but the following code makes sure of it. 
				comp = compareTo(qb);
				
				// this (the most significant...) == qe * b.
				if (comp == 0) {
					subtract(qb);
				}
				// this (the most significant...) < qe * b: find the first qe so
				// qe * b <= this (...).
				else if (comp < 0) {
					do {
						assert(qe > 1);
						
						qe--;
						qb.subtract(b);
						comp = compareTo(qb);
					}
					while (comp < 0);

					subtract(qb);
				}
				// this (the most significant...) > qe * b: subtract (qe * b) and
				// make sure that the result is < b.
				else if (comp > 0) {
					
					subtract(qb);
					comp = compareTo(b);
					
					while (comp >= 0) {
						qe++;
						subtract(b);
						comp = compareTo(b);
					}
				}

				// store the partial quotient.
				q[_uints.start] = qe;
				
				// reset start to 0 before computing _uints.length.
				_uints.start = 0;
			}
			while (_uints.length >= bLength);
			
			// remove leading zero if any.
			if (q[q.length - 1] == 0)
				q.length--;

			// store the remainder in a new MutableBigInteger and the quotient
			// in this.
			var remainder:MutableBigInteger = clone();
			_uints = new Uints(q);
			return remainder;
		}
		
		public function divideByUint(b:uint):uint {
			
			if (b == 0)
				throw new ArithmeticError("Cannot divide by 0");
			if (b == 1)
				return 0;
			
			if (_uints.length == 0)
				return 0;
			
			const length:int = _uints.length,
				  q:Array = Uints.newUintArray(length),
				  p:Array = [0, 0],
				  bn:Number = Number(b);
			
			var i:int, a:uint, q1:uint, q0:uint, p1:uint, p0:uint, r:uint = 0;
			
			// loop over this integer, starting with the most significant
			// value.
			for (i = length - 1; i >= 0; i--) {
				
				a = _uints[i];
				
				// easy (and initial) case: no remainder.
				if (r == 0) {
					if (a == b)
						q[i] = 1;
					else if (a < b) {
						q[i] = 0;
						r = a;
					}
					else {
						q0 = uint(a / b);
						q[i] = q0;
						r = a - (q0 * b);
					}
				}
				// r > 0.
				else {
					// calculate an estimate quotient: ((r << 32) + a) / b.
					// we don't have a 64 bits type, so we use the Number type instead (53 bits
					// of precision isn't a problem here because (Number(r) * _BASE) is
					// equivalent to (r << 32), ie: 0x????????00000000, with only 32 significant
					// bits).
					q1 = (Number(r) * Radix.BASE_UINT) / bn;
                    q0 = Number(a) / bn;
                    if (q1 < uint.MAX_VALUE - q0)
                        q1 += q0;
                    else
                        q1 = uint.MAX_VALUE;
					
					// calculate q1 * b.
					multiplyUints(q1, b, p);

					p1 = p[1];
					p0 = p[0];

					// correct the estimate (assume a +/-1 error).
					if (r == p1) {
						if (p0 > a) {
							q1--;
							p0 -= b;
						}
						else if (a > b && a - b >= p0) {
							q1++;
							p0 += b;
						}
					}
					else {
						assert((r == p1 + 1) && (p0 > uint.MAX_VALUE - b));
						// let p0 + b overflow.
						if (uint(p0 + b) <= a) {
							q1++;
							p0 += b;
						}
					}
					
					q[i] = q1;
					
					// calculate the new remainder.
					r = a - p0;
					assert(r < b);
				}
			}

			// remove leading 0 if any.
			if (q[q.length - 1] == 0)
				q.length--;
			
			this._uints = new Uints(q);
			
			return r;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Comparisons.

		public function compareTo(b:MutableBigInteger):int {
			if (this === b)
				return 0;
			
			const length:int = _uints.length,
				  bUints:Uints = b._uints,
				  bLength:int = bUints.length;
			
			if (length != bLength)
				return (length < bLength ? -1 : 1);
			
			var i:int, ua:uint, ub:uint;

			for (i = length - 1; i >= 0; i--) {
				ua = _uints[i];
				ub = bUints[i];
				if (ua != ub)
					return (ua < ub ? -1 : 1);
			}
			
			return 0;
		}
		
		public function equals(b:MutableBigInteger):Boolean {
			if (b == null)
				return false;
			return compareTo(b) == 0;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conversions.
		
		public function toUint():uint {
			if (_uints.length == 0)
				return 0;
			return _uints[0];
		}
		
		public function toNumber():Number {
			return new Number(toString());
		}

		public function toString(radix:int = 10):String {

			var radixConstraints:Radix = Radix.getRadix(radix);

			if (_uints.length == 0)
				return "0";
			
			var s:String = "",
				mbi:MutableBigInteger = clone(),
				div:uint = radixConstraints.maxUintPower,
				zr:uint = radixConstraints.maxUintExponent,
				done:Boolean = false,
				rs:String,
				nb0:uint;

			do {
				rs = mbi.divideByUint(div).toString(radix);
				done = (mbi.length == 0);
				
				if (done || (nb0 = zr - rs.length) == 0)
					s = rs + s;
				else
					s = Radix.getZeros(nb0) + rs + s;
			}
			while (!done);
			
			return s;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		public function clone():MutableBigInteger {
			return new MutableBigInteger(_uints.clone());
		}
		
		public function copy(b:MutableBigInteger):void {
			_uints = b._uints.clone();
		}
		
		internal static function multiplyUints(a:uint, b:uint, r:Array):void {
			var a0:uint = (a & 0xffff),
				a1:uint = (a >>> 16),
				b0:uint = (b & 0xffff),
				b1:uint = (b >>> 16),
				
				a0b0:uint = (a0 * b0),
				a0b1:uint = (a0 * b1),
				a1b0:uint = (a1 * b0),
				a1b1:uint = (a1 * b1);
			
			b0 = (a0b1 << 16);
			a0 = a0b0 + b0;
			if (a0 < a0b0 || a0 < b0)
				a1 = 1;
			else
				a1 = 0;
			
			b0 = (a1b0 << 16);
			b1 = a0 + b0;
			if (b1 < a0 || b1 < b0)
				a1++;
			
			// return a little endian result.
			r[0] = b1;
			// 0x2 + 0xfffe0001 + 0xfffe + 0xfffe = 0xffffffff max.
			r[1] = a1 + a1b1 + (a0b1 >>> 16) + (a1b0 >>> 16);
		}
	}
}
