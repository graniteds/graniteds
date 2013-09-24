/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.spring
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	import mx.events.CollectionEvent;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.flexunit.async.AsyncHandler;
	import org.granite.test.tide.Person;
	import org.granite.tide.BaseContext;
	import org.granite.tide.collections.PagedCollection;
	import org.granite.tide.spring.PagedQuery;
    
    
    public class TestSpringClientPagedQuery 
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().tokenClass = MockSimpleCallAsyncToken;
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient", PagedQuery, { maxResults: 20 });
        }
        
        
        [Test(async)]
        public function testSpringPagedQuery():void {
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

		
		private var _get1ResultHandler:Function = null;
		private static var _findCount:int = 0;

		[Test(async)]
		public function testSpringPagedQueryInitialFindGDS1058():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			_get1ResultHandler = Async.asyncHandler(this, get1Result, 1000, null, get1Timeout);
			pagedQuery.addEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _get1ResultHandler);			
			
			_findCount = 0;
			pagedQuery.refresh();
			pagedQuery.getItemAt(0);
		}
		
		private function get1Result(event:Object, pass:Object = null):void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			_findCount++;
			
			if (_findCount > 1)
				Assert.assertFalse("Initial find should not be called twice", true);
			
			pagedQuery.removeEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _get1ResultHandler);
			_get1ResultHandler = Async.asyncHandler(this, get1Result, 1000, null, get1Timeout);
			pagedQuery.addEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _get1ResultHandler);
		}
		
		private function get1Timeout(event:Object, pass:Object = null):void {
			Assert.assertEquals("Initial find called once", 1, _findCount);
		}

			
		[Test(async)]
		public function testSpringPagedQueryRefresh():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			pagedQuery.methodName = "find2";
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, initialResultHandler);
			pagedQuery.fullRefresh();
		}
		
		private function initialResultHandler(event:Object, pass:Object = null):void {
			for (var i:int = 0; i < 20; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient.getItemAt(i).id);
			
			_ctx.pagedQueryClient.getItemAt(10).lastName = "test";
			
			Async.handleEvent(this, _ctx.pagedQueryClient, PagedCollection.COLLECTION_PAGE_CHANGE, secondResultHandler);
			_ctx.pagedQueryClient.refresh();
		}
				
		private function secondResultHandler(event:Object, pass:Object = null):void {
			for (var i:int = 0; i < 10; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient.getItemAt(i).id);
			for (var j:int = 10; j < 20; j++)
				Assert.assertEquals("Elt " + j, j+2, _ctx.pagedQueryClient.getItemAt(j).id);
		}
		
		[Test(async)]
		public function testSpringPagedQueryRefresh2():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			pagedQuery.methodName = "find3";
			
			Async.handleEvent(this, pagedQuery, PagedCollection.COLLECTION_PAGE_CHANGE, initialResult2Handler);
			pagedQuery.fullRefresh();
		}
		
		private function initialResult2Handler(event:Object, pass:Object = null):void {
			for (var i:int = 0; i < 10; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient.getItemAt(i).id);
			
			_ctx.pagedQueryClient.getItemAt(5).lastName = "test";
			
			Async.handleEvent(this, _ctx.pagedQueryClient, PagedCollection.COLLECTION_PAGE_CHANGE, secondResult2Handler);
			_ctx.pagedQueryClient.refresh();
		}
		
		private function secondResult2Handler(event:Object, pass:Object = null):void {
			for (var i:int = 0; i < 5; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient.getItemAt(i).id);
			for (var j:int = 5; j < 9; j++)
				Assert.assertEquals("Elt " + j, j+2, _ctx.pagedQueryClient.getItemAt(j).id);
		}
		
	}
}


import flash.events.TimerEvent;
import flash.utils.Timer;

import mx.collections.ArrayCollection;
import mx.messaging.messages.AcknowledgeMessage;
import mx.messaging.messages.ErrorMessage;
import mx.messaging.messages.IMessage;
import mx.rpc.AsyncToken;
import mx.rpc.Fault;
import mx.rpc.IResponder;
import mx.rpc.events.AbstractEvent;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import org.granite.test.tide.Person;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
	
	private static var count2:int = 0;
	private static var count3:int = 0;
	
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		var coll:ArrayCollection, first:int, max:int, i:int, person:Person;
		
        if (componentName == "pagedQueryClient" && op == "find") {
			coll = new ArrayCollection();
			first = params[1];
			max = params[2];
        	if (max == 0)
        		max = 20;
        	for (i = first; i < first+max; i++) {
        		person = new Person();
        		person.id = i;
        		person.lastName = "Person" + i;
        		coll.addItem(person);
        	}
            return buildResult({ resultCount: 1000, resultList: coll });
        }
		else if (componentName == "pagedQueryClient" && op == "find2") {
			coll = new ArrayCollection();
			first = params[1];
			max = params[2];
			if (max == 0)
				max = 20;
			for (i = first; i < first+max; i++) {
				person = new Person();
				if (count2 > 0 && i >= 10)
					person.id = i+2;
				else
					person.id = i+1;
				person.uid = "P" + person.id;
				person.version = 0;
				person.lastName = "Person" + i;
				coll.addItem(person);
			}
			count2++;
			return buildResult({ resultCount: 1000, resultList: coll });
		}
		else if (componentName == "pagedQueryClient" && op == "find3") {
			coll = new ArrayCollection();
			first = params[1];
			max = params[2];
			if (max == 0)
				max = 20;
			var size:int = count3 == 0 ? 10 : 9;
			for (i = 0; i < size; i++) {
				person = new Person();
				if (count3 > 0 && i >= 5)
					person.id = i+2;
				else
					person.id = i+1;
				person.uid = "P" + person.id;
				person.version = 0;
				person.lastName = "Person" + i;
				coll.addItem(person);
			}
			count3++;
			return buildResult({ resultCount: size, resultList: coll });
		}
        
        return buildFault("Server.Error");
    }
}
