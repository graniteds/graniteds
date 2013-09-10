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

package org.granite.tide.seam.framework {
    
    import flash.utils.Dictionary;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    
    import org.granite.tide.IComponent;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideContextEvent;
    
	
	[Bindable]
	public class ConversationList extends ArrayCollection implements IComponent {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.framework.ConversationList");
        
        public static const BEGIN_CONVERSATION:String = "org.jboss.seam.beginConversation";
        public static const END_CONVERSATION:String = "org.jboss.seam.endConversation";
        public static const CONVERSATION_TIMEOUT:String = "org.jboss.seam.conversationTimeout";
        public static const CONVERSATION_DESTROYED:String = "org.jboss.seam.conversationDestroyed";
		
		
	    private var _componentName:String = null;
        private var _context:BaseContext = null;
        
        private var _initialized:Boolean = false;
        
		
		public function meta_init(componentName:String, context:BaseContext):void {			
		    log.debug("init ConversationList {0}", componentName);
			_componentName = componentName;
			_context = context;
		    _context.addContextEventListener(BEGIN_CONVERSATION, refreshList, true); 
		    _context.addContextEventListener(END_CONVERSATION, refreshList, true); 
		    _context.addContextEventListener(CONVERSATION_TIMEOUT, refreshList, true); 
		    _context.addContextEventListener(CONVERSATION_DESTROYED, refreshList, true);
		    _context.addContextEventListener(Tide.CONTEXT_CREATE, contextCreate);
		    _context.addContextEventListener(Tide.CONTEXT_DESTROY, contextDestroy);
		    _context.addContextEventListener(Tide.CONTEXT_RESULT, contextResult);
		}
		
		public function refreshList(event:TideContextEvent):void {
			internalRefreshList();
		}
		
		public function loginHandler(event:TideContextEvent):void {
			internalRefreshList();
		}
		
		private function internalRefreshList():void {
			_context.meta_addResult(_componentName, null);
			_context.conversationListFactory.getConversationEntryList(refreshListResult, refreshListFault);
		}
		
		protected function refreshListResult(event:TideResultEvent):void {
			// GDS-772: Force creation of local conversation contexts without parent
			// Otherwise dispatching conversation events from existing server conversation events creates nested conversations
			for each (var obj:Object in this)
				_context.meta_tide.getContext(obj.id);
		}
		
		protected function refreshListFault(event:TideFaultEvent):void {
			log.error("Could not get converstation list: " + event);
		}
		

		private var _pendingDescription:Dictionary = new Dictionary(true);
		
		public function contextCreate(event:TideContextEvent):void {
			event.context.conversation.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, conversationChangeHandler, false, 0, true); 
		}
		
		public function contextDestroy(event:TideContextEvent):void {
			delete _pendingDescription[event.context.conversation];
			event.context.conversation.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, conversationChangeHandler); 
		}
		
		private function conversationChangeHandler(event:PropertyChangeEvent):void {
			if (event.property == "description")
				_pendingDescription[event.target] = event.newValue;
		}
		
		public function contextResult(event:TideContextEvent):void {
			if (event.context.contextId !== null && !event.context.meta_isGlobal()) {
				var description:String = _pendingDescription[event.context.conversation] as String;
				if (description) { 
					for (var i:int = 0; i < length; i++) {
						var centry:Object = getItemAt(i);
						if (centry.id == event.context.contextId) {
							centry.description = description;
							centry.lastDatetime = new Date();
							var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, CollectionEventKind.REPLACE, i);
							dispatchEvent(ce);
							break;
						}
					}
					delete _pendingDescription[event.context.conversation];
				}
			}
		}

		
		public override function get length():int {
			if (!_initialized) {
				_initialized = true;
				internalRefreshList();
			}
			return super.length;
		} 
		
		public override function getItemAt(index:int, prefetch:int = 0):Object {
			if (!_initialized) {
				_initialized = true;
				internalRefreshList();
			}
			return super.getItemAt(index, prefetch);
		}
		
		
		public function get meta_name():String {
		    return _componentName;
		}
		
		public function meta_clear():void {
			_initialized = true;	// Avoid triggering remote refresh on destruction
			removeAll();
			_pendingDescription = new Dictionary(true);
			_initialized = false;
		}
		
		public function destroy():void {
			_initialized = true;	// Avoid triggering remote refresh on destruction
			removeAll();
			_pendingDescription = new Dictionary(true);
			_initialized = false;
		}
	}
}
