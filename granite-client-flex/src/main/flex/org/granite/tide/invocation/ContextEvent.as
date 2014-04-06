/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.tide.invocation {
    
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