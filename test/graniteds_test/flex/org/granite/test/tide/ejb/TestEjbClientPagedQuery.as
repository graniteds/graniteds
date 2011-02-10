package org.granite.test.tide.ejb
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.test.tide.Person;
	import org.granite.tide.BaseContext;
	import org.granite.tide.collections.PagedQuery;
    
    
    public class TestEjbClientPagedQuery 
    {
        private var _ctx:BaseContext = MockEjb.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockEjb.reset();
            _ctx = MockEjb.getInstance().getEjbContext();
            MockEjb.getInstance().token = new MockSimpleCallAsyncToken();
            MockEjb.getInstance().addComponentWithFactory("pagedQueryClient", PagedQuery, { maxResults: 20 });
        }
        
        
        [Test(async)]
        public function testEjbPagedQuery():void {
        	var timer:Timer = new Timer(500);
        	timer.addEventListener(TimerEvent.TIMER, Async.asyncHandler(this, get0Result, 1000), false, 0, true);
        	timer.start();
        	
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	pagedQuery.fullRefresh();
        }
        
        private function getFault(fault:Object, token:Object = null):void {
        }
        
        private var cont20:Function;
        
        private function get0Result(event:Object, pass:Object = null):void {
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	var person:Person = pagedQuery.getItemAt(0) as Person;
        	Assert.assertEquals("Person id", 0, person.id);
        	
        	var ipe:Boolean = false;
        	try {
        		person = pagedQuery.getItemAt(20) as Person;
        	}
        	catch (e:ItemPendingError) {
        		e.addResponder(new ItemResponder(get20Result, getFault));
        		ipe = true;
        	}
        	Assert.assertTrue("IPE thrown 20", ipe);
        	cont20 = Async.asyncHandler(this, continue20, 1000);
        }
        
        private function get20Result(result:Object, token:Object = null):void {
        	cont20(result);
        }
        
        private function continue20(event:Object, pass:Object = null):void {
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	var person:Person = pagedQuery.getItemAt(20) as Person;
        	Assert.assertEquals("Person id", 20, person.id);
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
import org.granite.test.tide.ejb.MockEjbAsyncToken;
import org.granite.test.tide.Person;


class MockSimpleCallAsyncToken extends MockEjbAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "pagedQueryClient" && op == "find") {
        	var coll:ArrayCollection = new ArrayCollection();
        	var first:int = params[1];
        	var max:int = params[2];
        	if (max == 0)
        		max = 20;
        	for (var i:int = first; i < first+max; i++) {
        		var person:Person = new Person();
        		person.id = i;
        		person.lastName = "Person" + i;
        		coll.addItem(person);
        	}
            return buildResult({ resultCount: 1000, resultList: coll });
        }
        
        return buildFault("Server.Error");
    }
}
