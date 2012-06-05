package org.granite.test.tide.spring
{
	import flash.events.Event;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.core.FlexGlobals;
	import mx.core.ScrollPolicy;
	import mx.core.UIComponent;
	import mx.events.ChildExistenceChangedEvent;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	import mx.events.FlexEvent;
	import mx.logging.Log;
	import mx.logging.targets.TraceTarget;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.fluint.uiImpersonation.UIImpersonator;
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.collections.PagedCollection;
	
	import spark.components.SkinnableContainer;
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
        
		private var _asyncHandler:Function = null;
		
		[Test(async)]
        public function testSpringComponentRestrict():void {
			var login:Login = new Login();
			_main.addElement(login);
			_ctx.main = _main;
			FlexGlobals.topLevelApplication.addElement(_main);
			
			_asyncHandler = Async.asyncHandler(this, loginHandler, 1000);
			_main.addEventListener(ElementExistenceEvent.ELEMENT_ADD, _asyncHandler); 
			_ctx.identity.login("test", "test");
        }
		
		private function loginHandler(event:Object, pass:Object = null):void {
			_main.removeEventListener(ElementExistenceEvent.ELEMENT_ADD, _asyncHandler); 
			
			var home:Home = _main.getElementAt(0) as Home;
			_refs[home.dgWelcomes] = true;	// Stores a weak reference to the DataGrid to check later if it has been removed from memory
			
			_asyncHandler = Async.asyncHandler(this, refreshHandler, 1000);
			_ctx.allWelcomes.addEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
		}
		
		private function refreshHandler(event:Object, pass:Object = null):void {
			_ctx.allWelcomes.removeEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
			if (event.kind == CollectionEventKind.RESET) {
				var home:Home = _main.getElementAt(0) as Home;
				home.dgWelcomes.invalidateDisplayList();
				Async.delayCall(this, scrollHandler, 50);
			}
			else {
				_asyncHandler = Async.asyncHandler(this, refreshHandler, 1000);
				_ctx.allWelcomes.addEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
			}
		}
		
		private function scrollHandler(event:Object = null, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			var test:Object = this;
			Async.delayCall(this, function(pass:Object = null):void {
				_asyncHandler = Async.asyncHandler(test, pageChangeHandler, 1000);
				_ctx.allWelcomes.addEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _asyncHandler);
				home.dgWelcomes.validateNow();
				home.dgWelcomes.scrollToIndex(20);
			}, 200);
		}
		
		private function pageChangeHandler(event:Object, pass:Object = null):void {
			_ctx.allWelcomes.removeEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _asyncHandler);
			
			Assert.assertEquals("Grid scrolled", 20, _ctx.allWelcomes.getItemAt(20).id);
			
			_asyncHandler = Async.asyncHandler(this, logoutHandler, 1000);
			Tide.getInstance().addEventListener("org.granite.tide.logout", _asyncHandler);
			_ctx.identity.logout();
		}
		
		private function logoutHandler(event:Object, pass:Object = null):void {
			Tide.getInstance().removeEventListener("org.granite.tide.logout", _asyncHandler);
			Async.delayCall(this, relogin, 200);
		}
		
		private function relogin(pass:Object = null):void {
			Assert.assertNull("Home cleared from context", _ctx.meta_getInstance("home", false, true));
			
			_asyncHandler = Async.asyncHandler(this, login2Handler, 1000);
			_main.addEventListener(ElementExistenceEvent.ELEMENT_ADD, _asyncHandler); 
			_ctx.identity.login("test", "test");
		}
		
		private function login2Handler(event:Object, pass:Object = null):void {
			_main.removeEventListener(ElementExistenceEvent.ELEMENT_ADD, _asyncHandler);
			
			var home:Home = _main.getElementAt(0) as Home;
			for (var obj:Object in _refs) {
				if (obj.dataProvider === _ctx.allWelcomes && home.dgWelcomes.dataProvider === _ctx.allWelcomes)
					Assert.fail("Multiple dataProvider references to same PagedQuery");
			}
			
			_asyncHandler = Async.asyncHandler(this, refresh2Handler, 1000);
			_ctx.allWelcomes.addEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
		}
		
		private function refresh2Handler(event:Object, pass:Object = null):void {
			_ctx.allWelcomes.removeEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
			if (event.kind == CollectionEventKind.RESET) {
				var home:Home = _main.getElementAt(0) as Home;
				home.dgWelcomes.invalidateDisplayList();
				Async.delayCall(this, scroll2Handler, 50);
			}
			else {
				_asyncHandler = Async.asyncHandler(this, refresh2Handler, 1000);
				_ctx.allWelcomes.addEventListener(CollectionEvent.COLLECTION_CHANGE, _asyncHandler);
			}
		}
		
		private function scroll2Handler(event:Object = null, pass:Object = null):void {
			var home:Home = _main.getElementAt(0) as Home;
			var test:Object = this;
			Async.delayCall(this, function(pass:Object = null):void {
				_asyncHandler = Async.asyncHandler(test, pageChange2Handler, 1000);
				_ctx.allWelcomes.addEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _asyncHandler);
				home.dgWelcomes.validateNow();
				home.dgWelcomes.scrollToIndex(100);
			}, 200);
		}
		
		private function pageChange2Handler(event:Object, pass:Object = null):void {
			_ctx.allWelcomes.removeEventListener(PagedCollection.COLLECTION_PAGE_CHANGE, _asyncHandler);
			_asyncHandler = null;
			
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
		else if (componentName == null && op == "list") {
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
				w.message "M" + i;
				coll.addItem(w);
			}
			var result:Object = { firstResult: first, maxResults: max, resultList: coll, resultCount: 1000 };
			return buildResult(result, []);
		}
        
        return buildFault("Server.Error");
    }
}
