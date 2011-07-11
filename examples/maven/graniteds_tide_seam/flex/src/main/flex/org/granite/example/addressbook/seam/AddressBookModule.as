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

package org.granite.example.addressbook.seam {

    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    
    import org.granite.tide.ITideModule;
    import org.granite.tide.Tide;
    import org.granite.tide.data.DataObserver;
    import org.granite.tide.data.OptimisticLockExceptionHandler;
    import org.granite.tide.seam.framework.PagedQuery;
    import org.granite.tide.validators.ValidatorExceptionHandler;
    
    import org.granite.example.addressbook.seam.ctl.*;
    import org.granite.example.addressbook.entity.Person;
    
    
    [Bindable]
    public class AddressBookModule implements ITideModule {
        
        public function init(tide:Tide):void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
            
            tide.addExceptionHandler(DefaultExceptionHandler);
            tide.addExceptionHandler(OptimisticLockExceptionHandler);
            tide.addExceptionHandler(ValidatorExceptionHandler);
            tide.addExceptionHandler(AccessDeniedExceptionHandler);
            tide.addExceptionHandler(SecurityExceptionHandler);
            
            // Initialize Tide client components
            
            // people component is a client PagedQuery component, linked to the server-side EntityQuery component named people
            // it is marked as autoCreate=true to be initialized as soon as it is referenced somewhere
            // it is marked as RESTRICT_YES so the collection content is cleared on user logout
            tide.addComponentWithFactory("people", PagedQuery, { elementClass: Person }, false, true, Tide.RESTRICT_YES);
            // searchCtl component is a client component with 2 static injections
            // references to personHome and people are directly injected instead of bound with Flex data binding
            tide.addComponentWithFactory("searchCtl", SearchCtl, {
            	persons: "#{application.persons}", 
                personHome: "#{personHome}", 
                people: "#{people}" 
            });
            // addressBookCtl is just a simple client component with default settings
            tide.addComponents([AddressBookCtl]);
            // we force the examplePerson entity to be autoCreate=true so any reference in a Flex binding created it
            tide.setComponentAutoCreate("examplePerson", true);
            
            // bind a refresh of the people list to the login event
            tide.addEventObserver("org.granite.tide.login", "people", "fullRefresh");
            
            // simple DataObserver component: the component name must match the JMS topic name
            // We define event observers so the component subscribed the topic on user login
            tide.addComponent("addressBookTopic", DataObserver);
            tide.addEventObserver("org.granite.tide.login", "addressBookTopic", "subscribe");
            tide.addEventObserver("org.granite.tide.logout", "addressBookTopic", "unsubscribe");
        }
    }
}
