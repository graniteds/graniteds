/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

	import org.granite.reflect.Field;
	import org.granite.reflect.Type;
	import org.granite.reflect.visitor.IHandler;
	import org.granite.reflect.visitor.Visitable;

	/**
	 * Handles any <code>Object</code> (usually a user class instance). This
	 * handler should be the last tried one, after all more specific handlers
	 * have returned <code>false</code> from their <code>canHandle</code>
	 * method.
	 * 
	 * @author Franck WOLFF
	 */
	public class BeanHandler implements IHandler {
		
		public function canHandle(visitable:Visitable):Boolean {
			return (visitable.value is Object);
		}
		
		public function handle(visitable:Visitable, filter:Function):Array {
			var visitables:Array = new Array();
			
			var type:Type = Type.forInstance(visitable.value);
			for each (var element:Field in type.properties) {
				var child:Visitable = new Visitable(visitable, element, element.getValue(visitable.value));
				if (filter(child))
					visitables.push(child);
			}
			
			return visitables;
		}
	}
}