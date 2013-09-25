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
package org.granite.test.tide.seam
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.seam.Seam;
    import org.granite.tide.seam.SeamOperation;
    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.service.ServerSession;

    public class MockSeam extends Seam
    {
        public var token:MockSeamAsyncToken;
        
        public function MockSeam() {
            super();
        }

        protected override function initServerSession():ServerSession {
            return new MockSeamServerSession();
        }
        
		public static function getInstance():MockSeam {
			var tide:Tide = Tide.getInstance(null, MockSeam);
			if (!(tide is MockSeam)) {
				Tide.resetInstance();
				tide = Tide.getInstance(null, MockSeam);
			}
			return tide as MockSeam;
		}
		
		public static function reset():void {
		    Tide.resetInstance();
		}
    }
}



import mx.rpc.remoting.mxml.RemoteObject;
import mx.rpc.AbstractOperation;
import org.granite.test.tide.seam.MockSeamAsyncToken;
import org.granite.tide.Tide;
import org.granite.test.tide.seam.MockSeam;
import org.granite.tide.rpc.TideOperation;
import org.granite.tide.seam.SeamOperation;
import mx.rpc.AsyncToken;

import org.granite.tide.seam.SeamServerSession;
import org.granite.tide.service.ServerSession;

class MockSeamServerSession extends SeamServerSession {

    public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
        return new MockSeamOperation(this, name);
    }
}

class MockSeamOperation extends SeamOperation {
    
    private var _name:String = null;
    
    public function MockSeamOperation(serverSession:ServerSession, name:String):void {
        super(serverSession);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockSeamAsyncToken = MockSeam.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
