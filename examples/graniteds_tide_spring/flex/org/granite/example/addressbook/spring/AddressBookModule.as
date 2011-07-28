/*
GRANITE DATA SERVICES
Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

This file is part of Granite Data Services.

Granite Data Services is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at your
option) any later version.

Granite Data Services is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
for more details.

You should have received a copy of the GNU Library General Public License
along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.example.addressbook.spring {
	
    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    
    import org.granite.tide.ITideModule;
    import org.granite.tide.Tide;
    import org.granite.tide.data.DataObserver;
    import org.granite.tide.data.OptimisticLockExceptionHandler;
    import org.granite.tide.spring.PagedQuery;
    import org.granite.tide.validators.ValidatorExceptionHandler;
    
    import org.granite.example.addressbook.spring.ctl.*;
	import org.granite.example.addressbook.spring.service.PersonService;
    import org.granite.example.addressbook.entity.Person;
    
    
    [Bindable]
    public class AddressBookModule implements ITideModule {
        
        public function init(tide:Tide):void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
            
            tide.addExceptionHandler(ValidatorExceptionHandler);
            tide.addExceptionHandler(OptimisticLockExceptionHandler);
            tide.addExceptionHandler(AccessDeniedExceptionHandler);
            
            // Initialize Tide client components
            
            // people component is a client PagedQuery component, linked to the server-side EntityQuery component named people
            // it is marked as autoCreate=true to be initialized as soon as it is referenced somewhere
            // it is marked as RESTRICT_YES so the collection content is cleared on user logout
            tide.addComponentWithFactory("people", PagedQuery, 
            	{ remoteComponentClass: PersonService,
				  methodName: "findPersons",
            	  filterClass: Person,
            	  elementClass: Person,
            	  maxResults: 36 }, 
            	false, true, Tide.RESTRICT_YES);
            // addressBookCtl is a simple client component
            // it is marked as autoCreate=true to be initialized as soon as it is referenced somewhere
            // it is marked as RESTRICT_YES so its properties are automatically cleared on user logout
            tide.addComponent("addressBookCtl", AddressBookCtl, false, true, Tide.RESTRICT_YES);
            // searchCtl component is a client component with 2 static injections
            // references to the persons DataGrid and people are directly injected instead of bound with Flex data binding
            tide.addComponentWithFactory("searchCtl", SearchCtl, {
                persons: "#{application.persons}",
                people: "#{people}"
            }, false, true, Tide.RESTRICT_YES);
            // the client Person component is marked RESTRICT_YES so it is cleared on user logout
            tide.addComponent("person", Person, false, true, Tide.RESTRICT_YES);
            
            // bind a list refresh to user login
            tide.addEventObserver("org.granite.tide.login", "people", "fullRefresh");
            
            // simple DataObserver component: the component name must match the JMS topic name
            // We define event observers so the component subscribed the topic on user login
            tide.addComponent("addressBookTopic", DataObserver);
            tide.addEventObserver("org.granite.tide.login", "addressBookTopic", "subscribe");
            tide.addEventObserver("org.granite.tide.logout", "addressBookTopic", "unsubscribe");
        }
    }
}
