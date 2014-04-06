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

	import org.granite.reflect.Type;

	/**
	 * Validate bean instances or properties.
	 * 
	 * @author Franck WOLFF
	 */
	public interface IValidator {
		
		/**
		 * Validates all constraints on <code>bean</code>.
		 * 
		 * @param bean the bean to validate.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintValidation</code>, possibly empty
		 * 		or null.
		 */
		function validate(bean:Object, groups:Array = null):Array;

		/**
		 * Validates all constraints placed on the property of <code>bean</code>
		 * named <code>propertyName</code>.
		 * 
		 * @param bean the bean holding the property to validate.
		 * @param propertyName the property name to validate.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintValidation</code>, possibly empty
		 * 		or null.
		 */
		function validateProperty(bean:Object, propertyName:String, groups:Array = null):Array;
		
		/**
		 * Validates all constraints placed on the property named <code>propertyName</code>
		 * of the class <code>type</code> would the property value be <code>value</code>.
		 * 
		 * @param type the bean type holding the property to validate.
		 * @param propertyName the property name to validate.
		 * @param value the property value to validate.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintValidation</code>, possibly empty
		 * 		or null.
		 */
		function validateValue(type:Type, propertyName:String, value:*, groups:Array = null):Array;
	}
}