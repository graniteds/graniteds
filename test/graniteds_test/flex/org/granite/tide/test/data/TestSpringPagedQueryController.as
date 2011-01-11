package org.granite.tide.test.data
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import flexunit.framework.TestCase;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.spring.PagedQuery;
	import org.granite.tide.test.MockSpring;
	import org.granite.tide.test.Person;
    
    
    public class TestSpringPagedQueryController extends TestCase
    {
        public function TestSpringPagedQueryController() {
            super("testSpringPagedQueryController");
        }
        
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient", PagedQuery, { maxResults: 20, useController: true });
        }
        
        
        public function testSpringPagedQueryController():void {
        	var timer:Timer = new Timer(500);
        	timer.addEventListener(TimerEvent.TIMER, addAsync(get0Result, 1000), false, 0, true);
        	timer.start();
        	
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	pagedQuery.fullRefresh();
        }
        
        private function getFault(fault:Object, token:Object = null):void {
        }
        
        private var cont20:Function;
        
        private function get0Result(event:Object):void {
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	var person:Person = pagedQuery.getItemAt(0) as Person;
        	assertEquals("Person id", 0, person.id);
        	
        	var ipe:Boolean = false;
        	try {
        		person = pagedQuery.getItemAt(20) as Person;
        	}
        	catch (e:ItemPendingError) {
        		e.addResponder(new ItemResponder(get20Result, getFault));
        		ipe = true;
        	}
        	assertTrue("IPE thrown 20", ipe);
        	cont20 = addAsync(continue20, 1000);
        }
        
        private function get20Result(result:Object, token:Object = null):void {
        	cont20(result);
        }
        
        private function continue20(event:Object):void {
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
        	var person:Person = pagedQuery.getItemAt(20) as Person;
        	assertEquals("Person id", 20, person.id);
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
import org.granite.tide.test.MockSpringAsyncToken;
import org.granite.tide.test.Person;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "pagedQueryClient" && op == "find") {
        	var coll:ArrayCollection = new ArrayCollection();
        	var first:int = params[0].first;
        	var max:int = params[0].max;
        	if (max == 0)
        		max = 20;
        	for (var i:int = first; i < first+max; i++) {
        		var person:Person = new Person();
        		person.id = i;
        		person.lastName = "Person" + i;
        		coll.addItem(person);
        	}
            return buildResult(null, [[ "pagedQueryClient.resultCount", 1000 ], [ "pagedQueryClient.resultList", coll ]]);
        }
        
        return buildFault("Server.Error");
    }
}
