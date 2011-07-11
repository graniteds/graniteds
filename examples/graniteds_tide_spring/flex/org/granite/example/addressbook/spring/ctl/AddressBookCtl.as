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

package org.granite.example.addressbook.spring.ctl {
	
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.events.CloseEvent;
    import mx.events.PropertyChangeEvent;
    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    import mx.data.utils.Managed;
    
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.spring.Context;
    
    import org.granite.example.addressbook.entity.Address;
    import org.granite.example.addressbook.entity.Contact;
    import org.granite.example.addressbook.entity.Person;
	import org.granite.example.addressbook.spring.service.PersonService;
    
    
    [Bindable]
    [Name("addressBookCtl")]
    public class AddressBookCtl {
        
        [In]
        public var context:Context;
        
        [Inject]
        public var personService:PersonService;
        
        [In]
        public var application:Object;
        
        [In] [Out]
        public var person:Person;
        
        [Out]
        public var contact:Contact;
        
        
        public function init():void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
        }
        
        
        [Observer("selectPerson")]
        public function selectPerson(selectedPerson:Person):void {
            if (selectedPerson)
                person = selectedPerson;
        }
        
        [Observer("newPerson")]
        public function newPerson():void {
            person = new Person();
            person.contacts = new ArrayCollection();
            application.currentState = "CreatePerson";
        }

        [Observer("createPerson")]
        public function createPerson():void {
			personService.createPerson(person, personCreated);
        }
        
        private function personCreated(event:TideResultEvent):void {
            application.currentState = "";
        }

        [Observer("editPerson")]
        public function editPerson(selectedPerson:Person):void {
        	person = selectedPerson;
            application.currentState = "EditPerson";
        }
        
        [Observer("cancelPerson")]
        public function cancelPerson():void {
            Managed.resetEntity(person);
            application.currentState = "";
        }

        [Observer("modifyPerson")]
        public function modifyPerson():void {
            personService.modifyPerson(person, personModified);
        }
        
        private function personModified(event:TideResultEvent):void {
            application.currentState = "";
        }

        
        [Observer("askDeletePerson")]
        public function askDeletePerson():void {
            Alert.show('Do you really want to delete this person ?', 'Confirmation', (Alert.YES | Alert.NO), 
                null, deletePerson);
        }
        
        private function deletePerson(event:CloseEvent):void {
            if (event.detail == Alert.YES) {
                personService.deletePerson(person.id, personDeleted);
            }
        }
        
        private function personDeleted(event:TideResultEvent):void {
            person = null;
        }
        
        
        [Observer("newContact")]
        public function newContact():void {
            var newContact:Contact = new Contact();
            newContact.address = new Address();
            newContact.person = person;
            person.contacts.addItem(newContact);
            contact = newContact;
            application.currentState = "CreateContact";
        }
        
        [Observer("editContact")]
        public function editContact(selectedContact:Contact):void {
            contact = selectedContact;
            application.currentState = "EditContact";
        }
        
        [Observer("cancelContact")]
        public function cancelContact():void {
            Managed.resetEntity(person);
            contact = null;
            application.currentState = "";
        }

        [Observer("askDeleteContact")]
        public function askDeleteContact(selectedContact:Contact):void {
            contact = selectedContact;
            Alert.show('Do you really want to delete this contact ?', 'Confirmation', (Alert.YES | Alert.NO), 
                null, deleteContact);
        }
        
        private function deleteContact(event:CloseEvent):void {
            if (event.detail == Alert.YES) {
                var contactIndex:int = person.contacts.getItemIndex(contact);
                person.contacts.removeItemAt(contactIndex);
                personService.modifyPerson(person);
            }
        }

        [Observer("createContact")]
        public function createContact():void {
            personService.modifyPerson(person, contactCreated);
        }
        
        private function contactCreated(event:TideResultEvent):void {
            application.currentState = "";
            contact = null;
        }

        [Observer("modifyContact")]
        public function modifyContact():void {
            personService.modifyPerson(person, contactModified);
        }
        
        private function contactModified(event:TideResultEvent):void {
            application.currentState = "";
        }

        [Observer("create100")]
        public function create100():void {
            personService.create100Persons();
        }
    }
}
