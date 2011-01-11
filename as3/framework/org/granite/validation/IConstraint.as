/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
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