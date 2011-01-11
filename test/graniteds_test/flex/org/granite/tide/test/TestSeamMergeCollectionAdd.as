package org.granite.tide.test
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.rpc.Fault;
    
    import org.granite.tide.TideResponder;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.seam.Context;
    
    
    public class TestSeamMergeCollectionAdd extends TestCase
    {
        public function TestSeamMergeCollectionAdd() {
            super("testMergeCollectionAdd");
        }
        
        private var _ctx:Context;
        
        
        private var _list:IList;
        
        public override function setUp():void {
            super.setUp();
            
            MockSeam.reset();
            _ctx = MockSeam.getInstance().getSeamContext();
            MockSeam.getInstance().token = new MockMergeCollectionChangeAsyncToken();
        }
        
        
        public function testMergeCollectionAdd():void {
            _ctx.users.find(2, addAsync(findResult, 1000));
        }
        
        private var _add:int = 0;
        
        private function findResult(event:TideResultEvent):void {
            _list = event.result as ArrayCollection;
            _ctx.userList = _list;
            assertEquals(_list.length, 2);
            
            _ctx.userList.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangeHandler);
            _ctx.users.find(3, new TideResponder(addAsync(findResult2, 1000), null, null, _list));
        }
        
        private function collectionChangeHandler(event:CollectionEvent):void {
            if (event.kind == CollectionEventKind.ADD)
                _add++;
        }
        
        private function findResult2(event:TideResultEvent):void {
            _ctx.userList = event.result as ArrayCollection;
            
            assertEquals(3, _ctx.userList.length);
            assertEquals(1, _add);
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


class MockMergeCollectionChangeAsyncToken extends MockSeamAsyncToken {
    
    private var _array:Array;
    
    function MockMergeCollectionChangeAsyncToken() {
        super(null);
        
        _array = new Array();
        for (var i:int = 0; i < 3; i++) {
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
            for (var i:int = 0; i < (params[0] as int); i++) {
                var u:User = new User();
                u.username = _array[i].username;
                list.addItem(u);
            }
            return buildResult(list, null);
        }
        
        return buildFault("Server.Error");
    }
}
