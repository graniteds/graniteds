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
    
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    
    import org.granite.tide.BaseContext;
	import org.granite.tide.IContextManager;
    import org.granite.tide.Tide;


    public class ContextEventDispatcher extends EventDispatcher {
        
		private var _context:BaseContext;
        
        public function ContextEventDispatcher(target:BaseContext):void {
        	super(target);
			_context = target;
        }

		public override function hasEventListener(type:String):Boolean {
			return type == PropertyChangeEvent.PROPERTY_CHANGE || super.hasEventListener(type);
		} 
        
        public override function dispatchEvent(event:Event):Boolean {
        	var res:Boolean = super.dispatchEvent(event);
        	
        	if (event is PropertyChangeEvent
					&& !(PropertyChangeEvent(event).property is String && PropertyChangeEvent(event).property.indexOf("meta_") == 0)) {
        		_context.meta_contextManager.forEachChildContext(_context, function(ctx:BaseContext):void {
        			ctx.dispatchEvent(event);
        		});
        	}
        	return res;
        }
    }
}
