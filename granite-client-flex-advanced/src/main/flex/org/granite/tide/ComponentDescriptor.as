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
    
    import org.granite.tide.impl.ComponentFactory;
    

    /**
     * 	ComponentDescriptor contains the component definition
     *
     * 	@author William DRAI
     */
    [ExcludeClass]
    public class ComponentDescriptor {
        public var name:String;
		public var autoUIName:Boolean = false;
        public var factory:ComponentFactory;
        public var xmlDescriptor:XML;
		public var types:Array = [];
        public var proxy:Boolean = false;
        public var global:Boolean = false;
        public var scope:int = Tide.SCOPE_UNKNOWN;
        public var restrict:int = Tide.RESTRICT_UNKNOWN;
        public var autoCreate:Boolean = true;
        public var remoteSync:int = Tide.SYNC_NONE;
        public var postConstructMethodName:String = null;
        public var destroyMethodName:String = null;
		public var events:Array = [];
    }
}
