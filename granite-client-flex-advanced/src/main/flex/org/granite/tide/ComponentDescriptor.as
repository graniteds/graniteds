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
