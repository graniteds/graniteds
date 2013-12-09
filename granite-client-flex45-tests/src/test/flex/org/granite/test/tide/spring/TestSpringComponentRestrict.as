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
	import flash.utils.Dictionary;
	
	import mx.core.FlexGlobals;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.logging.Log;
	import mx.logging.targets.TraceTarget;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.collections.PagedCollection;
	
	import spark.events.ElementExistenceEvent;
    
    
    public class TestSpringComponentRestrict 
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
			var t:TraceTarget = new TraceTarget();
			t.filters = ["org.granite.*"];
			Log.addTarget(t);
			
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().token = new MockSimpleCallAsyncToken();
			MockSpring.getInstance().initApplication();
        }
		
		[After]
		public function tearDown():void {
			if (_main != null) {
				FlexGlobals.topLevelApplication.removeElement(_main);
				_main = null;
			}
		}
        
		private var _main:Main = new Main();
		private var _refs:Dictionary = new Dictionary(true);
        
		[Test(async)]
        public function testSpringComponentRestrict():void {
			var login:Login = new Login();
			_main.addElement(login);
			_ctx.main = _main;
			FlexGlobals.topLevelApplication.addElement(_main);

            Async.handleEvent(this, _main, ElementExistenceEvent.ELEMENT_ADD, loginHandler, 1000);
			_ctx.identity.login("test", "test");
        }
		
		private function loginHandler(event:Object, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			_refs[home.dgWelcomes] = true;	// Stores a weak reference to the DataGrid to check later if it has been removed from memory

            Async.handleEvent(this, _ctx.allWelcomes, CollectionEvent.COLLECTION_CHANGE, refreshHandler, 1000);
		}
		
		private function refreshHandler(event:Object, pass:Object = null):void {
			if (event.kind == CollectionEventKind.RESET) {
				var home:Home = _main.getElementAt(0) as Home;
				home.dgWelcomes.invalidateDisplayList();
				Async.delayCall(this, scrollHandler, 50);
			}
			else {
                Async.handleEvent(this, _ctx.allWelcomes, CollectionEvent.COLLECTION_CHANGE, refreshHandler, 1000);
			}
		}
		
		private function scrollHandler(event:Object = null, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			var test:Object = this;
			Async.delayCall(this, function(pass:Object = null):void {
                Async.handleEvent(test, _ctx.allWelcomes, PagedCollection.COLLECTION_PAGE_CHANGE, pageChangeHandler, 1000);
				home.dgWelcomes.validateNow();
				home.dgWelcomes.scrollToIndex(20);
			}, 200);
		}
		
		private function pageChangeHandler(event:Object, pass:Object = null):void {
			Assert.assertEquals("Grid scrolled", 20, _ctx.allWelcomes.getItemAt(20).id);

            Async.handleEvent(this, Tide.getInstance(), Tide.LOGOUT, logoutHandler, 1000);
			_ctx.identity.logout();
		}
		
		private function logoutHandler(event:Object, pass:Object = null):void {
			Async.delayCall(this, relogin, 200);
		}
		
		private function relogin(pass:Object = null):void {
			Assert.assertNull("Home cleared from context", _ctx.meta_getInstance("home", false, true));

            Async.handleEvent(this, _main, ElementExistenceEvent.ELEMENT_ADD, login2Handler, 1000);
			_ctx.identity.login("test", "test");
		}
		
		private function login2Handler(event:Object, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			for (var obj:Object in _refs) {
				if (obj.dataProvider === _ctx.allWelcomes && home.dgWelcomes.dataProvider === _ctx.allWelcomes)
					Assert.fail("Multiple dataProvider references to same PagedQuery");
			}

            Async.handleEvent(this, _ctx.allWelcomes, CollectionEvent.COLLECTION_CHANGE, refresh2Handler, 1000);
		}
		
		private function refresh2Handler(event:Object, pass:Object = null):void {
			if (event.kind == CollectionEventKind.RESET) {
				var home:Home = _main.getElementAt(0) as Home;
				home.dgWelcomes.invalidateDisplayList();
				Async.delayCall(this, scroll2Handler, 50);
			}
			else {
                Async.handleEvent(this, _ctx.allWelcomes, CollectionEvent.COLLECTION_CHANGE, refresh2Handler, 1000);
			}
		}
		
		private function scroll2Handler(event:Object = null, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			var test:Object = this;
			Async.delayCall(this, function(pass:Object = null):void {
                Async.handleEvent(test, _ctx.allWelcomes, PagedCollection.COLLECTION_PAGE_CHANGE, pageChange2Handler, 1000);
				home.dgWelcomes.validateNow();
				home.dgWelcomes.scrollToIndex(100);
			}, 200);
		}
		
		private function pageChange2Handler(event:Object, pass:Object = null):void {
			Assert.assertEquals("Grid scrolled 2", 100, _ctx.allWelcomes.getItemAt(100).id);
		}
    }
}


import mx.collections.ArrayCollection;
import mx.rpc.events.AbstractEvent;

import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.test.tide.spring.Welcome;
import org.granite.tide.invocation.InvocationCall;


class MockSimpleCallAsyncToken extends MockSpringAsyncToken {
    
    function MockSimpleCallAsyncToken() {
        super(null);
    }
    
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
        if (componentName == "identity" && op == "login") {
            return buildResult(true, []);
        }
		else if (op == "list") {
			var first:int = params[1];
			var max:int = params[2];
			var coll:ArrayCollection = new ArrayCollection();
			for (var i:int = first; i < first+max; i++) {
				if (i >= 1000)
					break;
				var w:Welcome = new Welcome();
				w.id = i;
				w.version = 0;
				w.uid = "W" + i;
				w.name = "W" + i;
				w.message = "M" + i;
				coll.addItem(w);
			}
			var result:Object = { firstResult: first, maxResults: max, resultList: coll, resultCount: 1000 };
			return buildResult(result, []);
		}
        
        return buildFault("Server.Error");
    }
}
