/**
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
