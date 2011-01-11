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

	/**
	 * Interpolate a given constraint violation message. Implementations should
	 * be as tolerant as possible on syntax errors.
	 * 
	 * @author Franck WOLFF
	 */
	public interface IMessageInterpolator {
		
		/**
		 * Interpolate the message template based on the contraint validation context.
		 * The locale is defaulted according to the <code>IMessageInterpolator</code>
		 * implementation or may be passed as a parameter.
		 *
		 * @param messageTemplate the message to interpolate.
		 * @param constraint the constraint related to the interpolation.
		 * @param value the invalid value (according to the current constraint).
		 * @param locale an optional locale used for message localization.
		 *
		 * @return the interpolated error message.
		 */
		function interpolate(messageTemplate:String, constraint:IConstraint, value:*, locale:String = null):String;
	}
}