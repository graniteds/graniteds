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

package org.granite.reflect {

	/**
	 * InvocationTargetError is an error that wraps an error thrown by an invoked method or
	 * constructor.
	 * 
	 * @see Method#invoke
	 * @see Method#invokeWithArray
	 * @see Constructor#newInstance
	 * @see Constructor#newInstanceWithArray
	 * 
	 * @author Franck WOLFF
	 */
	public class InvocationTargetError extends ReflectionError {

		/**
		 * @private
		 */
		private var _cause:Error;
		
		function InvocationTargetError(cause:Error, message:*="", id:*=0) {
			super((message ? message : "") + ': ' + (cause ? cause.toString() : ""), id);
			
			_cause = cause;
		}
		
		/**
		 * The original error cause of this  InvocationTargetError.
		 */
		public function get cause():Error {
			return _cause;
		}
	}
}