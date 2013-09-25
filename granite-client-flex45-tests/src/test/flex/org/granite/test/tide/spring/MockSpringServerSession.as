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
