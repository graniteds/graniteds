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
	
	import org.granite.math.BigDecimal;
	import org.granite.math.BigNumberError;
	import org.granite.math.MathContext;
	import org.granite.math.NumberFormatError;
	import org.granite.math.RoundingMode;

	public class TestBigDecimal {

		/**
		 * @author Franck WOLFF
		 * @private
		 */
		[Test]
        public function testConstants():void {
			var a:BigDecimal;

			a = BigDecimal.ZERO;
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);

			a = BigDecimal.ONE;
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);

			a = BigDecimal.TEN;
			Assert.assertEquals("10", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(2, a.precision);
		}
		
		[Test]
        public function testForString():void {
			var a:BigDecimal;
			
			a = new BigDecimal("1.");
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(".1");
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("+1.");
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("+.1");
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("-1.");
			Assert.assertEquals("-1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("-.1");
			Assert.assertEquals("-1", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("0");
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);

			a = new BigDecimal("0.00");
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(2, a.scale);
			Assert.assertEquals(1, a.precision);

			a = new BigDecimal("123");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("-123");
			Assert.assertEquals("-123", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("1.23E3");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(-1, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("1.23E+3");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(-1, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("12.3E+7");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(-6, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("12.0");
			Assert.assertEquals("120", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("12.3");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("0.00123");
			Assert.assertEquals("123", a.unscaledValue.toString());
			Assert.assertEquals(5, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("-1.23E-12");
			Assert.assertEquals("-123", a.unscaledValue.toString());
			Assert.assertEquals(14, a.scale);
			Assert.assertEquals(3, a.precision);
			
			a = new BigDecimal("1234.5E-4");
			Assert.assertEquals("12345", a.unscaledValue.toString());
			Assert.assertEquals(5, a.scale);
			Assert.assertEquals(5, a.precision);
			
			a = new BigDecimal("0E+7");
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(-7, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal("-0");
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			try {
				a = new BigDecimal("");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Zero length BigDecimal", e.message);
			}
			
			try {
				a = new BigDecimal("E1");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal BigDecimal format: E1", e.message);
			}
			
			try {
				a = new BigDecimal("0E1.0");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal BigDecimal format: 0E1.0", e.message);
			}
			
			try {
				a = new BigDecimal("0E");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Zero length BigDecimal exponent: 0E", e.message);
			}
			
			try {
				a = new BigDecimal("0EE");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Invalid exponent digit: 0EE", e.message);
			}
			
			try {
				a = new BigDecimal("0.0E1.0");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Invalid exponent digit: 0.0E1.0", e.message);
			}
			
			try {
				a = new BigDecimal("1.0E-2147483647");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Scale out of range: 1.0E-2147483647", e.message);
			}
			
			try {
				a = new BigDecimal("0E2147483648");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Exponent out of range: 0E2147483648", e.message);
			}
			
			try {
				a = new BigDecimal("0E-2147483648");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Exponent out of range: 0E-2147483648", e.message);
			}
			
			try {
				a = new BigDecimal("1.0a456");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal BigDecimal format: 1.0a456", e.message);
			}
			
			try {
				a = new BigDecimal("a1.0456");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal BigDecimal format: a1.0456", e.message);
			}
			
			try {
				a = new BigDecimal("1.0456a");
				Assert.fail("Should throw a NumberFormatError");
			}
			catch (e:NumberFormatError) {
				Assert.assertEquals("Illegal BigDecimal format: 1.0456a", e.message);
			}
		}
		
		[Test]
        public function testForInt():void {
			var a:BigDecimal;
			
			a = new BigDecimal(int(0));
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(int(1));
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(int(-1));
			Assert.assertEquals("-1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(int.MIN_VALUE);
			Assert.assertEquals("-2147483648", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);
			
			a = new BigDecimal(int.MAX_VALUE);
			Assert.assertEquals("2147483647", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);
		}
		
		[Test]
        public function testForNumber():void {
			var a:BigDecimal;
			
			a = new BigDecimal(Number(0));
			Assert.assertEquals("0", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(Number(1));
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(Number(-1));
			Assert.assertEquals("-1", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(Number(int.MIN_VALUE));
			Assert.assertEquals("-2147483648", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);
			
			a = new BigDecimal(Number(int.MAX_VALUE));
			Assert.assertEquals("2147483647", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);
			
			a = new BigDecimal(Number(uint.MAX_VALUE));
			Assert.assertEquals("4294967295", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);

			a = new BigDecimal(-Number(uint.MAX_VALUE));
			Assert.assertEquals("-4294967295", a.unscaledValue.toString());
			Assert.assertEquals(0, a.scale);
			Assert.assertEquals(10, a.precision);
			
			// Number.MAX_VALUE.toString() --> "1.79769313486231e+308"
			// scale = 14 - 308 = -294.
			// precision = 1 + 14 = 15.
			const UNSCALED_MAX:String = "179769313486231";
			
			a = new BigDecimal(Number.MAX_VALUE);
			Assert.assertEquals(UNSCALED_MAX, a.unscaledValue.toString());
			Assert.assertEquals(-294, a.scale);
			Assert.assertEquals(15, a.precision);
			
			a = new BigDecimal(-Number.MAX_VALUE);
			Assert.assertEquals('-' + UNSCALED_MAX, a.unscaledValue.toString());
			Assert.assertEquals(-294, a.scale);
			Assert.assertEquals(15, a.precision);
			
			a = new BigDecimal(0.1);
			Assert.assertEquals("1", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(1, a.precision);
			
			a = new BigDecimal(1.1);
			Assert.assertEquals("11", a.unscaledValue.toString());
			Assert.assertEquals(1, a.scale);
			Assert.assertEquals(2, a.precision);
			
			a = new BigDecimal(0.12345);
			Assert.assertEquals("12345", a.unscaledValue.toString());
			Assert.assertEquals(5, a.scale);
			Assert.assertEquals(5, a.precision);
			
			a = new BigDecimal(1.12345);
			Assert.assertEquals("112345", a.unscaledValue.toString());
			Assert.assertEquals(5, a.scale);
			Assert.assertEquals(6, a.precision);
		}
		
		[Test]
        public function testAdd():void {
			var a:BigDecimal, b:BigDecimal, c:BigDecimal, i:int, s:String, results:Array;
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ZERO;
			c = a.add(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ZERO;
			c = a.add(b);
			Assert.assertEquals("1", c.toString());
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ONE;
			c = a.add(b);
			Assert.assertEquals("1", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ONE;
			c = a.add(b);
			Assert.assertEquals("2", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.1");
			c = a.add(b);
			Assert.assertEquals("0.2", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("0.1");
			c = a.add(b);
			Assert.assertEquals("0.0", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("-0.1");
			c = a.add(b);
			Assert.assertEquals("0.0", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("-0.1");
			c = a.add(b);
			Assert.assertEquals("-0.2", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.1");
			c = a.add(b);
			Assert.assertEquals("0.11", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.10");
			c = a.add(b);
			Assert.assertEquals("0.11", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.01");
			c = a.add(b);
			Assert.assertEquals("0.11", c.toString());
			
			a = new BigDecimal("0.10");
			b = new BigDecimal("0.01");
			c = a.add(b);
			Assert.assertEquals("0.11", c.toString());
			
			// Trusted java.math.BigDecimal results.
			results = [
				"4562.2",
				"45642.24",
				"456642.246",
				"4568642.2468",
				"45708642.24690",
				"457308642.246912",
				"4575308642.2469134",
				"45775308642.24691356",
				"457975308642.246913578",
				"45621975308642.24691357820",
				"4562221975308642.2469135782022",
				"456242221975308642.246913578202224",
				"45626242221975308642.24691357820222426",
				"4562826242221975308642.2469135782022242628",
				"456302826242221975308642.246913578202224262830",
				"45632302826242221975308642.24691357820222426283032",
				"4563432302826242221975308642.2469135782022242628303234",
				"456363432302826242221975308642.246913578202224262830323436",
				"45638363432302826242221975308642.24691357820222426283032343638",
				"4564038363432302826242221975308642.2469135782022242628303234363840",
				"456424038363432302826242221975308642.246913578202224262830323436384042",
				"45644424038363432302826242221975308642.24691357820222426283032343638404244",
				"4564644424038363432302826242221975308642.2469135782022242628303234363840424446",
				"456484644424038363432302826242221975308642.246913578202224262830323436384042444648",
				"45650484644424038363432302826242221975308642.24691357820222426283032343638404244464850",
				"4565250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052",
				"456545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254",
				"45656545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456",
				"4565856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658",
				"456605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860",
				"45662605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062",
				"4566462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264",
				"456666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466",
				"45668666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668",
				"4567068666462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264666870",
				"456727068666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466687072",
				"45674727068666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668707274",
				"4567674727068666462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264666870727476",
				"456787674727068666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466687072747678",
				"45680787674727068666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668707274767880",
				"4568280787674727068666462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264666870727476788082",
				"456848280787674727068666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466687072747678808284",
				"45686848280787674727068666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668707274767880828486",
				"4568886848280787674727068666462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264666870727476788082848688",
				"456908886848280787674727068666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466687072747678808284868890",
				"45692908886848280787674727068666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668707274767880828486889092",
				"4569492908886848280787674727068666462605856545250484644424038363432302826242221975308642.2469135782022242628303234363840424446485052545658606264666870727476788082848688909294",
				"456969492908886848280787674727068666462605856545250484644424038363432302826242221975308642.246913578202224262830323436384042444648505254565860626466687072747678808284868890929496",
				"45698969492908886848280787674727068666462605856545250484644424038363432302826242221975308642.24691357820222426283032343638404244464850525456586062646668707274767880828486889092949698"
			];
			
			s = ".";
			for (i = 1; i < 50; i++) {
				s = i + s + i;
				a = new BigDecimal(s);
				b = new BigDecimal("456" + s);
				c = a.add(b);
				Assert.assertEquals(results[i-1], c.toString());
			}
		}
		
		[Test]
        public function testSubtract():void {
			var a:BigDecimal, b:BigDecimal, c:BigDecimal, i:int, s:String, results:Array;
			
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ZERO;
			c = a.subtract(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ZERO;
			c = a.subtract(b);
			Assert.assertEquals("1", c.toString());
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ONE;
			c = a.subtract(b);
			Assert.assertEquals("-1", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ONE;
			c = a.subtract(b);
			Assert.assertEquals("0", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.1");
			c = a.subtract(b);
			Assert.assertEquals("0.0", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("0.1");
			c = a.subtract(b);
			Assert.assertEquals("-0.2", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("-0.1");
			c = a.subtract(b);
			Assert.assertEquals("0.2", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("-0.1");
			c = a.subtract(b);
			Assert.assertEquals("0.0", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.1");
			c = a.subtract(b);
			Assert.assertEquals("-0.09", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.10");
			c = a.subtract(b);
			Assert.assertEquals("-0.09", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.01");
			c = a.subtract(b);
			Assert.assertEquals("0.09", c.toString());
			
			a = new BigDecimal("0.10");
			b = new BigDecimal("0.01");
			c = a.subtract(b);
			Assert.assertEquals("0.09", c.toString());
			
			results = [
				"-4560.0123",
				"-45600.00123",
				"-456000.000123",
				"-4560000.0000123",
				"-45600000.00000123",
				"-456000000.000000123",
				"-4560000000.0000000123",
				"-45600000000.00000000123",
				"-456000000000.000000000123",
				"-45600000000000.00000000000123",
				"-4560000000000000.0000000000000123",
				"-456000000000000000.000000000000000123",
				"-45600000000000000000.00000000000000000123",
				"-4560000000000000000000.0000000000000000000123",
				"-456000000000000000000000.000000000000000000000123",
				"-45600000000000000000000000.00000000000000000000000123",
				"-4560000000000000000000000000.0000000000000000000000000123",
				"-456000000000000000000000000000.000000000000000000000000000123",
				"-45600000000000000000000000000000.00000000000000000000000000000123",
				"-4560000000000000000000000000000000.0000000000000000000000000000000123",
				"-456000000000000000000000000000000000.000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000.00000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000.0000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000.000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000.00000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-4560000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0000000000000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-456000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123",
				"-45600000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000123"
			];
			
			s = ".";
			for (i = 1; i < 50; i++) {
				s = i + s + i;
				a = new BigDecimal(s);
				b = new BigDecimal("456" + s + "123");
				c = a.subtract(b);
				Assert.assertEquals(results[i-1], c.toString());
			}
		}
		
		[Test]
        public function testMultiply():void {
			var a:BigDecimal, b:BigDecimal, c:BigDecimal, i:int, s:String, results:Array;

			a = BigDecimal.ZERO;
			b = BigDecimal.ZERO;
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ZERO;
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ONE;
			c = a.multiply(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ONE;
			c = a.multiply(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.1");
			c = a.multiply(b);
			Assert.assertEquals("0.01", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("0.1");
			c = a.multiply(b);
			Assert.assertEquals("-0.01", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("-0.1");
			c = a.multiply(b);
			Assert.assertEquals("-0.01", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("-0.1");
			c = a.multiply(b);
			Assert.assertEquals("0.01", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.1");
			c = a.multiply(b);
			Assert.assertEquals("0.001", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.10");
			c = a.multiply(b);
			Assert.assertEquals("0.0010", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.01");
			c = a.multiply(b);
			Assert.assertEquals("0.001", c.toString());
			
			a = new BigDecimal("0.10");
			b = new BigDecimal("0.01");
			c = a.multiply(b);
			Assert.assertEquals("0.0010", c.toString());
			
			a = new BigDecimal("0.001");
			b = new BigDecimal("0.001");
			c = a.multiply(b);
			Assert.assertEquals("0.000001", c.toString());
			
			results = [
				"5017.22353",
				"963518.0803776",
				"146535208.020627129",
				"19722994811.49117737782",
				"2479994013772.9369548843435",
				"298798568428536.802474882121088",
				"34962292954823547.53219110659340741",
				"4004720323241243249.5705491515098202394",
				"451345831490330132087.361993598248375706047",
				"501157765590710132555045.4515577957898637306605930",
				"5067337997272376915066209123.89588096862950330226452715353",
				"55241278241701037419877567399446.803776175666541944894144695980776",
				"598494824590587386183963041264634418.2165073474899987537895766534275048199",
				"6445829177378022686765062970768808354442.17448888140576418610322233082219454517622",
				"69067309159854940985711075432879305202599922.718124938966897149068605701174343666694389045",
				"736765306468469833212596784359337311146851411263.8878195619647282317846820040136471801521994662468",
				"7828595619505023689857904568337441138315534158828869.72397679081892874014270699173339803550920385355337891",
				"82895585804067622967912080188146114757020776507528893144.267000665933712967201595883857509122264998160256776415314",
				"875054194738951705659612109617188170759923469098107365644491.5572952277131232316919378592012193689145783459521136257894737",
				"9211545720453283998133546229394181415326634388523684374073123315.63526451656119995855903880981888116273889437023535003023799776160",
				"96725698995980630816903344427710937026600049862916224939348055370020.541312572881983552069931794633700027590715732702445224605919402059583",
				"1013361448482385430686933666654551173863791985950917910613267069716425010.3158434370795144162674629442349346387083697283493588048528329823064745006",
				"10594679476130657673666581234219864428330765021933876639775729071579460328688.99926114955783295519206247792847784904423762612605036693833446174734787832429",
				"110557648735184731820820284273711760605230443357152508471068915612756917691121460.631969750720979572884134715487388589789843218682019506822019799978140654571321852"
			];
			
			s = ".";
			for (i = 1; i < 25; i++) {
				s = i + s + i;
				a = new BigDecimal(s);
				b = new BigDecimal("456" + s + "123");
				c = a.multiply(b);
				Assert.assertEquals(results[i-1], c.toString());
			}
		}
		
		[Test]
        public function testDivide():void {
			var a:BigDecimal, b:BigDecimal, c:BigDecimal, i:int, s:String, results:Array;
			
			a = BigDecimal.ZERO;
			b = BigDecimal.ONE;
			c = a.divide(b);
			Assert.assertEquals("0", c.toString());
			
			a = BigDecimal.ONE;
			b = BigDecimal.ONE;
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.1");
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("0.1");
			c = a.divide(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("-0.1");
			c = a.divide(b);
			Assert.assertEquals("-1", c.toString());
			
			a = new BigDecimal("-0.1");
			b = new BigDecimal("-0.1");
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.1");
			c = a.divide(b);
			Assert.assertEquals("0.1", c.toString());
			
			a = new BigDecimal("0.01");
			b = new BigDecimal("0.10");
			c = a.divide(b);
			Assert.assertEquals("0.1", c.toString());
			
			a = new BigDecimal("0.1");
			b = new BigDecimal("0.01");
			c = a.divide(b);
			Assert.assertEquals("1E+1", c.toString());
			
			a = new BigDecimal("0.10");
			b = new BigDecimal("0.01");
			c = a.divide(b);
			Assert.assertEquals("10", c.toString());
			
			a = new BigDecimal("0.001");
			b = new BigDecimal("0.001");
			c = a.divide(b);
			Assert.assertEquals("1", c.toString());
			
			results = [
				"4146.46572727272727272727",
				"2160.09096732954545454545",
				"1421.01662952513522855728",
				"1056.28113360805664563988",
				"840.45244692838233263748",
				"697.90551573743280762728",
				"596.74192491426318686364",
				"521.22535130669317779025",
				"462.69999993651625053403",
				"4151.11235949926827459500",
				"4105.45605067179521039873",
				"3766.14145274357655649438",
				"3476.31547007758175266260",
				"3227.89963694119510375392",
				"3012.62803232717506037185",
				"2824.28237523673520750421",
				"2658.10820451842905136854",
				"2510.40819764177427172504",
				"2378.26382978723404244204",
				"2259.34057604850934815463",
				"2151.74879692011549566890",
				"2053.94258153422140560404",
				"1964.64499121265377855887",
				"1882.79199999999999999999"
			];
			
			s = ".";
			for (i = 1; i < 25; i++) {
				s = i + s + i;
				a = new BigDecimal("456" + s + "123");
				b = new BigDecimal(s);
				c = a.divide(b, 20, RoundingMode.FLOOR);
				Assert.assertEquals(results[i-1], c.toString());
			}
			
			results = [
				"4146.4657",
				"2160.09096",
				"1421.016629",
				"1056.2811336",
				"840.45244692",
				"697.905515737",
				"596.7419249142",
				"521.22535130669",
				"462.699999936516",
				"4151.11235949926827",
				"4105.4560506717952103",
				"3766.141452743576556494",
				"3476.31547007758175266260",
				"3227.8996369411951037539271",
				"3012.628032327175060371856909",
				"2824.28237523673520750421927548",
				"2658.1082045184290513685421371223",
				"2510.408197641774271725043200105997",
				"2378.26382978723404244204176028194720",
				"2259.3405760485093481546310857167017560",
				"2151.748796920115495668903318054895192270",
				"2053.94258153422140560404217709983787589217",
				"1964.6449912126537785588752189252882480064658",
				"1882.791999999999999999999999993035388765261193"
			];
			
			s = ".";
			for (i = 1; i < 25; i++) {
				s = i + s + i;
				a = new BigDecimal("456" + s + "123");
				b = new BigDecimal(s);
				c = a.divide(b, RoundingMode.FLOOR);
				Assert.assertEquals(results[i-1], c.toString());
			}

			results = [
				"4146.4658",
				"2160.09097",
				"1421.016630",
				"1056.2811337",
				"840.45244693",
				"697.905515738",
				"596.7419249143",
				"521.22535130670",
				"462.699999936517",
				"4151.11235949926828",
				"4105.4560506717952104",
				"3766.141452743576556495",
				"3476.31547007758175266261",
				"3227.8996369411951037539272",
				"3012.628032327175060371856910",
				"2824.28237523673520750421927549",
				"2658.1082045184290513685421371224",
				"2510.408197641774271725043200105998",
				"2378.26382978723404244204176028194721",
				"2259.3405760485093481546310857167017561",
				"2151.748796920115495668903318054895192271",
				"2053.94258153422140560404217709983787589218",
				"1964.6449912126537785588752189252882480064659",
				"1882.791999999999999999999999993035388765261194"
			];
			
			s = ".";
			for (i = 1; i < 25; i++) {
				s = i + s + i;
				a = new BigDecimal("456" + s + "123");
				b = new BigDecimal(s);
				c = a.divide(b, RoundingMode.CEILING);
				Assert.assertEquals(results[i-1], c.toString());
			}
		}

		[Test]
        public function testToString():void {
			var a:BigDecimal, i:int, z:String, s:String;
			
			a = BigDecimal.ZERO;
			Assert.assertEquals("0", a.toString());
			
			a = BigDecimal.ONE;
			Assert.assertEquals("1", a.toString());
			
			a = BigDecimal.TEN;
			Assert.assertEquals("10", a.toString());
			
			a = new BigDecimal("1234567890123456789012345678901234567890");
			Assert.assertEquals("1234567890123456789012345678901234567890", a.toString());
			
			a = new BigDecimal("-1234567890123456789012345678901234567890");
			Assert.assertEquals("-1234567890123456789012345678901234567890", a.toString());

			z = "";
			for (i = 0; i < 6; i++) {
				s = "0." + z + "1";
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());
				
				s = "-" + s;
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());

				z += "0";
			}

			z = "";
			for (i = 0; i < 6; i++) {
				s = "0." + z + "9";
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());
				
				s = "-" + s;
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());

				z += "9";
			}

			z = "";
			for (i = 0; i < 6; i++) {
				s = "1." + z + "1";
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());
				
				s = "-" + s;
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());

				z += "1";
			}

			z = "";
			for (i = 0; i < 6; i++) {
				s = "12345678901234567890." + z + "7";
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());
				
				s = "-" + s;
				
				a = new BigDecimal(s);
				Assert.assertEquals(s, a.toString());

				z += "7";
			}
			
			a = new BigDecimal("1.");
			Assert.assertEquals("1", a.toString());
	
			a = new BigDecimal(".1");
			Assert.assertEquals("0.1", a.toString());
	
			a = new BigDecimal("-1.");
			Assert.assertEquals("-1", a.toString());
	
			a = new BigDecimal("-.1");
			Assert.assertEquals("-0.1", a.toString());
			
			a = new BigDecimal("0");
			Assert.assertEquals("0", a.toString());
			
			a = new BigDecimal("-0");
			Assert.assertEquals("0", a.toString());
	
			a = new BigDecimal("0.00");
			Assert.assertEquals("0.00", a.toString());
	
			a = new BigDecimal("123");
			Assert.assertEquals("123", a.toString());
			
			a = new BigDecimal("-123");
			Assert.assertEquals("-123", a.toString());
			
			a = new BigDecimal("1.23E3");
			Assert.assertEquals("1.23E+3", a.toString());
			
			a = new BigDecimal("1.23E+3");
			Assert.assertEquals("1.23E+3", a.toString());
			
			a = new BigDecimal("12.3E+7");
			Assert.assertEquals("1.23E+8", a.toString());
			
			a = new BigDecimal("12.0");
			Assert.assertEquals("12.0", a.toString());
			
			a = new BigDecimal("12.3");
			Assert.assertEquals("12.3", a.toString());
			
			a = new BigDecimal("0.00123");
			Assert.assertEquals("0.00123", a.toString());
			
			a = new BigDecimal("-1.23E-12");
			Assert.assertEquals("-1.23E-12", a.toString());
			
			a = new BigDecimal("1234.5E-4");
			Assert.assertEquals("0.12345", a.toString());
			
			a = new BigDecimal("0E+7");
			Assert.assertEquals("0E+7", a.toString());
			
			a = new BigDecimal("15000000000000000000000000000000000000.000000000000000000000000000E-546546");
			Assert.assertEquals("1.5000000000000000000000000000000000000000000000000000000000000000E-546509", a.toString());
			
			a = new BigDecimal("15000000000000000000000000000000000000.000000000000000000000000000E+546546");
			Assert.assertEquals("1.5000000000000000000000000000000000000000000000000000000000000000E+546583", a.toString());
			
			a = new BigDecimal("1E-2147483647");
			Assert.assertEquals("1E-2147483647", a.toString());
			
			a = new BigDecimal("15000000000000000000000000000000000000.000000000000000000000000000E+2147483647");
			Assert.assertEquals("1.5000000000000000000000000000000000000000000000000000000000000000E+2147483684", a.toString());
			
			a = new BigDecimal("1.0E2147483646");
			Assert.assertEquals("1.0E+2147483646", a.toString());
		}
		
		[Test] 
		public function testRoundUp():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.UP); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("6", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-6", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundDown():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.DOWN); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("5", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-5", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundCeiling():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.CEILING); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("6", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-5", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundFloor():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.FLOOR); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("5", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-6", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundHalfUp():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.HALF_UP); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("6", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-3", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-6", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundHalfDown():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.HALF_DOWN); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("5", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-5", a.round(mc).toPlainString()); 
		} 
		
		[Test] 
		public function testRoundHalfEven():void { 
			var a:BigDecimal; 
			var mc:MathContext = new MathContext(1, RoundingMode.HALF_EVEN); 
			
			a = new BigDecimal("5.5"); 
			Assert.assertEquals("6", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("2.5"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.6"); 
			Assert.assertEquals("2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.1"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("1.0"); 
			Assert.assertEquals("1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.0"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.1"); 
			Assert.assertEquals("-1", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-1.6"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-2.5"); 
			Assert.assertEquals("-2", a.round(mc).toPlainString()); 
			
			a = new BigDecimal("-5.5"); 
			Assert.assertEquals("-6", a.round(mc).toPlainString()); 
		} 
	}
}