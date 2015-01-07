/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.ejb
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.ejb.Ejb;
    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.service.ServerSession;

    public class MockEjb extends Ejb
    {
        public var token:MockEjbAsyncToken;
        
        public function MockEjb():void {
            super();
        }

        protected override function initServerSession():ServerSession {
            return new MockEjbServerSession();
        }

		public static function getInstance():MockEjb {
			var tide:Tide = Tide.getInstance("server", MockEjb);
			if (!(tide is MockEjb)) {
				Tide.resetInstance();
				tide = Tide.getInstance("server", MockEjb);
			}
			return tide as MockEjb;
		}
		
		public static function reset():void {
		    Tide.resetInstance();
		}
    }
}



import mx.rpc.remoting.mxml.RemoteObject;
import org.granite.test.tide.ejb.MockEjb;
import org.granite.test.tide.ejb.MockEjbAsyncToken;
import org.granite.tide.ejb.EjbServerSession;
import org.granite.tide.rpc.TideOperation;
import mx.rpc.AsyncToken;

import org.granite.tide.service.ServerSession;

class MockEjbServerSession extends EjbServerSession {

    public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
        return new MockEjbOperation(this, name);
    }
}

class MockEjbOperation extends TideOperation {
    
    private var _name:String = null;
    
    public function MockEjbOperation(serverSession:ServerSession, name:String):void {
        super(serverSession);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockEjbAsyncToken = MockEjb.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
