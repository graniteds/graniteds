/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.spring
{
    import mx.rpc.remoting.mxml.RemoteObject;
    
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.spring.Spring;
    
    
    public class MockSpring extends Spring
    {
		public var token:MockSpringAsyncToken = null;
        public var tokenClass:Class = null;
        
        public function MockSpring(destination:String = null) {
            super(destination);
        }
        
        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
	        return new MockSpringOperation(this, name);
        } 
        
		public static function getInstance():MockSpring {
			var tide:Tide = Tide.getInstance("spring", MockSpring);
			if (!(tide is MockSpring)) {
				Tide.resetInstance();
				tide = Tide.getInstance("spring", MockSpring);
			}
			return tide as MockSpring;
		}
		
		public static function reset():void {
		    Tide.resetInstance();
		}
    }
}




import mx.rpc.AbstractOperation;
import mx.rpc.AsyncToken;
import mx.rpc.remoting.mxml.RemoteObject;

import org.granite.test.tide.spring.MockSpring;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.Tide;
import org.granite.tide.rpc.TideOperation;

class MockSpringOperation extends TideOperation {
    
    private var _name:String = null;
    
    public function MockSpringOperation(tide:Tide, name:String):void {
        super(tide);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
		var token:MockSpringAsyncToken = MockSpring.getInstance().token;
		if (token == null) {
			var tokenClass:Class = MockSpring.getInstance().tokenClass;
        	token = new tokenClass() as MockSpringAsyncToken;
		}
        token.send(_name, args);
        return token;
    }
}
