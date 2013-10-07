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

    import flash.events.Event;

	import org.flexunit.Assert;
	import org.flexunit.async.Async;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
	import org.granite.tide.BaseContext;
	import org.granite.tide.collections.PagedCollection;
    import org.granite.tide.service.SimpleServerApp;
    import org.granite.tide.spring.PagedQuery;

    public class TestSpringMultiServer
    {
        private var _ctx:BaseContext = MockSpring.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            MockSpring.reset();
            _ctx = MockSpring.getInstance().getSpringContext();
            MockSpring.getInstance().mainServerSession.serverApp = new SimpleServerApp("/app1", false, "localhost", "8080");
            Object(MockSpring.getInstance().mainServerSession).token = new MockMultiCallAsyncToken("app1");
            _ctx.serverSession2 = new MockSpringServerSession(new MockMultiCallAsyncToken("app2"));
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient1", PagedQuery, { maxResults: 20 });
            MockSpring.getInstance().addComponentWithFactory("pagedQueryClient2", PagedQuery, { serverSession: _ctx.serverSession2, maxResults: 30 });
        }
        
        
        [Test(async)]
        public function testSpringMultiPagedQuery():void {
        	var pagedQuery1:PagedQuery = _ctx.pagedQueryClient1;
        	pagedQuery1.fullRefresh();
            Async.handleEvent(this, pagedQuery1, PagedCollection.COLLECTION_PAGE_CHANGE, find1Result, 1000);
        }

        private function find1Result(event:Event, pass:Object = null):void {
        	var pagedQuery1:PagedQuery = _ctx.pagedQueryClient1;

        	var person:Person = pagedQuery1.getItemAt(0) as Person;
        	Assert.assertEquals("Person id", 1, person.id);

            var pagedQuery2:PagedQuery = _ctx.pagedQueryClient2;
            pagedQuery2.fullRefresh();
            Async.handleEvent(this, pagedQuery2, PagedCollection.COLLECTION_PAGE_CHANGE, find2Result, 1000);
        }

        private function find2Result(event:Event, pass:Object = null):void {
            var pagedQuery2:PagedQuery = _ctx.pagedQueryClient2;

            var contact:Contact = pagedQuery2.getItemAt(0) as Contact;
            Assert.assertEquals("Contact id", 1, contact.id);
        }
	}
}


import mx.collections.ArrayCollection;
import mx.rpc.events.AbstractEvent;

import org.granite.test.tide.Contact;

import org.granite.test.tide.Person;
import org.granite.test.tide.spring.MockSpringAsyncToken;
import org.granite.tide.invocation.InvocationCall;


class MockMultiCallAsyncToken extends MockSpringAsyncToken {

    public var app:String;

    function MockMultiCallAsyncToken(app:String) {
        super(null);
        this.app = app;
    }
	
    protected override function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
		var coll:ArrayCollection, first:int, max:int, i:int, obj:Object;
		
        if (app == "app1" && componentName == "pagedQueryClient1" && op == "find") {
			coll = new ArrayCollection();
			first = params[1];
			max = params[2];
        	if (max == 0)
        		max = 20;
        	for (i = first; i < first+max; i++) {
        		obj = new Person();
        		obj.id = i+1;
        		obj.lastName = "Person" + (i+1);
        		coll.addItem(obj);
        	}
            return buildResult({ resultCount: 1000, resultList: coll });
        }
        else if (app == "app2" && componentName == "pagedQueryClient2" && op == "find") {
            coll = new ArrayCollection();
            first = params[1];
            max = params[2];
            if (max == 0)
                max = 30;
            for (i = first; i < first+max; i++) {
                obj = new Contact();
                obj.id = i+1;
                obj.email = "Contact" + (i+1);
                coll.addItem(obj);
            }
            return buildResult({ resultCount: 1000, resultList: coll });
        }
        
        return buildFault("Server.Error");
    }
}
