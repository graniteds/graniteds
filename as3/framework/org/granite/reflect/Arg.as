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
	 * Represents an ActionScript 3 annotation (or metadata) argument.
	 * 
	 * @see Annotation
	 * 
	 * @author Franck WOLFF
	 */
	public class Arg extends DescribedElement {

		/**
		 * @private
		 */
		private var _annotation:Annotation;
		
		/**
		 * Constructs a new <code>Arg</code> instance.
		 * 
		 * @param annotation the annotation to which this arguments belongs.
		 * @desc the XML description of this annotation.
		 */
		function Arg(annotation:Annotation, desc:XML) {
			super(annotation.declaredBy, desc);

			_annotation = annotation;
		}
		
		/**
		 * The key (or name) of this argument.
		 */
		public function get key():String {
			return desc.@key;
		}
		
		/**
		 * The value of this argument.
		 */
		public function get value():String {
			return desc.@value;
		}
		
		/**
		 * The annotation that holds this argument.
		 */
		public function get annotation():Annotation {
			return _annotation;
		}
	}
}