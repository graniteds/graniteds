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

package org.granite.tide.data.events {

    import flash.events.Event;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.data.Conflicts;


    public class DirtyChangeEvent extends Event {
    	
    	public static const DIRTY_CHANGE:String = "dirtyChange";
        
		public var object:Object;
        public var dirty:Boolean;
        

        public function DirtyChangeEvent(object:Object, dirty:Boolean):void {
            super(DIRTY_CHANGE, false, false);
			this.object = object;
            this.dirty = dirty;
        }
    }
}