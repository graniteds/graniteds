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

	import flash.events.Event;
	
	/**
	 * An <code>Event</code> that is dispatched when a validation failed.
	 * 
	 * @author William DRAI
	 */
	public class ConstraintViolationEvent extends Event {

		/**
		 * The ConstraintViolationsEvent.CONSTRAINT_VIOLATION constant defines
		 * the value of the type property of a constraint violation event object.
		 */
		public static const CONSTRAINT_VIOLATION:String = "constraintViolation";
		
		/**
		 * @private
		 */
		private var _violation:ConstraintViolation;
		
		/**
		 * Constructs a new <code>ConstraintViolationsEvent</code>.
		 * 
		 * @param violation a <code>ConstraintViolation</code> describing
		 * 		the failure for a given property.
		 * @param bubbles determines whether the Event object participates in
		 * 		the bubbling stage of the event flow.
		 * @param cancelable determines whether the Event object can be canceled.
		 */
		function ConstraintViolationEvent(violation:ConstraintViolation, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(CONSTRAINT_VIOLATION, bubbles, cancelable);
			
			if (violation == null)
				throw new ArgumentError("Parameter violation must not be null");
			
			_violation = violation;
		}
		
		/**
		 * The <code>ConstraintViolation</code> passed to this
		 * <code>ConstraintViolationEvent</code> construtor.
		 */
		public function get violation():ConstraintViolation {
			return _violation;
		}
	}
}