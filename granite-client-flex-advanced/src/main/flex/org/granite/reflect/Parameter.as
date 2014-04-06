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