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
	 * Base class for the <code>Method</code> and <code>Constructor</code> classes.
	 * 
	 * @see Method
	 * @see Constructor
	 * 
	 * @author Franck WOLFF
	 */
	public class Parameterizable extends Member {

		/**
		 * @private
		 */
		private var _parameters:Array;
		
		/**
		 * Constructs a new <code>Parameterizable</code> instance.
		 * 
		 * @param declaredBy the type that this method or constructor belongs to.
		 * @desc the XML description of this method or constructor.
		 */
		function Parameterizable(declaredBy:Type, desc:XML) {
			super(declaredBy, desc);
		}
		
		/**
		 * An array that contains all the parameters of this <code>Parameterizable</code>
		 * instance.
		 * 
		 * @see Parameter
		 */
		public function get parameters():Array {
			initParameters();
			
			return _parameters.concat();
		}
		
		/**
		 * @private
		 */
		private function initParameters():void {
			if (_parameters == null) {
				_parameters = new Array();
				for each (var parameter:XML in desc.parameter)
					_parameters.push(new Parameter(this, parameter));
			}
		}
	}
}