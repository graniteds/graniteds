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

package org.granite.tide {
    
    import mx.core.IUIComponent;
    
	
	/**
	 * 	Interface for context managers
	 * 
	 * 	@author William DRAI
	 */
	public interface IContextManager {
		
		function getContext(contextId:String = null, parentContextId:String = null, create:Boolean = true):BaseContext;
		
		function newContext(contextId:String = null, parentContextId:String = null):BaseContext;
		
		function destroyContext(contextId:String, force:Boolean = false):void;
		
		function getAllContexts():Array;
		
		function forEachChildContext(parentContext:BaseContext, callback:Function, token:Object = null):void;

		function destroyContexts(force:Boolean = false):void;
		
		function destroyFinishedContexts():void;
		
		function addToContextsToDestroy(contextId:String):void;
		
	    function removeFromContextsToDestroy(contextId:String):void;
	    
		function findContext(uiComponent:IUIComponent, returnDefault:Boolean = true):BaseContext;
		
		function findContextForUIComponent(uiComponent:IUIComponent):BaseContext;
		
		function unregisterComponent(name:String, component:Object):void;
		
		function destroyComponentInstances(name:String):void;
		
        function updateContextId(previousContextId:String, context:BaseContext):void;
	}
}
