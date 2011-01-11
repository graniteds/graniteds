package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.rpc.Fault;
    import mx.utils.StringUtil;
    
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamMergeCollectionOut extends TestCase
    {
        public function TestSeamMergeCollectionOut() {
            super("testMergeCollectionOut");
        }
        
        private var _ctx:Context;
        
        
        private var _list:IList;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockSimpleCallAsyncToken();
        }
        
        
        public function testMergeCollectionOut():void {
            _ctx.users.find(addAsync(findResult, 1000));
        }
        
        private function findResult(event:TideResultEvent):void {
            _list = event.context.userList as ArrayCollection;
            assertEquals(Object(MockSeam.getInstance().token).array.length, _list.length);
            for (var i:int = 0; i < _list.length; i++)
                assertEquals(Object(MockSeam.getInstance().token).array[i].uid, _list.getItemAt(i).uid);
            
            _ctx.users.find(addAsync(findResult2, 1000));
        }
        
        private function findResult2(event:TideResultEvent):void {
            assertEquals(_list, event.context.userList);
            for (var i:int = 0; i < _list.length; i++)
                assertEquals(Object(MockSeam.getInstance().token).array[i].uid, _ctx.userList.getItemAt(i).uid);
        }
    }
}


import flash.utils.Timer;
import flash.events.TimerEvent;
import mx.rpc.AsyncToken;
import mx.rpc.IResponder;
import mx.messaging.messages.IMessage;
import mx.messaging.messages.ErrorMessage;
import mx.rpc.Fault;
import mx.rpc.events.FaultEvent;
import mx.collections.ArrayCollection;
import mx.rpc.events.AbstractEvent;
import mx.rpc.events.ResultEvent;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.invocation.ContextUpdate;
import mx.messaging.messages.AcknowledgeMessage;
import org.granite.tide.test.MockSeamAsyncToken;
import org.granite.tide.test.User;


class MockSimpleCallAsyncToken extends MockSeamAsyncToken {
    
    private var _array:Array;
    
    function MockSimpleCallAsyncToken() {
        super(null);
        
        _array = new Array();
        for (var i:int = 0; i < 5; i++) {
            var u:User = new User();
            u.username = generateName();
            _array.push(u);
        }
    }
    
    private function generateName():String {
        var name:String = "";
        for (var i:int = 0; i < 10; i++)
            name += String.fromCharCode(32+96*Math.random());
        return name;
    }
    
    public function get array():Array {
        return _array;
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "users" && op == "find") {
            var list:ArrayCollection = new ArrayCollection();
            for each (var a:User in _array) {
                var u:User = new User();
                u.username = a.username;
                list.addItem(u);
            }
            return buildResult(list, [[ "userList", list ]]);
        }
        
        return buildFault("Server.Error");
    }
}
