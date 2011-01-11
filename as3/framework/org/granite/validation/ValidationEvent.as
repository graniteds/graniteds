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

	import flash.events.Event;
	
	/**
	 * The <code>ValidationEvent</code> is dipatched by the validated entity
	 * whenever a validation process starts (<code>START_VALIDATION</code>) or
	 * ends (<code>END_VALIDATION</code>).
	 * 
	 * @author Franck WOLFF
	 */
	public class ValidationEvent extends Event {

		/**
		 * The <code>ValidationEvent.START_VALIDATION</code> constant defines the
		 * value of the type property of a validation event object when a validation
		 * process is about to begin.
		 */
		public static const START_VALIDATION:String = "startValidation";

		/**
		 * The <code>ValidationEvent.END_VALIDATION</code> constant defines the
		 * value of the type property of a validation event object when a validation
		 * process is finished.
		 */
		public static const END_VALIDATION:String = "endValidation";

		public function ValidationEvent(type:String, bubbles:Boolean = false, cancelable:Boolean = false) {
			super(type, bubbles, cancelable);
		}
	}
}