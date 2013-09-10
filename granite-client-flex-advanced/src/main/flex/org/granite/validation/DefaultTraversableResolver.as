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

	import org.granite.collections.IPersistentCollection;
	import org.granite.meta;
	import org.granite.reflect.Field;
	import org.granite.reflect.IAnnotatedElement;
	import org.granite.reflect.Type;
	
	/**
	 * Default implementation of the <code>ITraversableResolver</code>. This
	 * implementation takes care of lazy-initialized beans or collections.
	 * 
	 * @author Franck WOLFF
	 */
	public class DefaultTraversableResolver implements ITraversableResolver {
		
		/**
		 * @inheritDoc
		 */
		public function isReachable(traversableObject:Object,
									traversableProperty:INode,
									rootBeanType:Type,
									pathToTraversableObject:IPath,
									elementType:IAnnotatedElement):Boolean {
			if (traversableObject == null)
				return true;
			
			if (elementType is Field) {
				var value:* = Field(elementType).getValue(traversableObject);

				if (value == null)
					return true;
				
				if (value is IPersistentCollection)
					return (value as IPersistentCollection).isInitialized();
				
				try {
					if (!value.meta::isInitialized())
						return false;
				}
				catch (e:Error) {
				}
			}
			
			return true;
		}
		
		/**
		 * @inheritDoc
		 */
		public function isCascadable(traversableObject:Object,
									 traversableProperty:INode,
									 rootBeanType:Type,
									 pathToTraversableObject:IPath,
									 elementType:IAnnotatedElement):Boolean {
			return true;
		}
	}
}