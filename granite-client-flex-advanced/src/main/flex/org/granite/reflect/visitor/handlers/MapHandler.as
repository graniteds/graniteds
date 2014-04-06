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