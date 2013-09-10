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

package org.granite.tide.invocation {
    
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;


    [ExcludeClass]
    [RemoteClass(alias="org.granite.tide.invocation.ContextEvent")]
    /**
     * @author William DRAI
     */
    public class ContextEvent {
        
        private var _eventType:String;
        private var _params:Array;
        
        
        public function ContextEvent(eventType:String = null, params:Array = null) {
            super();
            _eventType = eventType;
            _params = params;
        }
        
        
        public function get eventType():String {
            return _eventType;
        }
        public function set eventType(eventType:String):void {
            _eventType = eventType;
        }
        
        public function get params():Array {
            return _params;
        }
        public function set params(params:Array):void {
            _params = params;
        }
        
        
        public function toString():String {
            return _eventType;
        }
    }
}