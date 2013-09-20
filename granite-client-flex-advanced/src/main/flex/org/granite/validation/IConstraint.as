/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

	import org.granite.reflect.Annotation;

	/**
	 * The interface that all constraint validors must implement.
	 * 
	 * @author Franck WOLFF
	 */
	public interface IConstraint {
		
		/**
		 * The error message associated with this constraint.
		 */
		function get message():String;
		
		/**
		 * The processing groups with which this constraint is associated as
		 * an array of <code>Class</code>.
		 */
		function get groups():Array;
		
		/**
		 * The payload with which this constraint is associated as
		 * an array of <code>String</code>.
		 */
		function get payload():Array;
		
		/**
		 * The properties with which this constraint is associated as
		 * an array of <code>String</code> (only for class-level constraints that
		 * deal with one or more properties, in order to figure out where error
		 * messages should be displayed when this constraint fails).
		 */
		function get properties():Array;
		
		/**
		 * The <code>ValidatorFactory</code> passed to the <code>initialized</code>
		 * function.
		 */
		function get factory():ValidatorFactory;

		/**
		 * Initialize this constraint, according to the supplied annotation
		 * parameters.
		 * <br>
		 * <br>
		 * <i>The <code>org.granite.validation.BaseConstraint</code>
		 * implementation of this method throws an <code>Error</code>
		 * and must be overridden in inherited classes.</i>
		 * 
		 * @param annotation the anntotation defining this constraint.
		 * @throws org.granite.validation.ConstraintDefinitionError if a required
		 * 		argument is missing, if an argument value isn't of the expected
		 * 		type or if an unknown argument is present.
		 * 
		 * @see org.granite.validation.BaseConstraint
		 */
		function initialize(annotation:Annotation, factory:ValidatorFactory):void;

		/**
		 * Validate the <code>value</code> parameter, according to this
		 * constraint specification.
		 * <br>
		 * <br>
		 * <i>The <code>org.granite.validation.BaseConstraint</code>
		 * implementation of this method throws an <code>Error</code>
		 * and must be overridden in inherited classes.</i>
		 * 
		 * @param value the value to be validated.
		 * @return <code>null</code> if the supplied value is valid, an error
		 * 		message key otherwise.
		 * @throws org.granite.validation.ConstraintExecutionError if the
		 * 		<code>value</code> parameter is an instance of an unsupported
		 * 		type according to this constraint specification.
		 * 
		 * @see org.granite.validation.BaseConstraint
		 */
		function validate(value:*):String;
	}
}