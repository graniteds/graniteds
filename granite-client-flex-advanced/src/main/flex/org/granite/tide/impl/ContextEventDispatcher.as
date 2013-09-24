/*
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
