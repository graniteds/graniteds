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
package org.granite.test.tide.spring {

    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.spring.SpringServerSession;

    public class MockSpringServerSession extends SpringServerSession {

        public var token:Object = null;

        public function MockSpringServerSession(token:Object = null):void {
            super();
            this.token = token;
        }

        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
            return new MockSpringOperation(this, name);
        }
    }
}

import mx.rpc.AsyncToken;

import org.granite.test.tide.spring.MockSpringServerSession;

import org.granite.tide.rpc.TideOperation;
import org.granite.tide.service.ServerSession;

class MockSpringOperation extends TideOperation {

    private var _name:String = null;

    public function MockSpringOperation(serverSession:ServerSession, name:String):void {
        super(serverSession);
        _name = name;
    }

    public override function send(... args:Array):AsyncToken {
        var token:AsyncToken = null;
        if (MockSpringServerSession(_serverSession).token is AsyncToken)
            token = AsyncToken(MockSpringServerSession(_serverSession).token);
        else if (MockSpringServerSession(_serverSession).token is Class) {
            var tokenClass:Class = Class(MockSpringServerSession(_serverSession).token);
            token = new tokenClass() as AsyncToken;
        }
        token.send(_name, args);
        return token;
    }
}
