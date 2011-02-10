package org.granite.test.tide.cdi
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.cdi.Cdi;
    import org.granite.tide.cdi.CdiOperation;
    import mx.rpc.remoting.mxml.RemoteObject;
    
    
    public class MockCdi extends Cdi
    {
        public var token:MockCdiAsyncToken;
        
        public function MockCdi(destination:String = null) {
            super(destination);
        }
        
        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
	        return new MockCdiOperation(this, name);
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
import mx.rpc.AbstractOperation;
import org.granite.tide.Tide;
import org.granite.tide.cdi.CdiOperation;
import mx.rpc.AsyncToken;
import org.granite.test.tide.cdi.MockCdi;
import org.granite.test.tide.cdi.MockCdiAsyncToken;

class MockCdiOperation extends CdiOperation {
    
    private var _name:String = null;
    
    public function MockCdiOperation(tide:Tide, name:String):void {
        super(tide);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockCdiAsyncToken = MockCdi.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
