/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.math {

	import org.flexunit.Assert;
	
	import org.granite.math.BigInteger;
	import org.granite.math.BigNumberError;
	import org.granite.math.NumberFormatError;

	/**
	 * @author Franck WOLFF
	 * @private
	 */
	public class TestBigInteger {

		[Test]
        public function testConstants():void {
			Assert.assertEquals("0", BigInteger.ZERO.toString(16));
			Assert.assertEquals("1", BigInteger.ONE.toString(16));
			Assert.assertEquals("a", BigInteger.TEN.toString(16));
		}
		
		[Test]
        public function testEquals():void {
			Assert.assertTrue(BigInteger.ZERO.equals(BigInteger.ZERO));
			Assert.assertTrue(BigInteger.ZERO.equals(0));
			Assert.assertTrue(BigInteger.ZERO.equals("0"));
			Assert.assertFalse(BigInteger.ZERO.equals(BigInteger.ONE));
			Assert.assertFalse(BigInteger.ZERO.equals(1));
			Assert.assertFalse(BigInteger.ZERO.equals("1"));
			Assert.assertFalse(BigInteger.ZERO.equals(new Array()));
			
			Assert.assertTrue(BigInteger.ONE.equals(BigInteger.ONE));
			Assert.assertTrue(BigInteger.ONE.equals(1));
			Assert.assertTrue(BigInteger.ONE.equals("1"));
			Assert.assertFalse(BigInteger.ONE.equals(BigInteger.TEN));
			Assert.assertFalse(BigInteger.ONE.equals(10));
			Assert.assertFalse(BigInteger.ONE.equals("10"));
			Assert.assertFalse(BigInteger.ONE.equals(new Array()));
			
			Assert.assertTrue(BigInteger.TEN.equals(BigInteger.TEN));
			Assert.assertTrue(BigInteger.TEN.equals(10));
			Assert.assertTrue(BigInteger.TEN.equals("10"));
			Assert.assertFalse(BigInteger.TEN.equals(BigInteger.ONE));
			Assert.assertFalse(BigInteger.TEN.equals(1));
			Assert.assertFalse(BigInteger.TEN.equals("1"));
			Assert.assertFalse(BigInteger.TEN.equals(new Array()));
		}
		
		[Test]
        public function testForBigInteger():void {
			var a:BigInteger;
			
			a = new BigInteger(BigInteger.ZERO);
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger(BigInteger.ONE);
			Assert.assertEquals("1", a.toString(16));
			
			a = new BigInteger(BigInteger.TEN);
			Assert.assertEquals("a", a.toString(16));
			
			a = new BigInteger(new BigInteger("3213465adcfa456", 16));
			Assert.assertEquals("3213465adcfa456", a.toString(16));
			
			a = new BigInteger(new BigInteger("-3213465adcfa456", 16));
			Assert.assertEquals("-3213465adcfa456", a.toString(16));
		}
		
		[Test]
        public function testForString():void {
			var a:BigInteger;
			
			a = new BigInteger(null, 16);
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger("0", 16);
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger("-0", 16);
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger("1", 16);
			Assert.assertEquals("1", a.toString(16));
			
			a = new BigInteger("-1", 16);
			Assert.assertEquals("-1", a.toString(16));
			
			a = new BigInteger("3213465adcfa456", 16);
			Assert.assertEquals("3213465adcfa456", a.toString(16));
			
			a = new BigInteger("-3213465adcfa456", 16);
			Assert.assertEquals("-3213465adcfa456", a.toString(16));
			
			try {
				a = new BigInteger("", 16);
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Zero length BigInteger", e.message);
			}

			try {
				a = new BigInteger("-", 16);
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Zero length BigInteger", e.message);
			}
			
			try {
				a = new BigInteger("+", 16);
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal digit(s) for radix 16: +", e.message);
			}
			
			try {
				a = new BigInteger("+0", 16);
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal digit(s) for radix 16: +0", e.message);
			}
			
			try {
				a = new BigInteger("abcdefg", 16);
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal digit(s) for radix 16: abcdefg", e.message);
			}
		}
		
		[Test]
        public function testForInt():void {
			var a:BigInteger;
			
			a = new BigInteger(int(0));
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger(int(1));
			Assert.assertEquals("1", a.toString(16));
			
			a = new BigInteger(int(-1));
			Assert.assertEquals("-1", a.toString(16));
			
			a = new BigInteger(int.MIN_VALUE);
			Assert.assertEquals("-80000000", a.toString(16));
			
			a = new BigInteger(int.MAX_VALUE);
			Assert.assertEquals("7fffffff", a.toString(16));
		}
		
		[Test]
        public function testForNumber():void {
			var a:BigInteger;
			
			a = new BigInteger(Number(0));
			Assert.assertEquals("0", a.toString(16));
			
			a = new BigInteger(Number(1));
			Assert.assertEquals("1", a.toString(16));
			
			a = new BigInteger(Number(-1));
			Assert.assertEquals("-1", a.toString(16));
			
			a = new BigInteger(Number(int.MIN_VALUE));
			Assert.assertEquals("-80000000", a.toString(16));
			
			a = new BigInteger(Number(int.MAX_VALUE));
			Assert.assertEquals("7fffffff", a.toString(16));
			
			a = new BigInteger(-Number(uint.MAX_VALUE));
			Assert.assertEquals("-ffffffff", a.toString(16));
			
			a = new BigInteger(Number(uint.MAX_VALUE));
			Assert.assertEquals("ffffffff", a.toString(16));
			
			a = new BigInteger(Number.MAX_VALUE);
			Assert.assertEquals(Number.MAX_VALUE.toFixed(0), a.toString(10));
			Assert.assertEquals("fffffffffffff7ac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", a.toString(16));
			
			a = new BigInteger(-Number.MAX_VALUE);
			Assert.assertEquals((-Number.MAX_VALUE).toFixed(0), a.toString(10));
			Assert.assertEquals("-fffffffffffff7ac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", a.toString(16));
		}
		
		[Test]
        public function testSign():void {
			var a:BigInteger;
			
			a = new BigInteger("0");
			Assert.assertEquals(0, a.sign);
			
			a = new BigInteger("-0");
			Assert.assertEquals(0, a.sign);
			
			a = new BigInteger("1");
			Assert.assertEquals(1, a.sign);
			
			a = new BigInteger("-1");
			Assert.assertEquals(-1, a.sign);
		}
		
		[Test]
        public function testAbs():void {
			var a:BigInteger;
			
			a = new BigInteger("0");
			a = a.abs();
			Assert.assertEquals("0", a.toString());
			
			a = new BigInteger("-0");
			a = a.abs();
			Assert.assertEquals("0", a.toString());
			
			a = new BigInteger("1");
			a = a.abs();
			Assert.assertEquals("1", a.toString());
			
			a = new BigInteger("-1");
			a = a.abs();
			Assert.assertEquals("1", a.toString());
		}
		
		[Test]
        public function testNegate():void {
			var a:BigInteger;
			
			a = new BigInteger("0");
			a = a.negate();
			Assert.assertEquals("0", a.toString());
			
			a = new BigInteger("-0");
			a = a.negate();
			Assert.assertEquals("0", a.toString());
			
			a = new BigInteger("1");
			a = a.negate();
			Assert.assertEquals("-1", a.toString());
			
			a = new BigInteger("-1");
			a = a.negate();
			Assert.assertEquals("1", a.toString());
		}
		
		[Test]
        public function testMin():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("0");
			c = a.min(b);
			Assert.assertStrictlyEquals(c, b);
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.min(b);
			Assert.assertStrictlyEquals(c, a);
			
			a = new BigInteger("-1");
			b = new BigInteger("0");
			c = a.min(b);
			Assert.assertStrictlyEquals(c, a);
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.min(b);
			Assert.assertStrictlyEquals(c, a);
		}
		
		[Test]
        public function testMax():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("0");
			c = a.max(b);
			Assert.assertStrictlyEquals(c, b);
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.max(b);
			Assert.assertStrictlyEquals(c, b);
			
			a = new BigInteger("-1");
			b = new BigInteger("0");
			c = a.max(b);
			Assert.assertStrictlyEquals(c, b);
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.max(b);
			Assert.assertStrictlyEquals(c, b);
		}
		
		[Test]
        public function testAdd():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("0");
			c = a.add(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.add(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("0");
			c = a.add(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.add(b);
			Assert.assertEquals("2", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.add(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("0");
			c = a.add(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.add(b);
			Assert.assertEquals("-2", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.add(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.add(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("5");
			b = new BigInteger("5");
			c = a.add(b);
			Assert.assertEquals("10", c.toString());
		}
		
		[Test]
        public function testSubtract():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("0");
			c = a.subtract(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.subtract(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("0");
			c = a.subtract(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.subtract(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.subtract(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("0");
			c = a.subtract(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.subtract(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.subtract(b);
			Assert.assertEquals("-2", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.subtract(b);
			Assert.assertEquals("2", c.toString());
			
			a = new BigInteger("5");
			b = new BigInteger("-5");
			c = a.subtract(b);
			Assert.assertEquals("10", c.toString());
		}
		
		[Test]
        public function testMultiply():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("0");
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("0");
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.multiply(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("0");
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.multiply(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.multiply(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.multiply(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("5");
			b = new BigInteger("2");
			c = a.multiply(b);
			Assert.assertEquals("10", c.toString());
		}
		
		[Test]
        public function testDivide():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.divide(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.divide(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.divide(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.divide(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigInteger("-20");
			b = new BigInteger("-2");
			c = a.divide(b);
			Assert.assertEquals("10", c.toString());
		}
		
		[Test]
        public function testRemainder():void {
			var a:BigInteger, b:BigInteger, c:BigInteger;
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.remainder(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigInteger("2");
			b = new BigInteger("3");
			c = a.remainder(b);
			Assert.assertEquals("2", c.toString());
			
			a = new BigInteger("2");
			b = new BigInteger("-3");
			c = a.remainder(b);
			Assert.assertEquals("2", c.toString());
			
			a = new BigInteger("-2");
			b = new BigInteger("-3");
			c = a.remainder(b);
			Assert.assertEquals("-2", c.toString());
			
			a = new BigInteger("-2");
			b = new BigInteger("3");
			c = a.remainder(b);
			Assert.assertEquals("-2", c.toString());
		}
		
		[Test]
		[Test]
        public function testDivideAndRemainder():void {
			var a:BigInteger, b:BigInteger, c:Array;
			
			a = new BigInteger("0");
			b = new BigInteger("1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("1");
			b = new BigInteger("1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("1", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("0");
			b = new BigInteger("-1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("-1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("1", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("-1");
			b = new BigInteger("1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("-1", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("1");
			b = new BigInteger("-1");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("-1", c[0].toString());
			Assert.assertEquals("0", c[1].toString());
			
			a = new BigInteger("2");
			b = new BigInteger("3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("2", c[1].toString());
			
			a = new BigInteger("2");
			b = new BigInteger("-3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("2", c[1].toString());
			
			a = new BigInteger("-2");
			b = new BigInteger("-3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("-2", c[1].toString());
			
			a = new BigInteger("-2");
			b = new BigInteger("3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("0", c[0].toString());
			Assert.assertEquals("-2", c[1].toString());
			
			a = new BigInteger("7");
			b = new BigInteger("3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("2", c[0].toString());
			Assert.assertEquals("1", c[1].toString());
			
			a = new BigInteger("7");
			b = new BigInteger("-3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("-2", c[0].toString());
			Assert.assertEquals("1", c[1].toString());
			
			a = new BigInteger("-7");
			b = new BigInteger("-3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("2", c[0].toString());
			Assert.assertEquals("-1", c[1].toString());
			
			a = new BigInteger("-7");
			b = new BigInteger("3");
			c = a.divideAndRemainder(b);
			Assert.assertEquals("-2", c[0].toString());
			Assert.assertEquals("-1", c[1].toString());
		}
//		
//		[Test]
//      public function testTestBit():void {
//			var a:BigInteger, r:Boolean, i:int, s:String;
//			
//			a = new BigInteger("0");
//			r = a.testBit(0);
//			Assert.assertEquals(false, r);
//			
//			a = new BigInteger("1");
//			r = a.testBit(0);
//			Assert.assertEquals(true, r);
//			r = a.testBit(1);
//			Assert.assertEquals(false, r);
//			r = a.testBit(2);
//			Assert.assertEquals(false, r);
//			
//			a = new BigInteger("2");
//			r = a.testBit(0);
//			Assert.assertEquals(false, r);
//			r = a.testBit(1);
//			Assert.assertEquals(true, r);
//			r = a.testBit(2);
//			Assert.assertEquals(false, r);
//			
//			a = new BigInteger("3");
//			r = a.testBit(0);
//			Assert.assertEquals(true, r);
//			r = a.testBit(1);
//			Assert.assertEquals(true, r);
//			r = a.testBit(2);
//			Assert.assertEquals(false, r);
//			
//			a = new BigInteger("4");
//			r = a.testBit(0);
//			Assert.assertEquals(false, r);
//			r = a.testBit(1);
//			Assert.assertEquals(false, r);
//			r = a.testBit(2);
//			Assert.assertEquals(true, r);
//			
//			a = new BigInteger("100000000", 16);
//			for (i = 0; i < 100; i++) {
//				r = a.testBit(i);
//				if (i == 32)
//					Assert.assertEquals(true, r);
//				else
//					Assert.assertEquals(false, r);
//			}
//			
//			a = new BigInteger("ffffffff00000000", 16);
//			for (i = 0; i < 100; i++) {
//				r = a.testBit(i);
//				if (i >= 32 && i <= 63)
//					Assert.assertEquals(true, r);
//				else
//					Assert.assertEquals(false, r);
//			}
//			
//			s = "101010101010101010101010101010101010101010";
//			a = new BigInteger(s, 2);
//			for (i = 0; i < 100; i++) {
//				r = a.testBit(i);
//				if (i < s.length && (i % 2) == 1)
//					Assert.assertEquals(true, r);
//				else
//					Assert.assertEquals(false, r);
//			}
//		}
		
		[Test]
        public function testToInt():void {
			var a:BigInteger, i:int;
			
			a = new BigInteger("0");
			i = a.toInt();
			Assert.assertEquals(0, i);
			
			a = new BigInteger("1");
			i = a.toInt();
			Assert.assertEquals(1, i);
			
			a = new BigInteger("-1");
			i = a.toInt();
			Assert.assertEquals(-1, i);
			
			a = new BigInteger("-80000000", 16);
			i = a.toInt();
			Assert.assertEquals(int.MIN_VALUE, i);
			
			a = new BigInteger("80000000", 16);
			i = a.toInt();
			Assert.assertEquals(0, i);
			
			a = new BigInteger("7fffffff", 16);
			i = a.toInt();
			Assert.assertEquals(int.MAX_VALUE, i);
			
			a = new BigInteger("ffffffff", 16);
			i = a.toInt();
			Assert.assertEquals(int.MAX_VALUE, i);
			
			a = new BigInteger("-fffffff", 16);
			i = a.toInt();
			Assert.assertEquals(-0xfffffff, i);
			
			a = new BigInteger("-7fffffff", 16);
			i = a.toInt();
			Assert.assertEquals(-int.MAX_VALUE, i);
			
			a = new BigInteger("-ffffffff", 16);
			i = a.toInt();
			Assert.assertEquals(-int.MAX_VALUE, i);
		}
		
		[Test]
        public function testToNumber():void {
			var a:BigInteger, i:Number;
			
			a = new BigInteger("0");
			i = a.toNumber();
			Assert.assertEquals(0, i);
			
			a = new BigInteger("1");
			i = a.toNumber();
			Assert.assertEquals(1, i);
			
			a = new BigInteger("-1");
			i = a.toNumber();
			Assert.assertEquals(-1, i);
			
			a = new BigInteger("-80000000", 16);
			i = a.toNumber();
			Assert.assertEquals(Number(int.MIN_VALUE), i);
			
			a = new BigInteger("7fffffff", 16);
			i = a.toNumber();
			Assert.assertEquals(Number(int.MAX_VALUE), i);
			
			a = new BigInteger("ffffffff", 16);
			i = a.toNumber();
			Assert.assertEquals(Number(0xffffffff), i);
			
			a = new BigInteger("-ffffffff", 16);
			i = a.toNumber();
			Assert.assertEquals(-Number(0xffffffff), i);
			
			a = new BigInteger("fffffffffffff7ac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", 16);
			i = a.toNumber();
			Assert.assertEquals(Number.MAX_VALUE, i);
			
			a = new BigInteger("fffffffffffffcac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", 16);
			i = a.toNumber();
			Assert.assertEquals(Number.POSITIVE_INFINITY, i);
			
			a = new BigInteger("-fffffffffffff7ac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", 16);
			i = a.toNumber();
			Assert.assertEquals(-Number.MAX_VALUE, i);
			
			a = new BigInteger("-fffffffffffffcac6b26715bb52611f3557ab9dff9b8b22ed97fd6ccd362d748b926b935ae4cef9ce976c482510fe77592128540fd165024271d198d0f4dc7903c685ce0d8f622cfbd38498d87a8565633a38b99f3a6aa7406629e50000000000000000000000000000000000000000000000000000000000000000000000000", 16);
			i = a.toNumber();
			Assert.assertEquals(Number.NEGATIVE_INFINITY, i);
		}
//		
//		[Test]
//      public function testBigPowerOfTen():void {
//			var a:BigInteger, i:int, s:String;
//			
//			a = BigInteger.bigPowerOfTen(-1);
//			Assert.assertEquals("0", a.toString());
//			
//			s = "1";
//			for (i = 0; i < 100; i++) {
//				a = BigInteger.bigPowerOfTen(i);
//				Assert.assertEquals(s, a.toString());
//				s += "0";
//			}
//		}
	}
}