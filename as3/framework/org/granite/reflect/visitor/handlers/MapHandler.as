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

package org.granite.reflect.visitor.handlers {

	import org.granite.collections.IMap;
	import org.granite.reflect.DynamicProperty;
	import org.granite.reflect.visitor.IHandler;
	import org.granite.reflect.visitor.Visitable;
	
	/**
	 * Handles any <code>org.granite.collections.IMap</code>
	 * instance.
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see org.granite.collections.IMap
	 */
	public class MapHandler implements IHandler {
		
		public function canHandle(visitable:Visitable):Boolean {
			return (visitable.value is IMap);
		}
		
		public function handle(visitable:Visitable, filter:Function):Array {
			var visitables:Array = new Array();

			for (var key:* in (visitable.value as IMap).keySet) {
				var element:DynamicProperty = new DynamicProperty(
					visitable.value,
					key,
					function(holder:IMap, key:*):* {
						return holder.get(key);
					},
					function(holder:IMap, key:*, value:*):* {
						return holder.put(key, value);
					}
				);
				var child:Visitable = new Visitable(visitable, element, element.value);
				if (filter(child))
					visitables.push(child);
			}
			
			return visitables;
		}
	}
}