/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
	
	import org.granite.math.ArithmeticError;
	import org.granite.math.Long;
	import org.granite.math.NumberFormatError;

	/**
	 * @author Franck WOLFF
	 * @private
	 */
	public class TestLong {

		[Test]
        public function testConstants():void {
			Assert.assertEquals("0", Long.ZERO.toHexString());
			Assert.assertEquals("1", Long.ONE.toHexString());
			Assert.assertEquals("a", Long.TEN.toHexString());
		}
		
		[Test]
        public function testForString():void {
			var a:Long, s:String;

			// java.lang.Long.MIN_VALUE.
			a = new Long("-9223372036854775808");
			Assert.assertEquals("8000000000000000", a.toHexString());

			// java.lang.Long.MIN_VALUE + 1.
			a = new Long("-9223372036854775807");
			Assert.assertEquals("8000000000000001", a.toHexString());
			
			// -4294967295
			s = "-" + uint.MAX_VALUE.toString(10);
			Assert.assertEquals("-4294967295", s);
			a = new Long(s);
			Assert.assertEquals("ffffffff00000001", a.toHexString());
			
			// -ffffffff
			s = "-" + uint.MAX_VALUE.toString(16);
			Assert.assertEquals("-ffffffff", s);
			a = new Long(s, 16);
			Assert.assertEquals("ffffffff00000001", a.toHexString());

			// -2147483647
			s = "-" + int.MAX_VALUE.toString(10);
			Assert.assertEquals("-2147483647", s);
			a = new Long(s);
			Assert.assertEquals("ffffffff80000001", a.toHexString());

			// -7fffffff
			s = "-" + int.MAX_VALUE.toString(16);
			Assert.assertEquals("-7fffffff", s);
			a = new Long(s, 16);
			Assert.assertEquals("ffffffff80000001", a.toHexString());

			// -2147483648
			s = int.MIN_VALUE.toString(10);
			Assert.assertEquals("-2147483648", s);
			a = new Long(s);
			Assert.assertEquals("ffffffff80000000", a.toHexString());

			// -7fffffff
			s = int.MIN_VALUE.toString(16);
			Assert.assertEquals("-80000000", s);
			a = new Long(s, 16);
			Assert.assertEquals("ffffffff80000000", a.toHexString());

			a = new Long("-10", 10);
			Assert.assertEquals("fffffffffffffff6", a.toHexString());

			a = new Long("-a", 16);
			Assert.assertEquals("fffffffffffffff6", a.toHexString());

			a = new Long("-2");
			Assert.assertEquals("fffffffffffffffe", a.toHexString());
			
			a = new Long("-1");
			Assert.assertEquals("ffffffffffffffff", a.toHexString());

			a = new Long("0");
			Assert.assertEquals("0", a.toHexString());
			
			a = new Long("1");
			Assert.assertEquals("1", a.toHexString());

			a = new Long("2");
			Assert.assertEquals("2", a.toHexString());

			a = new Long("10", 10);
			Assert.assertEquals("a", a.toHexString());

			a = new Long("a", 16);
			Assert.assertEquals("a", a.toHexString());

			// 2147483647
			s = int.MAX_VALUE.toString(10);
			Assert.assertEquals("2147483647", s);
			a = new Long(s);
			Assert.assertEquals("7fffffff", a.toHexString());

			// 7fffffff
			s = int.MAX_VALUE.toString(16);
			Assert.assertEquals("7fffffff", s);
			a = new Long(s, 16);
			Assert.assertEquals(s, a.toHexString());

			// 4294967295
			s = uint.MAX_VALUE.toString();
			Assert.assertEquals("4294967295", s);
			a = new Long(s);
			Assert.assertEquals("ffffffff", a.toHexString());

			// ffffffff
			s = uint.MAX_VALUE.toString(16);
			Assert.assertEquals("ffffffff", s);
			a = new Long(s, 16);
			Assert.assertEquals(s, a.toHexString());

			// java.lang.Long.MAX_VALUE - 1.
			a = new Long("9223372036854775806", 10);
			Assert.assertEquals("7ffffffffffffffe", a.toHexString());

			// java.lang.Long.MAX_VALUE.
			a = new Long("9223372036854775807", 10);
			Assert.assertEquals("7fffffffffffffff", a.toHexString());
		}
		
		[Test]
        public function testForInt():void {
			var a:Long;

			// -2147483648
			a = new Long(int.MIN_VALUE);
			Assert.assertEquals("ffffffff80000000", a.toHexString());

			// -2147483647
			a = new Long(int.MIN_VALUE + 1);
			Assert.assertEquals("ffffffff80000001", a.toHexString());

			a = new Long(-10);
			Assert.assertEquals("fffffffffffffff6", a.toHexString());

			a = new Long(-2);
			Assert.assertEquals("fffffffffffffffe", a.toHexString());
			
			a = new Long(-1);
			Assert.assertEquals("ffffffffffffffff", a.toHexString());

			a = new Long(0);
			Assert.assertEquals("0", a.toHexString());
			
			a = new Long(1);
			Assert.assertEquals("1", a.toHexString());

			a = new Long(2);
			Assert.assertEquals("2", a.toHexString());

			a = new Long(10, 10);
			Assert.assertEquals("a", a.toHexString());

			// 2147483646
			a = new Long(int.MAX_VALUE - 1);
			Assert.assertEquals("7ffffffe", a.toHexString());

			// 2147483647
			a = new Long(int.MAX_VALUE);
			Assert.assertEquals("7fffffff", a.toHexString());
		}
		
		[Test]
        public function testForNumber():void {
			var a:Long;

			// java.lang.Long.MIN_VALUE (not representable as a Number)
			// Number(-9223372036854775808) -> "-9223372036854776000"
			try {
				a = new Long(Number(-9223372036854775808));
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Too large value for a Long: -9223372036854776000", e.message);
			}
			
			// Number(-9223372036854775296) -> "-9223372036854776000"
			try {
				a = new Long(Number(-9223372036854775296));
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Too large value for a Long: -9223372036854776000", e.message);
			}

			// Number(-9223372036854775295) -> "-9223372036854775000" (loose of precision)
			a = new Long(Number(-9223372036854775295));
			Assert.assertEquals("8000000000000328", a.toHexString());

			// exact.
			a = new Long(Number(-9223372036854775000));
			Assert.assertEquals("8000000000000328", a.toHexString());

			// exact.
			a = new Long(-Number(uint.MAX_VALUE));
			Assert.assertEquals("ffffffff00000001", a.toHexString());

			// exact.
			a = new Long(Number(uint.MAX_VALUE) + Number(1));
			Assert.assertEquals("100000000", a.toHexString());

			// exact.
			a = new Long(Number(9223372036854775000));
			Assert.assertEquals("7ffffffffffffcd8", a.toHexString());

			// Number(9223372036854775295) -> "9223372036854775000" (loose of precision)
			a = new Long(Number(9223372036854775295));
			Assert.assertEquals("7ffffffffffffcd8", a.toHexString());
			
			// Number(9223372036854775296) -> "9223372036854776000"
			try {
				a = new Long(Number(9223372036854775296));
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Too large value for a Long: 9223372036854776000", e.message);
			}

			// java.lang.Long.MAX_VALUE (not representable as a Number)
			// Number(9223372036854775807) -> "9223372036854776000"
			try {
				a = new Long(Number(9223372036854775807));
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Too large value for a Long: 9223372036854776000", e.message);
			}
		}
		
		[Test]
        public function testAbs():void {
			var a:Long, b:Long;
			
			a = new Long(0);
			b = a.abs();
			Assert.assertEquals("0", b.toHexString());
			
			a = new Long(1);
			b = a.abs();
			Assert.assertEquals("1", b.toHexString());
			
			a = new Long(-1);
			b = a.abs();
			Assert.assertEquals("1", b.toHexString());
			
			a = Long.MIN_VALUE;
			b = a.abs();
			Assert.assertEquals("8000000000000000", b.toHexString());
		}
		
		[Test]
        public function testNegate():void {
			var a:Long, b:Long;
			
			a = new Long(0);
			b = a.negate();
			Assert.assertEquals("0", b.toHexString());
			
			a = new Long(1);
			b = a.negate();
			Assert.assertEquals("ffffffffffffffff", b.toHexString());
			
			a = new Long(-1);
			b = a.negate();
			Assert.assertEquals("1", b.toHexString());
			
			a = new Long("100000000", 16);
			b = a.negate();
			Assert.assertEquals("ffffffff00000000", b.toHexString());
			
			a = new Long("-100000000", 16);
			b = a.negate();
			Assert.assertEquals("100000000", b.toHexString());
			
			a = Long.MAX_VALUE
			b = a.negate();
			Assert.assertEquals("8000000000000001", b.toHexString());
			
			a = Long.MIN_VALUE
			b = a.negate();
			Assert.assertEquals("8000000000000000", b.toHexString());
		}
		
		[Test]
        public function testNot():void {
			var a:Long, b:Long;
			
			a = new Long(0);
			b = a.not();
			Assert.assertEquals("ffffffffffffffff", b.toHexString());
			
			a = new Long(1);
			b = a.not();
			Assert.assertEquals("fffffffffffffffe", b.toHexString());
			
			a = new Long(-1);
			b = a.not();
			Assert.assertEquals("0", b.toHexString());
			
			a = Long.MAX_VALUE;
			b = a.not();
			Assert.assertEquals("8000000000000000", b.toHexString());
			
			a = Long.MIN_VALUE;
			b = a.not();
			Assert.assertEquals("7fffffffffffffff", b.toHexString());
		}
		
		[Test]
        public function testLeftShift():void {
			var a:Long, b:Long, i:int, results:Array;
			
			results = [
				"1",
				"2",
				"4",
				"8",
				"10",
				"20",
				"40",
				"80",
				"100",
				"200",
				"400",
				"800",
				"1000",
				"2000",
				"4000",
				"8000",
				"10000",
				"20000",
				"40000",
				"80000",
				"100000",
				"200000",
				"400000",
				"800000",
				"1000000",
				"2000000",
				"4000000",
				"8000000",
				"10000000",
				"20000000",
				"40000000",
				"80000000",
				"100000000",
				"200000000",
				"400000000",
				"800000000",
				"1000000000",
				"2000000000",
				"4000000000",
				"8000000000",
				"10000000000",
				"20000000000",
				"40000000000",
				"80000000000",
				"100000000000",
				"200000000000",
				"400000000000",
				"800000000000",
				"1000000000000",
				"2000000000000",
				"4000000000000",
				"8000000000000",
				"10000000000000",
				"20000000000000",
				"40000000000000",
				"80000000000000",
				"100000000000000",
				"200000000000000",
				"400000000000000",
				"800000000000000",
				"1000000000000000",
				"2000000000000000",
				"4000000000000000",
				"8000000000000000",
				"1",
				"2"
			];
			
			a = Long.ONE;
			for (i = 0; i < 66; i++) {
				b = a.leftShift(i);
				Assert.assertEquals(results[i], b.toHexString());
			}
			
			results = [
				"a",
				"14",
				"28",
				"50",
				"a0",
				"140",
				"280",
				"500",
				"a00",
				"1400",
				"2800",
				"5000",
				"a000",
				"14000",
				"28000",
				"50000",
				"a0000",
				"140000",
				"280000",
				"500000",
				"a00000",
				"1400000",
				"2800000",
				"5000000",
				"a000000",
				"14000000",
				"28000000",
				"50000000",
				"a0000000",
				"140000000",
				"280000000",
				"500000000",
				"a00000000",
				"1400000000",
				"2800000000",
				"5000000000",
				"a000000000",
				"14000000000",
				"28000000000",
				"50000000000",
				"a0000000000",
				"140000000000",
				"280000000000",
				"500000000000",
				"a00000000000",
				"1400000000000",
				"2800000000000",
				"5000000000000",
				"a000000000000",
				"14000000000000",
				"28000000000000",
				"50000000000000",
				"a0000000000000",
				"140000000000000",
				"280000000000000",
				"500000000000000",
				"a00000000000000",
				"1400000000000000",
				"2800000000000000",
				"5000000000000000",
				"a000000000000000",
				"4000000000000000",
				"8000000000000000",
				"0",
				"a",
				"14"
			];
			
			a = Long.TEN;
			for (i = 0; i < 66; i++) {
				b = a.leftShift(i);
				Assert.assertEquals(results[i], b.toHexString());
			}
		}
		
		[Test]
        public function testRightShift():void {
			var a:Long, b:Long, i:int, results:Array;
			
			results = [
				"8000000000000000",
				"c000000000000000",
				"e000000000000000",
				"f000000000000000",
				"f800000000000000",
				"fc00000000000000",
				"fe00000000000000",
				"ff00000000000000",
				"ff80000000000000",
				"ffc0000000000000",
				"ffe0000000000000",
				"fff0000000000000",
				"fff8000000000000",
				"fffc000000000000",
				"fffe000000000000",
				"ffff000000000000",
				"ffff800000000000",
				"ffffc00000000000",
				"ffffe00000000000",
				"fffff00000000000",
				"fffff80000000000",
				"fffffc0000000000",
				"fffffe0000000000",
				"ffffff0000000000",
				"ffffff8000000000",
				"ffffffc000000000",
				"ffffffe000000000",
				"fffffff000000000",
				"fffffff800000000",
				"fffffffc00000000",
				"fffffffe00000000",
				"ffffffff00000000",
				"ffffffff80000000",
				"ffffffffc0000000",
				"ffffffffe0000000",
				"fffffffff0000000",
				"fffffffff8000000",
				"fffffffffc000000",
				"fffffffffe000000",
				"ffffffffff000000",
				"ffffffffff800000",
				"ffffffffffc00000",
				"ffffffffffe00000",
				"fffffffffff00000",
				"fffffffffff80000",
				"fffffffffffc0000",
				"fffffffffffe0000",
				"ffffffffffff0000",
				"ffffffffffff8000",
				"ffffffffffffc000",
				"ffffffffffffe000",
				"fffffffffffff000",
				"fffffffffffff800",
				"fffffffffffffc00",
				"fffffffffffffe00",
				"ffffffffffffff00",
				"ffffffffffffff80",
				"ffffffffffffffc0",
				"ffffffffffffffe0",
				"fffffffffffffff0",
				"fffffffffffffff8",
				"fffffffffffffffc",
				"fffffffffffffffe",
				"ffffffffffffffff",
				"8000000000000000",
				"c000000000000000"
			];
			
			a = Long.MIN_VALUE;
			for (i = 0; i < 66; i++) {
				b = a.rightShift(i);
				Assert.assertEquals(results[i], b.toHexString());
			}
			
			results = [
				"8000000000000000",
				"4000000000000000",
				"2000000000000000",
				"1000000000000000",
				"800000000000000",
				"400000000000000",
				"200000000000000",
				"100000000000000",
				"80000000000000",
				"40000000000000",
				"20000000000000",
				"10000000000000",
				"8000000000000",
				"4000000000000",
				"2000000000000",
				"1000000000000",
				"800000000000",
				"400000000000",
				"200000000000",
				"100000000000",
				"80000000000",
				"40000000000",
				"20000000000",
				"10000000000",
				"8000000000",
				"4000000000",
				"2000000000",
				"1000000000",
				"800000000",
				"400000000",
				"200000000",
				"100000000",
				"80000000",
				"40000000",
				"20000000",
				"10000000",
				"8000000",
				"4000000",
				"2000000",
				"1000000",
				"800000",
				"400000",
				"200000",
				"100000",
				"80000",
				"40000",
				"20000",
				"10000",
				"8000",
				"4000",
				"2000",
				"1000",
				"800",
				"400",
				"200",
				"100",
				"80",
				"40",
				"20",
				"10",
				"8",
				"4",
				"2",
				"1",
				"8000000000000000",
				"4000000000000000"
			];
			
			a = Long.MIN_VALUE;
			for (i = 0; i < 66; i++) {
				b = a.rightShift(i, true);
				Assert.assertEquals(results[i], b.toHexString());
			}
			
			results = [
				"7fffffffffffffff",
				"3fffffffffffffff",
				"1fffffffffffffff",
				"fffffffffffffff",
				"7ffffffffffffff",
				"3ffffffffffffff",
				"1ffffffffffffff",
				"ffffffffffffff",
				"7fffffffffffff",
				"3fffffffffffff",
				"1fffffffffffff",
				"fffffffffffff",
				"7ffffffffffff",
				"3ffffffffffff",
				"1ffffffffffff",
				"ffffffffffff",
				"7fffffffffff",
				"3fffffffffff",
				"1fffffffffff",
				"fffffffffff",
				"7ffffffffff",
				"3ffffffffff",
				"1ffffffffff",
				"ffffffffff",
				"7fffffffff",
				"3fffffffff",
				"1fffffffff",
				"fffffffff",
				"7ffffffff",
				"3ffffffff",
				"1ffffffff",
				"ffffffff",
				"7fffffff",
				"3fffffff",
				"1fffffff",
				"fffffff",
				"7ffffff",
				"3ffffff",
				"1ffffff",
				"ffffff",
				"7fffff",
				"3fffff",
				"1fffff",
				"fffff",
				"7ffff",
				"3ffff",
				"1ffff",
				"ffff",
				"7fff",
				"3fff",
				"1fff",
				"fff",
				"7ff",
				"3ff",
				"1ff",
				"ff",
				"7f",
				"3f",
				"1f",
				"f",
				"7",
				"3",
				"1",
				"0",
				"7fffffffffffffff",
				"3fffffffffffffff"
			];
			
			a = Long.MAX_VALUE;
			for (i = 0; i < 66; i++) {
				b = a.rightShift(i);
				Assert.assertEquals(results[i], b.toHexString());
			}
			
			a = Long.MAX_VALUE;
			for (i = 0; i < 66; i++) {
				b = a.rightShift(i, true);
				Assert.assertEquals(results[i], b.toHexString());
			}
		}
			
		[Test]
        public function testTestBit():void {
			var a:Long;
			
			a = Long.ZERO;
			Assert.assertFalse(a.testBit(0));
			Assert.assertFalse(a.testBit(1));
			Assert.assertFalse(a.testBit(31));
			Assert.assertFalse(a.testBit(32));
			Assert.assertFalse(a.testBit(63));
			
			a = Long.ONE;
			Assert.assertTrue(a.testBit(0));
			Assert.assertFalse(a.testBit(1));
			Assert.assertFalse(a.testBit(31));
			Assert.assertFalse(a.testBit(32));
			Assert.assertFalse(a.testBit(63));
			
			a = new Long(-1);
			Assert.assertTrue(a.testBit(0));
			Assert.assertTrue(a.testBit(1));
			Assert.assertTrue(a.testBit(31));
			Assert.assertTrue(a.testBit(32));
			Assert.assertTrue(a.testBit(63));
			
			a = Long.MAX_VALUE;
			Assert.assertTrue(a.testBit(0));
			Assert.assertTrue(a.testBit(1));
			Assert.assertTrue(a.testBit(31));
			Assert.assertTrue(a.testBit(32));
			Assert.assertFalse(a.testBit(63));
			
			a = Long.MIN_VALUE;
			Assert.assertFalse(a.testBit(0));
			Assert.assertFalse(a.testBit(1));
			Assert.assertFalse(a.testBit(31));
			Assert.assertFalse(a.testBit(32));
			Assert.assertTrue(a.testBit(63));
			
			a = new Long("100000000", 16);
			Assert.assertFalse(a.testBit(0));
			Assert.assertFalse(a.testBit(1));
			Assert.assertFalse(a.testBit(31));
			Assert.assertTrue(a.testBit(32));
			Assert.assertFalse(a.testBit(63));
			
			a = new Long("80000000", 16);
			Assert.assertFalse(a.testBit(0));
			Assert.assertFalse(a.testBit(1));
			Assert.assertTrue(a.testBit(31));
			Assert.assertFalse(a.testBit(32));
			Assert.assertFalse(a.testBit(63));
		}
			
		[Test]
        public function testSetBit():void {
			var a:Long, b:Long;
			
			a = Long.ZERO;
			b = a.setBit(0);
			Assert.assertEquals("1", b.toHexString());

			a = Long.ZERO;
			b = a.setBit(1);
			Assert.assertEquals("2", b.toHexString());

			a = Long.ZERO;
			b = a.setBit(31);
			Assert.assertEquals("80000000", b.toHexString());

			a = Long.ZERO;
			b = a.setBit(32);
			Assert.assertEquals("100000000", b.toHexString());

			a = Long.ZERO;
			b = a.setBit(63);
			Assert.assertEquals("8000000000000000", b.toHexString());
		}
			
		[Test]
        public function testClearBit():void {
			var a:Long, b:Long;
			
			a = Long.ONE;
			b = a.clearBit(0);
			Assert.assertEquals("0", b.toHexString());

			a = Long.TEN;
			b = a.clearBit(1);
			Assert.assertEquals("8", b.toHexString());

			a = Long.TEN;
			b = a.clearBit(3);
			Assert.assertEquals("2", b.toHexString());

			a = new Long("80000000", 16);
			b = a.clearBit(31);
			Assert.assertEquals("0", b.toHexString());

			a = new Long("100000000", 16);
			b = a.clearBit(32);
			Assert.assertEquals("0", b.toHexString());

			a = Long.MIN_VALUE;
			b = a.clearBit(63);
			Assert.assertEquals("0", b.toHexString());
		}
			
		[Test]
        public function testAdd():void {
			var a:Long, b:Long, c:Long;
			
			a = new Long(0);
			b = new Long(0);
			c = a.add(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(0);
			b = new Long(1);
			c = a.add(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = new Long(1);
			b = new Long(1);
			c = a.add(b);
			Assert.assertEquals("2", c.toHexString());
			
			a = new Long(int.MAX_VALUE);
			b = new Long(int.MAX_VALUE);
			c = a.add(b);
			Assert.assertEquals("fffffffe", c.toHexString());
			
			a = new Long("7fffffff", 16);
			b = new Long("80000000", 16);
			c = a.add(b);
			Assert.assertEquals("ffffffff", c.toHexString());
			
			a = new Long("7fffffff", 16);
			b = new Long("80000001", 16);
			c = a.add(b);
			Assert.assertEquals("100000000", c.toHexString());
			
			a = new Long("80000001", 16);
			b = new Long("7fffffff", 16);
			c = a.add(b);
			Assert.assertEquals("100000000", c.toHexString());
			
			a = new Long("ffffffff", 16);
			b = new Long("1", 16);
			c = a.add(b);
			Assert.assertEquals("100000000", c.toHexString());
			
			a = new Long("1", 16);
			b = new Long("ffffffff", 16);
			c = a.add(b);
			Assert.assertEquals("100000000", c.toHexString());
			
			a = new Long("ffffffff", 16);
			b = new Long("ffffffff", 16);
			c = a.add(b);
			Assert.assertEquals("1fffffffe", c.toHexString());
			
			a = new Long("7fffffffffffffff", 16);
			b = new Long("-8000000000000000", 16);
			c = a.add(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());

			a = Long.MAX_VALUE;
			b = Long.MIN_VALUE;
			c = a.add(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());

			a = new Long("7fffffffffffffff", 16);
			b = new Long("7fffffffffffffff", 16);
			c = a.add(b);
			Assert.assertEquals("fffffffffffffffe", c.toHexString());

			a = Long.MAX_VALUE;
			b = Long.MAX_VALUE;
			c = a.add(b);
			Assert.assertEquals("fffffffffffffffe", c.toHexString());

			a = Long.MAX_VALUE;
			b = Long.ONE;
			c = a.add(b);
			Assert.assertEquals("8000000000000000", c.toHexString());
			
			a = new Long(0);
			b = new Long(-1);
			c = a.add(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long(-1);
			b = new Long(-1);
			c = a.add(b);
			Assert.assertEquals("fffffffffffffffe", c.toHexString());
			
			a = new Long(int.MIN_VALUE);
			b = new Long(int.MIN_VALUE);
			c = a.add(b);
			Assert.assertEquals("ffffffff00000000", c.toHexString());
			
			a = Long.MIN_VALUE;
			b = Long.MIN_VALUE;
			c = a.add(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = Long.MIN_VALUE;
			b = new Long(-1);
			c = a.add(b);
			Assert.assertEquals("7fffffffffffffff", c.toHexString());
		}
		
		[Test]
        public function testSubtract():void {
			var a:Long, b:Long, c:Long;
			
			a = new Long(0);
			b = new Long(0);
			c = a.subtract(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(0);
			b = new Long(1);
			c = a.subtract(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long(0);
			b = new Long(2);
			c = a.subtract(b);
			Assert.assertEquals("fffffffffffffffe", c.toHexString());
			
			a = new Long(1);
			b = new Long(2);
			c = a.subtract(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long(1);
			b = new Long(0);
			c = a.subtract(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = new Long(1);
			b = new Long(1);
			c = a.subtract(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(2);
			b = new Long(1);
			c = a.subtract(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = Long.ZERO;
			b = Long.MAX_VALUE;
			c = a.subtract(b);
			Assert.assertEquals("8000000000000001", c.toHexString());
			
			a = Long.ZERO;
			b = Long.MIN_VALUE;
			c = a.subtract(b);
			Assert.assertEquals("8000000000000000", c.toHexString());
			
			a = Long.MIN_VALUE;
			b = Long.MAX_VALUE;
			c = a.subtract(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = Long.MAX_VALUE;
			b = Long.MIN_VALUE;
			c = a.subtract(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long("fffffffe", 16);
			b = new Long("ffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long("2fffffffe", 16);
			b = new Long("1ffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("ffffffff", c.toHexString());
			
			a = new Long("2fffffffe", 16);
			b = new Long("2ffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long("2ffffffff", 16);
			b = new Long("2ffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long("3ffffffff", 16);
			b = new Long("2ffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("100000000", c.toHexString());
			
			a = new Long("3ffffffff", 16);
			b = new Long("2fffffffe", 16);
			c = a.subtract(b);
			Assert.assertEquals("100000001", c.toHexString());
			
			a = new Long("7ffffffefffffffe", 16);
			b = new Long("7fffffffffffffff", 16);
			c = a.subtract(b);
			Assert.assertEquals("fffffffeffffffff", c.toHexString());
		}
		
		[Test]
        public function testMultiply():void {
			var a:Long, b:Long, c:Long;
			
			a = new Long(0);
			b = new Long(0);
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(0);
			b = new Long(1);
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(1);
			b = new Long(0);
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(1);
			b = new Long(1);
			c = a.multiply(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = new Long(-1);
			b = new Long(0);
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(0);
			b = new Long(-1);
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
			
			a = new Long(-1);
			b = new Long(1);
			c = a.multiply(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long(1);
			b = new Long(-1);
			c = a.multiply(b);
			Assert.assertEquals("ffffffffffffffff", c.toHexString());
			
			a = new Long(-1);
			b = new Long(-1);
			c = a.multiply(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = new Long("ffff", 16);
			b = new Long("ffff", 16);
			c = a.multiply(b);
			Assert.assertEquals("fffe0001", c.toHexString());
			
			a = new Long("1ffff", 16);
			b = new Long("1ffff", 16);
			c = a.multiply(b);
			Assert.assertEquals("3fffc0001", c.toHexString());
			
			a = new Long("ffff", 16);
			b = new Long("-ffff", 16);
			c = a.multiply(b);
			Assert.assertEquals("ffffffff0001ffff", c.toHexString());
			
			a = new Long("1ffff", 16);
			b = new Long("-1ffff", 16);
			c = a.multiply(b);
			Assert.assertEquals("fffffffc0003ffff", c.toHexString());
			
			a = new Long(int.MAX_VALUE);
			b = new Long(int.MAX_VALUE);
			c = a.multiply(b);
			Assert.assertEquals("3fffffff00000001", c.toHexString());
			
			a = new Long(int.MIN_VALUE);
			b = new Long(int.MAX_VALUE);
			c = a.multiply(b);
			Assert.assertEquals("c000000080000000", c.toHexString());
			
			a = new Long(int.MAX_VALUE);
			b = new Long(int.MIN_VALUE);
			c = a.multiply(b);
			Assert.assertEquals("c000000080000000", c.toHexString());
			
			a = new Long(int.MIN_VALUE);
			b = new Long(int.MIN_VALUE);
			c = a.multiply(b);
			Assert.assertEquals("4000000000000000", c.toHexString());
			
			a = Long.MAX_VALUE;
			b = Long.MAX_VALUE;
			c = a.multiply(b);
			Assert.assertEquals("1", c.toHexString());
			
			a = Long.MIN_VALUE;
			b = Long.MAX_VALUE;
			c = a.multiply(b);
			Assert.assertEquals("8000000000000000", c.toHexString());
			
			a = Long.MAX_VALUE;
			b = Long.MIN_VALUE;
			c = a.multiply(b);
			Assert.assertEquals("8000000000000000", c.toHexString());
			
			a = Long.MIN_VALUE;
			b = Long.MIN_VALUE;
			c = a.multiply(b);
			Assert.assertEquals("0", c.toHexString());
		}
		
		[Test]
        public function testDivideAndRemainder():void {
			var a:Long, b:Long, res:Array, q:Long, r:Long;
			
			try {
				a = Long.ZERO;
				b = Long.ZERO;
				a.divideAndRemainder(b);
				Assert.fail("Should throw a ArithmeticError");
			}
			catch (e:ArithmeticError) {
				Assert.assertEquals("Cannot divide by zero", e.message);
			}
			
			try {
				a = Long.ONE;
				b = Long.ZERO;
				a.divideAndRemainder(b);
				Assert.fail("Should throw a ArithmeticError");
			}
			catch (e:ArithmeticError) {
				Assert.assertEquals("Cannot divide by zero", e.message);
			}
			
			a = Long.ZERO;
			b = Long.ONE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.ZERO;
			b = Long.ONE.negate();
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.ONE;
			b = Long.ONE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("1", q.toHexString());
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.ONE;
			b = Long.ONE.negate();
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("ffffffffffffffff", q.toHexString()); // -1.
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.TEN;
			b = new Long(2);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("5", q.toHexString());
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.TEN;
			b = new Long(-2);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("fffffffffffffffb", q.toHexString()); // -5.
			Assert.assertEquals("0", r.toHexString());
			
			a = Long.TEN;
			b = new Long(3);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("3", q.toHexString());
			Assert.assertEquals("1", r.toHexString());
			
			a = Long.TEN;
			b = new Long(-3);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("fffffffffffffffd", q.toHexString()); // -3.
			Assert.assertEquals("1", r.toHexString());
			
			a = Long.TEN.negate();
			b = new Long(3);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("fffffffffffffffd", q.toHexString()); // -3.
			Assert.assertEquals("ffffffffffffffff", r.toHexString()); // -1.
			
			a = Long.TEN.negate();
			b = new Long(-3);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("3", q.toHexString());
			Assert.assertEquals("ffffffffffffffff", r.toHexString()); // -1.
			
			a = new Long(3);
			b = Long.TEN;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("3", r.toHexString());
			
			a = new Long(-3);
			b = Long.TEN;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("fffffffffffffffd", r.toHexString()); // -3.
			
			a = new Long(3);
			b = Long.TEN.negate();
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("3", r.toHexString());
			
			a = new Long(-3);
			b = Long.TEN.negate();
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("fffffffffffffffd", r.toHexString()); // -3.
			
			a = Long.MAX_VALUE;
			b = new Long(int.MAX_VALUE);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("100000002", q.toHexString());
			Assert.assertEquals("1", r.toHexString());
			
			a = Long.MAX_VALUE;
			b = new Long("ffffffff", 16);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("80000000", q.toHexString());
			Assert.assertEquals("7fffffff", r.toHexString());
			
			a = Long.MAX_VALUE;
			b = new Long("100000000", 16);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("7fffffff", q.toHexString());
			Assert.assertEquals("ffffffff", r.toHexString());
			
			a = Long.MAX_VALUE;
			b = Long.MIN_VALUE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("0", q.toHexString());
			Assert.assertEquals("7fffffffffffffff", r.toHexString()); // Long.MAX_VALUE.
			
			a = Long.MIN_VALUE;
			b = new Long(-1);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("8000000000000000", q.toHexString()); // Long.MIN_VALUE.
			Assert.assertEquals("0", r.toHexString());

			a = Long.MIN_VALUE;
			b = Long.MAX_VALUE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("ffffffffffffffff", q.toHexString()); // -1.
			Assert.assertEquals("ffffffffffffffff", r.toHexString()); // -1.

			a = Long.MAX_VALUE;
			b = Long.MAX_VALUE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("1", q.toHexString());
			Assert.assertEquals("0", r.toHexString());

			a = Long.MIN_VALUE;
			b = Long.MIN_VALUE;
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("1", q.toHexString());
			Assert.assertEquals("0", r.toHexString());

			a = new Long("100000000", 16);
			b = new Long("-100000000", 16);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("ffffffffffffffff", q.toHexString()); // -1.
			Assert.assertEquals("0", r.toHexString());

			a = new Long("-100000000", 16);
			b = new Long("100000000", 16);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("ffffffffffffffff", q.toHexString()); // -1.
			Assert.assertEquals("0", r.toHexString());

			a = new Long("-100000000", 16);
			b = new Long("-100000000", 16);
			res = a.divideAndRemainder(b);
			q = (res[0] as Long);
			r = (res[1] as Long);
			Assert.assertEquals("1", q.toHexString());
			Assert.assertEquals("0", r.toHexString());
		}
		
		[Test]
        public function testCompareTo():void {
			var a:Long, b:Long;
			
			a = Long.ZERO;
			b = Long.ZERO;
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = Long.ONE;
			b = Long.ONE;
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = Long.TEN;
			b = Long.TEN;
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = new Long(0);
			b = new Long(0);
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = new Long(0);
			b = new Long(1);
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
			
			a = new Long(0);
			b = new Long(-1);
			Assert.assertEquals(1, a.compareTo(b));
			Assert.assertEquals(-1, b.compareTo(a));
			
			a = new Long(1);
			b = new Long(1);
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = new Long(-1);
			b = new Long(-1);
			Assert.assertEquals(0, a.compareTo(b));
			Assert.assertEquals(0, b.compareTo(a));
			
			a = new Long(-1);
			b = new Long(1);
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
			
			a = new Long(-1);
			b = Long.MIN_VALUE;
			Assert.assertEquals(1, a.compareTo(b));
			Assert.assertEquals(-1, b.compareTo(a));
			
			a = new Long(int.MIN_VALUE);
			b = Long.MIN_VALUE;
			Assert.assertEquals(1, a.compareTo(b));
			Assert.assertEquals(-1, b.compareTo(a));
			
			a = new Long("-100000000");
			b = Long.MIN_VALUE;
			Assert.assertEquals(1, a.compareTo(b));
			Assert.assertEquals(-1, b.compareTo(a));
			
			a = new Long(1);
			b = Long.MAX_VALUE;
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
			
			a = new Long(int.MAX_VALUE);
			b = Long.MAX_VALUE;
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
			
			a = new Long("100000000");
			b = Long.MAX_VALUE;
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
			
			a = Long.MIN_VALUE;
			b = Long.MAX_VALUE;
			Assert.assertEquals(-1, a.compareTo(b));
			Assert.assertEquals(1, b.compareTo(a));
		}
		
		[Test]
        public function testEquals():void {
			var a:Long, b:Long;
			
			a = Long.ZERO;
			b = Long.ZERO;
			Assert.assertTrue(a.equals(b));
			Assert.assertTrue(b.equals(a));
			
			a = Long.ONE;
			b = Long.ONE;
			Assert.assertTrue(a.equals(b));
			Assert.assertTrue(b.equals(a));
			
			a = Long.TEN;
			b = Long.TEN;
			Assert.assertTrue(a.equals(b));
			Assert.assertTrue(b.equals(a));
			
			a = Long.ZERO;
			b = Long.ONE;
			Assert.assertFalse(a.equals(b));
			Assert.assertFalse(b.equals(a));
			
			a = Long.ONE;
			b = Long.TEN;
			Assert.assertFalse(a.equals(b));
			Assert.assertFalse(b.equals(a));
			
			a = Long.ZERO;
			b = Long.TEN;
			Assert.assertFalse(a.equals(b));
			Assert.assertFalse(b.equals(a));
			
			a = Long.ZERO;
			Assert.assertTrue(a.equals(0));
			
			a = Long.ONE;
			Assert.assertTrue(a.equals(1));
			
			a = Long.TEN;
			Assert.assertTrue(a.equals(10));
			
			a = new Long(int.MIN_VALUE);
			Assert.assertTrue(a.equals(int.MIN_VALUE));
			
			a = new Long(int.MAX_VALUE);
			Assert.assertTrue(a.equals(int.MAX_VALUE));
		}
		
		[Test]
        public function testToInt():void {
			var a:Long;
			
			a = new Long(0);
			Assert.assertEquals(0, a.toInt());
			
			a = new Long(1);
			Assert.assertEquals(1, a.toInt());
			
			a = new Long(-1);
			Assert.assertEquals(-1, a.toInt());

			a = Long.MAX_VALUE;
			Assert.assertEquals(-1, a.toInt());

			a = Long.MIN_VALUE;
			Assert.assertEquals(0, a.toInt());

			a = new Long("100000000", 16);
			Assert.assertEquals(0, a.toInt());

			a = new Long("-100000000", 16);
			Assert.assertEquals(0, a.toInt());

			a = new Long("ffffffff", 16);
			Assert.assertEquals(-1, a.toInt());

			a = new Long("-ffffffff", 16);
			Assert.assertEquals(1, a.toInt());
		}
		
		[Test]
        public function testToNumber():void {
			var a:Long;
			
			a = new Long(0);
			Assert.assertEquals(0, a.toNumber());
			
			a = new Long(1);
			Assert.assertEquals(1, a.toNumber());
			
			a = new Long(-1);
			Assert.assertEquals(-1, a.toNumber());

			a = Long.MAX_VALUE;
			Assert.assertEquals(Number("9.223372036854776E18"), a.toNumber());

			a = Long.MIN_VALUE;
			Assert.assertEquals(Number("-9.223372036854776E18"), a.toNumber());

			a = new Long("100000000", 16);
			Assert.assertEquals(Number("4.294967296E9"), a.toNumber());

			a = new Long("-100000000", 16);
			Assert.assertEquals(Number("-4.294967296E9"), a.toNumber());

			a = new Long("ffffffff", 16);
			Assert.assertEquals(Number("4.294967295E9"), a.toNumber());

			a = new Long("-ffffffff", 16);
			Assert.assertEquals(Number("-4.294967295E9"), a.toNumber());
		}
		
		[Test]
        public function testToString():void {
			var a:Long;
			
			a = new Long(0);
			Assert.assertEquals("0", a.toString());
			
			a = new Long(1);
			Assert.assertEquals("1", a.toString());
			
			a = new Long(-1);
			Assert.assertEquals("-1", a.toString());

			a = Long.MAX_VALUE;
			Assert.assertEquals(Number("9223372036854775807"), a.toString());

			a = Long.MIN_VALUE;
			Assert.assertEquals(Number("-9223372036854775808"), a.toString());

			a = new Long("100000000", 16);
			Assert.assertEquals(Number("4294967296"), a.toString());

			a = new Long("-100000000", 16);
			Assert.assertEquals(Number("-4294967296"), a.toString());

			a = new Long("ffffffff", 16);
			Assert.assertEquals(Number("4294967295"), a.toString());

			a = new Long("-ffffffff", 16);
			Assert.assertEquals(Number("-4294967295"), a.toString());
		}
	}
}