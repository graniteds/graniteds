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
	 * The <code>Parameter</code> class represents a method or constructor parameter.
	 * 
	 * @see Parameterizable#parameters
	 * @see Method
	 * @see Constructor
	 * 
	 * @author Franck WOLFF
	 */
	public class Parameter extends DescribedElement {

		/**
		 * @private
		 */
		private var _parameterizable:Parameterizable;
		
		/**
		 * Constructs a new <code>Parameter</code> instance.
		 * 
		 * @param parameterizable a <code>Parameterizable</code> instance (either a method
		 * 		or a constructor).
		 * @desc the XML description of this parameter.
		 */
		function Parameter(parameterizable:Parameterizable, desc:XML) {
			super(parameterizable.declaredBy, desc);

			_parameterizable = parameterizable;
		}
		
		/**
		 * The type of this parameter (may be <code>Type.WILD</code>).
		 * 
		 * @see Type#WILD
		 */
		public function get type():Type {
			return Type.forName(desc.@type, declaredBy.domain);
		}
		
		/**
		 * The method or constructor this parameter belongs to.
		 */
		public function get parameterizable():Parameterizable {
			return _parameterizable;
		}
		
		/**
		 * Checks if this parameter is optional.
		 * 
		 * @return <code>true</code> if this parameter is optional, <code>false</code>
		 * 		otherwise.
		 */
		public function isOptional():Boolean {
			return desc.@optional == "true";
		}
		
		/**
		 * Returns the 1-based position of this parameter in the method or constructor
		 * it belongs to.
		 * 
		 * @return the 1-based position of this parameter.
		 */
		public function get index():int {
			return int(desc.@index.toString());
		}
	}
}