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
package org.granite.reflect {

	/**
	 * The root parent class of all reflection API classes. It should be abstract and,
	 * as such, it is not intented to be used standalone.
	 * 
	 * @author Franck WOLFF
	 */
	public class DescribedElement {
		
		/**
		 * @private
		 */
		private var _declaredBy:Type;

		/**
		 * @private
		 */
		private var _desc:XML;

		/**
		 * Constructs a new <code>DescribedElement</code> instance.
		 * 
		 * @param declaredBy the <code>Type</code> that declares this element (for a
		 * 		<code>Type</code> instance, this parameter must be the <code>Type</code>
		 * 		instance itself).
		 * @param desc the XML description of this element.
		 * @throws ArgumentError if one of the parameter is <code>null</code>.
		 */
		function DescribedElement(declaredBy:Type, desc:XML) {
			if (declaredBy == null || desc == null)
				throw new ArgumentError("declaredBy and desc cannot be null");
			
			_declaredBy = declaredBy;
			_desc = desc;
		}
		
		/**
		 * The <code>Type</code> instance this described element belongs to.
		 */
		public function get declaredBy():Type {
			return _declaredBy;
		}
		
		/**
		 * @private
		 */
		internal function get desc():XML {
			return _desc;
		}
		
		/**
		 * Returns the a string (XML) representation of this described element.
		 */
		public function toString():String {
			return _desc.toXMLString();
		}
		
		/**
		 * Tells if this <code>DescribedElement</code> instance is equal to the supplied
		 * o parameter.
		 * 
		 * @param o the object to check for equality.
		 * @return <code>true</code> if this instance is equal to the o parameter,
		 * 		<code>false</code> otherwise.
		 */
		public function equals(o:*):Boolean {
			return (o === this);
		}
	}
}