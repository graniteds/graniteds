package org.granite.test.tide.spring
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
	import org.granite.tide.collections.PagedCollection;
	import org.granite.tide.spring.PagedQuery;
    
    
    public class TestSpringPagedQueryRefresh 
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient", PagedQuery, { elementClass: Person, maxResults: 75 });
        }
        
        
        [Test(async)]
        public function testSpringPagedQueryRefresh():void {
        	var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, refreshHandler, 1000, 1);
        	pagedQuery.fullRefresh();
        }
        
        private function refreshHandler(event:Object, pass:Object = null):void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			
			var count:Number = pass as Number;
			if (count < 100) {
				Async.delayCall(this, function():void { 
					triggerRefresh(pagedQuery, count);
				}, 1000);
				return;
			}
			
			// Check entityManager size
			var statistics:Object = _ctx.meta_getEntityManagerStatistics();
			Assert.assertTrue("Entities managed", statistics.cacheSize >= 75);
			Assert.assertEquals("No reference", 0, statistics.referenceSize);
        }		
		
		private function triggerRefresh(pagedQuery:PagedQuery, count:Number):void {
			var person:Person = new Person();
			person.id = count;
			person.version = 0;
			person.uid = "P" + count;
			person.lastName = "Person" + count;
			
			var updates:Array = [ [ 'PERSIST', person ] ];
			_ctx.meta_handleUpdates(null, updates);
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, refreshHandler, 1000, count+1);
			
			_ctx.meta_handleUpdateEvents(updates);
		}
		
		
		private var _refreshCount:int = 0;
		
		[Test(async)]
		public function testSpringPagedQueryRefresh2():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			pagedQuery.refreshFilter = function(objects:Array):Boolean {
				for each (var obj:Object in objects) {
					if (obj.id % 2 == 0)
						return false;
				}
				return true;
			};
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, refresh2Handler, 1000, 1);
			pagedQuery.fullRefresh();
		}
		
		private function refresh2Handler(event:Object, count:Number = NaN):void {
			_refreshCount++;
			
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			
			if (count > 2) {
				Assert.fail("Too many refreshes");
				return;
			}
			
			Async.delayCall(this, function():void { 
				triggerRefresh2(pagedQuery, count);
			}, 1000);
		}
		
		private function timeout2Handler(count:Number = NaN):void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			
			// Check entityManager size
			var statistics:Object = _ctx.meta_getEntityManagerStatistics();
			Assert.assertEquals("No reference", 0, statistics.referenceSize);
			Assert.assertEquals("Refresh count", 2, _refreshCount);
		}
		
		private function triggerRefresh2(pagedQuery:PagedQuery, count:Number):void {
			var person:Person = new Person();
			person.id = count;
			person.version = 0;
			person.uid = "P" + count;
			person.lastName = "Person" + count;
			
			var updates:Array = [ [ 'PERSIST', person ] ];
			_ctx.meta_handleUpdates(null, updates);
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, refresh2Handler, 1000, count+1, timeout2Handler);
			
			_ctx.meta_handleUpdateEvents(updates);
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
import org.granite.test.tide.Person;
import org.granite.test.tide.spring.MockSpringAsyncToken;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
	
	private static var count:int = 1;
	
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		var coll:ArrayCollection, first:int, max:int, i:int, person:Person;
		
        if (componentName == "pagedQueryClient" && op == "find") {
			coll = new ArrayCollection();
			first = params[1];
			max = params[2];
        	if (max == 0)
        		max = 75;
        	for (i = 1; i <= count; i++) {
        		person = new Person();
        		person.id = i;
				person.version = 0;
				person.uid = "P" + i;
        		person.lastName = "Person" + i;
        		coll.addItem(person);
        	}
			if (count < 75) 
				count++;
            return buildResult({ resultCount: count, resultList: coll });
        }
        
        return buildFault("Server.Error");
    }
}
