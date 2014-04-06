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
	
	import mx.collections.IList;
	
	import org.granite.reflect.Annotation;
	import org.granite.util.ClassUtil;
	import org.granite.validation.BaseConstraint;
	import org.granite.validation.ConstraintDefinitionError;
	import org.granite.validation.ConstraintExecutionError;
	import org.granite.validation.ValidatorFactory;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;

	/**
	 * The ActionsScript3 implementation of the
	 * <code>javax.validation.constraints.Size</code> annotation
	 * validator.<p />
	 * 
	 * The annotated element size must be between the specified boundaries
	 * (included).<p />
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
	 * <li>min (optional): the minimum size (default is <code>0</code>).</li>
	 * <li>max (optional): the maximum size (default is <code>int.MAX_VALUE</code>.</li>
	 * </ul>
	 * 
	 * Supported types are:
	 * <ul>
	 * <li><code>String</code></li>
	 * <li><code>Array</code></li>
	 * <li><code>Vector</code></li>
	 * <li><code>Dictionary</code></li>
	 * <li><code>IList</code> implementators (such as <code>ArrayCollection</code>)</li>
	 * <li>any other class instance that defines a length property
	 * 		(such as <code>org.granite.collection.IMap</code> implementators)</li>
	 * </ul>
	 * 
	 * <code>null</code> elements are considered valid.<p />
	 * 
	 * Example:<p />
	 * <listing>
	 * [Size(message="{my.custom.message}", groups="path.to.MyGroup1, path.to.MyGroup2", min="2", max="10")]
	 * public function get property():String {
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
	public class Size extends BaseConstraint {
		
		///////////////////////////////////////////////////////////////////////
		// Static constants.

		/**
		 * The default message key of this constraint. 
		 */
		public static const DEFAULT_MESSAGE:String = "{javax.validation.constraints.Size.message}";

		private static const MIN_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"min",
			int,
			0,
			true
		);
		
		private static const MAX_PARAMETER:ParameterDefinition = new ParameterDefinition(
			"max",
			int,
			int.MAX_VALUE,
			true
		);

		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		private var _min:int;
		private var _max:int;

		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The minimum size as specified in the annotation arguments.
		 */
		public function get min():int {
			return _min;
		}
		
		/**
		 * The maximum size as specified in the annotation arguments.
		 */
		public function get max():int {
			return _max;
		}

		///////////////////////////////////////////////////////////////////////
		// IConstraint implementation.

		/**
		 * @inheritDoc
		 */
		override public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
			var params:Dictionary = internalInitialize(factory, annotation, DEFAULT_MESSAGE, [MIN_PARAMETER, MAX_PARAMETER]);

			_min = int(params[MIN_PARAMETER.name]);
			if (_min < 0)
				throw new ConstraintDefinitionError(this, "min parameter cannot be negative");

			_max = int(params[MAX_PARAMETER.name]);
			if (_max < 0)
				throw new ConstraintDefinitionError(this, "max parameter cannot be negative");
		}
		
		/**
		 * @inheritDoc
		 */
		override public function validate(value:*):String {
			if (Null.isNull(value))
				return null;

			var size:uint = 0;
			
			if (value is Dictionary) {
				for (var o:* in (value as Dictionary))
            		size++;
			}
			else {
				try {
					size = uint(value.length);
				}
				catch (e:Error) {
					throw new ConstraintExecutionError(this, value, "Value must have a length property");
				}
			}
				
			if (size < min || size > max)
				return message;
			
			return null;
		}
	}
}