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
	public class RemoteList extends ArrayCollection implements IComponent {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.framework.RemoteList");
		
		
	    private var _componentName:String = null;
        private var _context:BaseContext = null;
        
        private var _initialized:Boolean = false;
        
		
		public function RemoteList() {
		    log.debug("create RemoteList");
			
			super();
		}
		
		public function meta_init(componentName:String, context:BaseContext):void {			
			_componentName = componentName;
			_context = context;
		}
		
		public function refreshList(event:TideContextEvent):void {
			internalRefreshList();
		}
		
		public function loginHandler(event:TideContextEvent):void {
			internalRefreshList();
		}
		
		private function internalRefreshList():void {
			_context.meta_addResult(_componentName, null);
			_context.meta_resync(refreshListResult, refreshListFault);
		}
		
		protected function refreshListResult(event:TideResultEvent):void {
		}
		
		protected function refreshListFault(event:TideFaultEvent):void {
			log.error("Could not get remote list: " + event);
		}
		
		public override function refresh():Boolean {
			_initialized = false;
			return super.refresh();
		}
		
		[Bindable("collectionChange")]
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
			_initialized = false;
		}
		
		public function destroy():void {
			_initialized = true;	// Avoid triggering remote refresh on destruction
			removeAll();
			_initialized = false;
		}
	}
}
