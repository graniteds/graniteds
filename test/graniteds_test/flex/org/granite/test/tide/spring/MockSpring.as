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
