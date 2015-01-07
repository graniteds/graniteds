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
package org.granite.validation {

	import mx.resources.ResourceManager;

	/**
	 * Represents a constraint violation.
	 * 
	 * @author Franck WOLFF
	 */
	public class ConstraintViolation {
		
		private var _messageKey:String;
		private var _message:String;
		private var _constraint:IConstraint;
		private var _rootBean:*;
		private var _rootBeanType:*;
		private var _propertyPath:IPath;
		private var _leafBean:*;
		private var _invalidValue:*;
		
		/**
		 * Constructs a new <code>ConstraintViolation</code> instance.
		 * 
		 * @param messageKey the error message key to be used for message interpolation.
		 * @param constraint the <code>IConstraint</code> that failed.
		 * @param rootBean the root bean of the invalid property.
		 * @param rootBeanType the root bean type of the invalid property.
		 * @param propertyPath the path from the root bean to the invalid property.
		 * @param leafBean the leaf bean of the invalid property.
		 * @param invalidValue the value of the invalid property.
		 * 
		 * @see DefaultMessageInterpolator
		 * @see IMessageInterpolator
		 */
		function ConstraintViolation(messageKey:String,
											constraint:IConstraint,
											rootBean:*,
											rootBeanType:*,
											propertyPath:IPath,
											leafBean:*,
											invalidValue:*) {
			_messageKey = messageKey;
			_constraint = constraint;
			_rootBean = rootBean;
			_rootBeanType = rootBeanType;
			_propertyPath = propertyPath;
			_leafBean = leafBean;
			_invalidValue = invalidValue;
		}
		
		/**
		 * The error message, with all parameters resolved by the current message
		 * interpolator.
		 * 
		 * @see ValidatorFactory#messageInterpolator
		 * @see DefaultMessageInterpolator
		 * @see IMessageInterpolator
		 */
		public function get message():String {
			if (_message == null) {
				try {
					_message = ValidatorFactory.getInstance().messageInterpolator.interpolate(
						_messageKey, _constraint, _invalidValue
					);
				}
				catch (e:Error) {
					trace(e);
				}
				if (_message == null)
					_message = _messageKey;
			}
			return _message;
		}
		
		/**
		 * The raw error message key.
		 */
		public function get messageKey():String {
			return _messageKey;
		}
		
		/**
		 * The <code>IConstraint</code> that failed.
		 */
		public function get constraint():IConstraint {
			return _constraint;
		}
		
		/**
		 * The root bean of the invalid property.
		 */
		public function get rootBean():* {
			return _rootBean;
		}
		
		/**
		 * The root bean type of the invalid property.
		 */
		public function get rootBeanType():* {
			return _rootBeanType;
		}
		
		/**
		 * The path from the root bean to the invalid property.
		 */
		public function get propertyPath():IPath {
			return _propertyPath;
		}
		
		/**
		 * The property name (fields), index (collections) or key (map)
		 * of the invalid property.
		 */
		public function get property():* {
			var nodes:Array = _propertyPath.nodes;
			if (nodes.length > 0) {
				var node:INode = INode(nodes[nodes.length - 1]);
				return (node.isInIterable ? node.keyOrIndex : node.name);
			}
			return null;
		}
		
		/**
		 * The leaf bean type of the invalid property.
		 */
		public function get leafBean():* {
			return _leafBean;
		}
		
		/**
		 * The value of the invalid property.
		 */
		public function get invalidValue():* {
			return _invalidValue;
		}
		
		/**
		 * Returns the message property of this <code>ConstraintViolation</code>
		 * instance.
		 * 
		 * @return the message property of this <code>ConstraintViolation</code>
		 * 		instance.
		 * 
		 * @see #message
		 */
		public function toString():String {
			return message;
		}
	}
}