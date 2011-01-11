package org.granite.tide.test
{
    import org.granite.tide.Tide;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.seam.Seam;
    import org.granite.tide.seam.SeamOperation;
    import mx.rpc.remoting.mxml.RemoteObject;
    
    
    public class MockSeam extends Seam
    {
        public var token:MockSeamAsyncToken;
        
        public function MockSeam(destination:String = null) {
            super(destination);
        }
        
        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
	        return new MockSeamOperation(this, name);
        } 
        
		public static function getInstance():MockSeam {
			var tide:Tide = Tide.getInstance("seam", MockSeam);
			if (!(tide is MockSeam)) {
				Tide.resetInstance();
				tide = Tide.getInstance("seam", MockSeam);
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
import org.granite.tide.test.MockSeamAsyncToken;
import org.granite.tide.Tide;
import org.granite.tide.test.MockSeam;
import org.granite.tide.seam.SeamOperation;
import mx.rpc.AsyncToken;

class MockSeamOperation extends SeamOperation {
    
    private var _name:String = null;
    
    public function MockSeamOperation(tide:Tide, name:String):void {
        super(tide);
        _name = name;
    }
    
    public override function send(... args:Array):AsyncToken {
        var token:MockSeamAsyncToken = MockSeam.getInstance().token;
        token.send(_name, args);
        return token;
    }
}
