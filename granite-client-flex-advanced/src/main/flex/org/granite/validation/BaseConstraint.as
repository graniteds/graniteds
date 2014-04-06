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
package org.granite.validation {

	import flash.utils.Dictionary;
	
	import org.granite.math.BigDecimal;
	import org.granite.math.BigInteger;
	import org.granite.math.Long;
	import org.granite.reflect.Annotation;
	import org.granite.validation.groups.Default;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;

	/**
	 * A basic class that simplifies <code>IConstraint</code> implementations.
	 * See standard constraint implementations for usage.
	 * 
	 * @author Franck WOLFF
	 */
	public class BaseConstraint implements IConstraint {
		
		///////////////////////////////////////////////////////////////////////
		// Static constants.

		/**
		 * An array of date types: <code>[Date]</code> (used when checking
		 * the value type of a constraint annotation argument).
		 * 
		 * @see org.granite.validation.helper.ConstraintHelper#checkValueType
		 */
		protected static const DATE_TYPES:Array = [Date];

		/**
		 * An array of boolean types: <code>[Boolean]</code> (used when checking
		 * the value type of a constraint annotation argument).
		 * 
		 * @see org.granite.validation.helper.ConstraintHelper#checkValueType
		 */
		protected static const BOOLEAN_TYPES:Array = [Boolean];

		/**
		 * An array of string types: <code>[String]</code> (used when checking
		 * the value type of a constraint annotation argument).
		 * 
		 * @see org.granite.validation.helper.ConstraintHelper#checkValueType
		 */
		protected static const STRING_TYPES:Array = [String];

		/**
		 * An array of numeric or string types:
		 * <code>[BigDecimal, BigInteger, Long, Number, int, uint String]</code>
		 * (used when checking the value type of a constraint annotation argument).
		 * 
		 * @see org.granite.validation.helper.ConstraintHelper#checkValueType
		 */
		protected static const NUMBER_STRING_TYPES:Array = [
			BigDecimal,
			BigInteger,
			Long,
			Number,
			int,
			uint,
			String
		];
		
		/**
		 * The definition of the standard <code>groups</code> constraint annotation
		 * argument, as an optional array of <code>Class</code> names.
		 */
		protected static const GROUPS_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"groups",
			Class,
			null,
			true,
			true,
			["javax.validation.groups.Default", "org.granite.validation.groups.Default"]
		);
		
		/**
		 * The definition of the standard <code>payload</code> constraint annotation
		 * argument, as an optional array of <code>String</code>s.
		 */
		protected static const PAYLOAD_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"payload",
			String,
			[],
			true,
			true
		);
		
		/**
		 * The definition of the standard <code>properties</code> constraint annotation
		 * argument, as an optional array of <code>String</code>s.
		 */
		protected static const PROPERTIES_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"properties",
			String,
			[],
			true,
			true
		);

		/**
		 * Creates a definition of the standard <code>message</code> annotation
		 * argument with the supplied string.
		 * 
		 * @param message the message key used in the returned message
		 * 		<code>ParameterDefinition</code>.
		 * @return a new <code>ParameterDefinition</code> for the standard
		 * 		<code>message</code> constraint annotation argument.
		 */
		protected static function createMessageParameter(message:String):ParameterDefinition {
			return new ParameterDefinition(
				"message",
				String,
				message,
				true
			);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		/**
		 * Direct access (protected) to the <code>ValidatorFactory</code>
		 * instance passed to the initialized function.
		 * 
		 * @see #initialize
		 */
		protected var _factory:ValidatorFactory;
		
		/**
		 * Direct access (protected) to the message key defined for this
		 * constraint.
		 * 
		 * @see #internalInitialize
		 */
		protected var _message:String;
		
		/**
		 * Direct access (protected) to the groups defined for this
		 * constraint.
		 * 
		 * @see #internalInitialize
		 */
		protected var _groups:Array;
		
		/**
		 * Direct access (protected) to the payload defined for this
		 * constraint.
		 * 
		 * @see #internalInitialize
		 */
		protected var _payload:Array;
		
		/**
		 * Direct access (protected) to the properties defined for this
		 * constraint.
		 * 
		 * @see #internalInitialize
		 */
		protected var _properties:Array;
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * @inheritDoc
		 */
		public function get message():String {
			return _message;
		}
		
		
		/**
		 * @inheritDoc
		 */
		public function get groups():Array {
			if (_groups == null || _groups.length == 0)
				_groups = [Default];
			return _groups;
		}
		
		/**
		 * @inheritDoc
		 */
		public function get payload():Array {
			return _payload;
		}
		
		/**
		 * @inheritDoc
		 */
		public function get properties():Array {
			return _properties;
		}
		
		/**
		 * @inheritDoc
		 */
		public function get factory():ValidatorFactory {
			return _factory;
		}

		/**
		 * @inheritDoc
		 */
		public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
			throw new Error("Not implemented");
		}
		
		/**
		 * Parse constraint parameters according to the annotation arguments,
		 * initialize the message, groups and payload properties and return a
		 * <code>Dictionary</code> that contains all parsed values.
		 * 
		 * @param annotation the constraint annotation with its arguments.
		 * @param defaultMessage the default error message for this constraint.
		 * @param parameterDefinitions an array of <code>ParameterDefinition</code>
		 * 		that contains specific argument definitions for this constraint
		 * 		(excluding message, groups and payload).
		 * @return a <code>Dictionary</code> with parsed arguments.
		 * @throws org.granite.validation.ConstraintDefinitionError if a required
		 * 		argument is missing, if an argument value isn't of the expected
		 * 		type or if an unknown argument is present.
		 */
		protected function internalInitialize(factory:ValidatorFactory,
											  annotation:Annotation,
											  defaultMessage:String,
											  parameterDefinitions:Array = null):Dictionary {
			if (factory == null)
				throw new ArgumentError("factory cannot be null");
			_factory = factory;
			
			const defaultParameters:Array = [
				createMessageParameter(defaultMessage),
				GROUPS_PARAMETER,
				PAYLOAD_PARAMETER
			];
			
			if (parameterDefinitions == null)
				parameterDefinitions = defaultParameters;
			else
				parameterDefinitions = parameterDefinitions.concat(defaultParameters);
			
			var params:Dictionary = ConstraintHelper.parseParameters(this, annotation.args, parameterDefinitions);

			_message = params["message"];
			_groups = params[GROUPS_PARAMETER.name];
			_payload = params[PAYLOAD_PARAMETER.name];
			_properties = params[PROPERTIES_PARAMETER.name];
			
			return params;
		}

		/**
		 * @inheritDoc
		 */
		public function validate(value:*):String {
			throw new Error("Not implemented");
		}
	}
}