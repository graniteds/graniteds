package org.granite.test.tide.ejb
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.ejb.Ejb;
    import mx.rpc.remoting.mxml.RemoteObject;
    
    
    public class MockEjb extends Ejb
    {
        public var token:MockEjbAsyncToken;
        
        public function MockEjb(destination:String = null) {
            super(destination);
        }
        
        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
	        return new MockEjbOperation(this, name);
        } 
        
		public static function getInstance():MockEjb {
			var tide:Tide = Tide.getInstance("ejb", MockEjb);
			if (!(tide is MockEjb)) {
				Tide.resetInstance();
				tide = Tide.getInstance("ejb", MockEjb);
			}
			return tide as MockEjb;
		}
		
		public static function reset():void {
		    Tide.resetInstance();
		}
    }
}



import mx.rpc.remoting.mxml.RemoteObject;
import mx.rpc.AbstractOperation;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.test.tide.ejb.MockEjb;
import org.granite.test.tide.ejb.MockEjbAsyncToken;
import org.granite.tide.Tide;
import org.granite.tide.rpc.TideOperation;
import mx.rpc.AsyncToken;

class MockEjbOperation extends TideOperation {
    
    private var _name:String = null;
    
    public function MockEjbOperation(tide:Tide, name:String):void {
        super(tide);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockEjbAsyncToken = MockEjb.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
