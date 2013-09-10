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

package org.granite.tide.impl {
	
	import org.granite.reflect.Method;
	import org.granite.tide.BaseContext;
	
	
	/**
	 * 	Interface for component instance provider 
	 * 
	 * 	@author William DRAI
	 */
	public class MethodComponentProducer implements IComponentProducer {
		
		private var _implComponentName:String = null;
		private var _method:Method = null;
		
		public function MethodComponentProducer(implComponentName:String, method:Method):void {
			_implComponentName = implComponentName;
			_method = method;
		}
		
		public function get componentName():String {
			return _implComponentName;
		}
		
   		public function produce(context:BaseContext, create:Boolean):Object {
   			var instance:Object = context.meta_getInstance(_implComponentName, create);
   			if (instance == null)
   				return null;
   			
   			var args:Array = [];
   			var params:Array = _method.parameters;
   			for (var i:int = 0; i < params.length; i++)
   				args.push(context.byType(params[i].type.getClass()));
   			
   			return _method.invokeWithArray(instance, args);
   		}		
	}
}
