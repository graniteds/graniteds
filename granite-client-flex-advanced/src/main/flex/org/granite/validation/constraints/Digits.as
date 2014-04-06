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
package org.granite.validation.constraints {

	import flash.utils.Dictionary;
	
	import org.granite.math.BigDecimal;
	import org.granite.reflect.Annotation;
	import org.granite.validation.BaseConstraint;
	import org.granite.validation.ConstraintDefinitionError;
	import org.granite.validation.ValidatorFactory;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;

	/**
	 * The ActionsScript3 implementation of the
	 * <code>javax.validation.constraints.Digits</code> annotation
	 * validator.<p />
	 * 
	 * The annotated element must be a number whithin accepted range.<p />
	 * 
	 * Accepted arguments are:
	 * <ul>
	 * <li>message (optional): used to create the error message (override the
	 * 		default message).</li>
	 * <li>groups (optional): a comma separated list of fully qualified interface names that
	 * 		specifies the processing groups with which the constraint declaration is associated.</li>
	 * <li>payload (optional): a comma separated list of <code>String</code> that specifies the
	 * 		payloads with which the constraint declaration is associated (unlike the Java
	 * 		implementation, payloads are arbitrary <code>String</code>s and does not represent
	 * 		necessarily existing class names).</li>
	 * <li>integer (<b>required</b>): the maximum number of integral digits.</li>
	 * <li>fraction (<b>required</b>): the maximum number of fractional digits.</li>
	 * </ul>
	 * 
	 * Supported types are:
	 * <ul>
	 * <li><code>org.granite.math.BigDecimal</code></li>
	 * <li><code>org.granite.math.BigInteger</code></li>
	 * <li><code>org.granite.math.Long</code></li>
	 * <li><code>Number</code> (with possible rounding issues)</li>
	 * <li><code>int</code></li>
	 * <li><code>uint</code></li>
	 * <li><code>String</code></li>
	 * </ul>
	 * 
	 * <code>null</code> elements are considered valid.<p />
	 * 
	 * Example:<p />
	 * <listing>
	 * [Digits(message="{my.custom.message}", groups="path.to.MyGroup1, path.to.MyGroup2", integer="3", fraction="2")]
	 * public function get property():BigDecimal {
	 *     ...
	 * }</listing>
	 * 
	 * General notes on escaping:
	 * <ul>
	 * <li>Double quotes: all double quotes (<code>"</code>) in argument values <b>must</b> be escaped
	 * 		by using the XML entity (<code>&amp;quot;</code>).</li>
	 * <li>Ampersand: all ampersands (<code>&amp;</code>) in argument values should be escaped by
	 * 		using the XML entity (<code>&amp;amp;</code>).</li>
	 * <li>Less and greater than: all "less than" (<code>&lt;</code>) characters in argument values
	 * 		<b>must</b> be escaped by using the XML entity (<code>&amp;lt;</code>). All "greater than"
	 * 		(<code>&gt;</code>) characters in argument values should be escaped by using the XML entity
	 * 		(<code>&amp;gt;</code>)</li>
	 * <li>White spaces: since all argument values are trimed, you may use the pseudo XML entity
	 * 		(<code>&amp;space;</code>) in order to keep leading or trailing white spaces in literals.</li>
	 * <li>Comma: arguments specified as comma separated String lists (such as <code>groups</code>)
	 * 		may use the pseudo XML entity (<code>&amp;comma;</code>) in order to keep comma characters
	 * 		in literals.</li>
	 * </ul>
	 *
	 * @author Franck WOLFF
	 */
	public class Digits extends BaseConstraint {
		
		///////////////////////////////////////////////////////////////////////
		// Static constants.

		/**
		 * The default message key of this constraint. 
		 */
		public static const DEFAULT_MESSAGE:String = "{javax.validation.constraints.Digits.message}";

		private static const ACCEPTED_TYPES:Array = NUMBER_STRING_TYPES;

		private static const INTEGER_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"integer",
			int,
			null,
			false
		);		
		private static const FRACTION_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"fraction",
			int,
			null,
			false
		);

		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		private var _integer:int;
		private var _fraction:int;

		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The maximum number of integral digits accepted for this number.
		 */
		public function get integer():int {
			return _integer;
		}
		
		/**
		 * The maximum number of fractional digits accepted for this number.
		 */
		public function get fraction():int {
			return _fraction;
		}

		///////////////////////////////////////////////////////////////////////
		// IConstraint implementation.

		/**
		 * @inheritDoc
		 */
		override public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
			var params:Dictionary = internalInitialize(factory, annotation, DEFAULT_MESSAGE, [INTEGER_PARAMETER, FRACTION_PARAMETER]);

			_integer = int(params[INTEGER_PARAMETER.name]);
			if (_integer < 0)
				throw new ConstraintDefinitionError(this, "integer parameter cannot be negative");

			_fraction = int(params[FRACTION_PARAMETER.name]);
			if (_fraction < 0)
				throw new ConstraintDefinitionError(this, "fraction parameter cannot be negative");
		}
		
		/**
		 * @inheritDoc
		 */
		override public function validate(value:*):String {
			if (Null.isNull(value))
				return null;

			if (value === Number.NEGATIVE_INFINITY || value === Number.POSITIVE_INFINITY)
				return message;
			
			ConstraintHelper.checkValueType(this, value, ACCEPTED_TYPES);

			var number:BigDecimal = null;

			if (value is BigDecimal)
				number = (value as BigDecimal);
			else {
				try {
					number = new BigDecimal(value);
				}
				catch (e:Error) {
				}
			}
			
			var integerLength:int = number.precision - number.scale;
			var fractionLength:int = number.scale;
			if (fractionLength < 0)
				fractionLength = 0;

			if (integerLength > _integer || fractionLength > _fraction)
				return message;
			
			return null;
		}
	}
}