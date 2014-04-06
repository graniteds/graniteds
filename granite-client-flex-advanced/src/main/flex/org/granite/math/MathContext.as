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

	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	[RemoteClass(alias="java.math.MathContext")]
	/**
	 * Immutable objects which encapsulate the context settings which
	 * describe certain rules for numerical operators, such as those
	 * implemented by the <code>BigDecimal</code> class.
	 * 
	 * <p>The base-independent settings are:
	 * <ol>
	 * <li><code>precision</code>:
	 * the number of digits to be used for an operation; results are
	 * rounded to this precision</li>
	 * 
	 * <li><code>roundingMode</code>:
	 * a <code>RoundingMode</code> object which specifies the algorithm to be
	 * used for rounding.</li>
	 * </ol>
	 * 
	 * <i>This class is basically an ActionScript3 port of the Java
	 * class <code>java.math.MathContext</code> originally written by
	 * Mike Cowlishaw and Joseph D. Darcy.</i>
	 * </p>
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see RoundingMode
	 * @see BigDecimal
	 */
	public final class MathContext implements IExternalizable {
		
		///////////////////////////////////////////////////////////////////////
		// Constants.

		private static const STRING_PATTERN:RegExp = /^precision=([0-9]+) roundingMode=([A-Z_]+)$/;
		
	    /**
	     * A <code>MathContext</code> object whose settings have the values
	     * required for unlimited precision arithmetic.
		 * <br>
	     * The values of the settings are:
	     * <code>precision=0 roundingMode=HALF_UP</code>
		 * 
		 * @see RoundingMode#HALF_UP
	     */
		public static const UNLIMITED:MathContext = new MathContext(0, RoundingMode.HALF_UP);

	    /**
	     * A <code>MathContext</code> object with a precision setting
	     * matching the IEEE 754R Decimal32 format, 7 digits, and a
	     * rounding mode of <code>RoundingMode.HALF_EVEN</code>, the
	     * IEEE 754R default.
		 * 
		 * @see RoundingMode#HALF_EVEN
	     */
		public static const DECIMAL32:MathContext = new MathContext(7, RoundingMode.HALF_EVEN);
		
	    /**
	     * A <code>MathContext</code> object with a precision setting
	     * matching the IEEE 754R Decimal64 format, 16 digits, and a
	     * rounding mode of <code>RoundingMode.HALF_EVEN</code>, the
	     * IEEE 754R default.
		 * 
		 * @see RoundingMode#HALF_EVEN
	     */
		public static const DECIMAL64:MathContext = new MathContext(16, RoundingMode.HALF_EVEN);
		
	    /**
	     * A <code>MathContext</code> object with a precision setting
	     * matching the IEEE 754R Decimal128 format, 34 digits, and a
	     * rounding mode of <code>RoundingMode.HALF_EVEN</code>, the
	     * IEEE 754R default.
		 * 
		 * @see RoundingMode#HALF_EVEN
	     */
		public static const DECIMAL128:MathContext = new MathContext(34, RoundingMode.HALF_EVEN);
		
		///////////////////////////////////////////////////////////////////////
		// Fields.
		
		private var _precision:int = 0;
		private var _roundingMode:RoundingMode = RoundingMode.HALF_UP;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
	    /**
	     * Constructs a new <code>MathContext</code> with a specified
	     * precision and rounding mode.
	     *
	     * @param precision The non-negative <code>int</code> precision setting.
	     * @param roundingMode The rounding mode to use. If this parameter is
		 * 		omitted or <code>null</code>, it is defaulted to
		 * 		<code>RoundingMode.HALF_UP</code>.
		 *
   	     * @throws org.granite.math.IllegalArgumentError if the <code>precision</code>
		 * 		parameter is less than zero.
		 * 
		 * @see RoundingMode#HALF_UP
	     */
		function MathContext(precision:int = 0, roundingMode:RoundingMode = null) {
			if (precision < 0)
				throw new IllegalArgumentError("precision must be >= 0");
			_precision = precision;
			
			if (roundingMode != null)
				_roundingMode = roundingMode;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Public static initializers.
		
	    /**
	     * Returns a new <code>MathContext</code> from a string.
	     *
	     * The string must be in the same format as that produced by the
	     * <code>toString</code> method.
	     *
	     * @param s The string to be parsed
		 * @return a new <code>MathContext</code> instance according to
		 * 		the supplied parameter.
	     * @throws org.granite.math.IllegalArgumentError if the precision section
		 * 		is out of range or the string is not in the format created by the
		 * 		<code>toString</code> method.
		 * 
		 * @see #toString
	     */
		public static function forString(s:String):MathContext {
			if (!STRING_PATTERN.test(s))
				throw new IllegalArgumentError("Illegal MathContext format: " + s);
			
			var tokens:Array = s.split(STRING_PATTERN),
				prec:String = tokens[1].replace(/^0+/, ""),
				round:String = tokens[2],
				precision:int,
				roundingMode:RoundingMode;
			
			precision = parseInt(prec);
			// check precision for possible overflow (assume no leading zeros)...
			if (precision.toString() != prec)
				throw new IllegalArgumentError("Illegal MathContext format: " + s);
			
			roundingMode = RoundingMode.valueOf(round);
			
			return new MathContext(precision, roundingMode);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
	    /**
	     * The number of digits to be used for an operation.  A value of 0
	     * indicates that unlimited precision (as many digits as are
	     * required) will be used.  Note that leading zeros (in the
	     * coefficient of a number) are never significant.
	     * <br>
	     * <code>precision</code> will always be non-negative.
	     */
		public function get precision():int {
			return _precision;
		}
		
	    /**
	     * The rounding algorithm to be used for an operation.
	     *
	     * @see RoundingMode
	     */
		public function get roundingMode():RoundingMode {
			return _roundingMode;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conmparison.
		
	    /**
	     * Compares this <code>MathContext</code> with the specified
	     * <code>Object</code> for equality.
	     *
	     * @param o <code>Object</code> to which this <code>MathContext</code>
		 * 		is to be compared.
	     * @return <code>true</code> if and only if the specified
		 * 		<code>Object</code> is a <code>MathContext</code> object which
		 * 		has exactly the same settings as this object.
	     */
		public function equals(o:*):Boolean {
			if (!(o is MathContext))
				return false;
			return (
				(o as MathContext)._precision == _precision &&
				(o as MathContext)._roundingMode == _roundingMode
			);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Conversion.
		
	    /**
	     * Returns the string representation of this <code>MathContext</code>.
	     * The <code>String</code> returned represents the settings of the
	     * <code>MathContext</code> object as two space-delimited words
	     * (separated by a single space character, <code>'&#92;u0020'</code>,
	     * and with no leading or trailing white space), as follows:
	     * <ol>
	     * <li>
	     * The string <code>"precision="</code>, immediately followed
	     * by the value of the precision setting as a numeric string as if
	     * generated by the <code>int.toString()</code> method.</li>
	     *
	     * <li>
	     * The string <code>"roundingMode="</code>, immediately
	     * followed by the value of the <code>roundingMode</code> setting as a
	     * word.  This word will be the same as the name of the
	     * corresponding public constant in the <code>RoundingMode</code> enum.</li>
	     * </ol>
	     * <br>
	     * For example: <code>"precision=9 roundingMode=HALF_UP"</code>
	     * <br>
	     * Additional words may be appended to the result of
	     * <code>toString</code> in the future if more properties are added to
	     * this class.
	     *
	     * @return a <code>String</code> representing the context settings.
		 * 
		 * @see #forString
		 * @see RoundingMode
	     */
		public function toString():String {
			return "precision=" + _precision + " " + "roundingMode=" + roundingMode.toString();
		}
		
		///////////////////////////////////////////////////////////////////////
		// IExternalizable implementation.
		
		/**
		 * @private
		 */
		public function readExternal(input:IDataInput):void {
			_precision = (input.readObject() as int);
			_roundingMode = (input.readObject() as RoundingMode);
		}
		
		/**
		 * @private
		 */
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(_precision);
			output.writeObject(_roundingMode);
		}
	}
}