package org.granite.test.tide.spring
{
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.ItemResponder;
	import mx.collections.errors.ItemPendingError;
	import mx.controls.AdvancedDataGrid;
	import mx.controls.DataGrid;
	import mx.controls.advancedDataGridClasses.AdvancedDataGridColumn;
	import mx.controls.dataGridClasses.DataGridColumn;
	import mx.events.CollectionEvent;
	import mx.events.CollectionEventKind;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;
	import org.flexunit.async.AsyncHandler;
	import org.fluint.uiImpersonation.UIImpersonator;
	import org.granite.test.tide.Person;
	import org.granite.tide.BaseContext;
	import org.granite.tide.collections.PagedCollection;
	import org.granite.tide.spring.PagedQuery;
    
    
    public class TestSpringClientPagedQueryUI 
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().tokenClass = MockSimpleCallAsyncToken;
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient", PagedQuery, { maxResults: 20, methodName: "find2" });
			MockSpring.getInstance().addComponentWithFactory("pagedQueryClient2", PagedQuery, { maxResults: 20, methodName: "find2" });
        }
        
        
		[Test(async)]
		public function testSpringPagedQueryUIRefresh():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient;
			
			var dataGrid:DataGrid = new DataGrid();
			dataGrid.height = dataGrid.rowHeight*12;
			dataGrid.dataProvider = pagedQuery;
			dataGrid.columns = [];
			dataGrid.columns[0] = new DataGridColumn("firstName");
			dataGrid.columns[1] = new DataGridColumn("lastName");
			
			UIImpersonator.removeAllChildren();
			UIImpersonator.addChild(dataGrid);
			
			Async.handleEvent(this, pagedQuery, CollectionEvent.COLLECTION_CHANGE, initialResultHandler);
		}
		
		private function initialResultHandler(event:Object, pass:Object = null):void {
			if (event.kind != CollectionEventKind.REFRESH)
				return;
			
			for (var i:int = 0; i < 20; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient.getItemAt(i).id);
			
			var dataGrid:DataGrid = UIImpersonator.getChildAt(0) as DataGrid;
			var object:Object = _ctx.pagedQueryClient.getItemAt(10);
			dataGrid.selectedItem = object;
			dataGrid.selectedItem.lastName = "test";
			
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
		public function testSpringPagedQueryUI2Refresh():void {
			var pagedQuery:PagedQuery = _ctx.pagedQueryClient2;
			
			var dataGrid:AdvancedDataGrid = new AdvancedDataGrid();
			dataGrid.height = dataGrid.rowHeight*12;
			dataGrid.dataProvider = pagedQuery;
			dataGrid.columns = [];
			dataGrid.columns[0] = new AdvancedDataGridColumn("firstName");
			dataGrid.columns[1] = new AdvancedDataGridColumn("lastName");
			
			UIImpersonator.removeAllChildren();
			UIImpersonator.addChild(dataGrid);
			
			Async.handleEvent(this, pagedQuery, CollectionEvent.COLLECTION_CHANGE, initialResult2Handler);
		}
		
		private function initialResult2Handler(event:Object, pass:Object = null):void {
			if (event.kind != CollectionEventKind.REFRESH)
				return;
			
			for (var i:int = 0; i < 20; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient2.getItemAt(i).id);
			
			var dataGrid:AdvancedDataGrid = UIImpersonator.getChildAt(0) as AdvancedDataGrid;
			var object:Object = _ctx.pagedQueryClient2.getItemAt(10);
			dataGrid.selectedItem = object;
			dataGrid.selectedItem.lastName = "test";
			
			Async.handleEvent(this, _ctx.pagedQueryClient2, PagedCollection.COLLECTION_PAGE_CHANGE, secondResult2Handler);
			_ctx.pagedQueryClient2.refresh();
		}
		
		private function secondResult2Handler(event:Object, pass:Object = null):void {
			for (var i:int = 0; i < 10; i++)
				Assert.assertEquals("Elt " + i, i+1, _ctx.pagedQueryClient2.getItemAt(i).id);
			for (var j:int = 10; j < 20; j++)
				Assert.assertEquals("Elt " + j, j+2, _ctx.pagedQueryClient2.getItemAt(j).id);
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
	
	private static var count:int = 0;
	private static var count2:int = 0;
	
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
				if (count > 0 && i >= 10)
					person.id = i+2;
				else
					person.id = i+1;
				person.uid = "P" + person.id;
				person.version = 0;
				person.lastName = "Person" + i;
				coll.addItem(person);
			}
			count++;
			return buildResult({ firstResult: first, maxResults: max, resultCount: 1000, resultList: coll });
		}
		else if (componentName == "pagedQueryClient2" && op == "find2") {
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
			return buildResult({ firstResult: first, maxResults: max, resultCount: 1000, resultList: coll });
		}
        
        return buildFault("Server.Error");
    }
}
