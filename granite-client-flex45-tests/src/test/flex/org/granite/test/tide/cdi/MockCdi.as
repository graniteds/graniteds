/*
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
package org.granite.test.tide.cdi
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.cdi.Cdi;
    import org.granite.tide.cdi.CdiOperation;
    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.service.ServerSession;

    public class MockCdi extends Cdi
    {
        public var token:MockCdiAsyncToken;
        
        public function MockCdi(destination:String = null) {
            super(destination);
        }

        protected override function initServerSession(destination:String):ServerSession {
            return new MockCdiServerSession(destination);
        }
        
		public static function getInstance():MockCdi {
			var tide:Tide = Tide.getInstance("cdi", MockCdi);
			if (!(tide is MockCdi)) {
				Tide.resetInstance();
				tide = Tide.getInstance("cdi", MockCdi);
			}
			return tide as MockCdi;
		}
		
		public static function reset():void {
		    Tide.resetInstance();
		}
    }
}



import mx.rpc.remoting.mxml.RemoteObject;
import org.granite.tide.cdi.CdiOperation;
import mx.rpc.AsyncToken;
import org.granite.test.tide.cdi.MockCdi;
import org.granite.test.tide.cdi.MockCdiAsyncToken;
import org.granite.tide.cdi.CdiServerSession;
import org.granite.tide.rpc.TideOperation;
import org.granite.tide.service.ServerSession;

class MockCdiServerSession extends CdiServerSession {

    public function MockCdiServerSession(destination:String):void {
        super(destination);
    }

    public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
        return new MockCdiOperation(this, name);
    }
}

class MockCdiOperation extends CdiOperation {
    
    private var _name:String = null;
    
    public function MockCdiOperation(serverSession:ServerSession, name:String):void {
        super(serverSession);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockCdiAsyncToken = MockCdi.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
