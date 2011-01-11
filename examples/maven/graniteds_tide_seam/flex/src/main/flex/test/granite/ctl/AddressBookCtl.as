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

package test.granite.ctl
{
import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.data.utils.Managed;
import mx.events.CloseEvent;
import mx.logging.Log;
import mx.logging.targets.TraceTarget;

import org.granite.tide.events.*;

import test.granite.ejb3.entity.Address;
import test.granite.ejb3.entity.Contact;
import test.granite.ejb3.entity.Person;
import test.granite.events.*;

[Bindable]
[Name("addressBookCtl", restrict="true")]
public class AddressBookCtl
{

    [In]
    public var personHome:Object;

    [In]
    public var application:Object;

    [Out]
    public var contact:Contact;


    public function init():void
    {
        var t:TraceTarget = new TraceTarget();
        t.filters = ["org.granite.*"];
        Log.addTarget( t );
    }


    [Observer]
    public function selectPerson( event:SelectPersonEvent ):void
    {
        if( event.person )
        {
            personHome.id = event.person.id;
            personHome.instance = event.person;
        }
    }

    [Observer]
    public function newPerson( event:NewPersonEvent ):void
    {
        personHome.id = null;
        personHome.instance = new Person();
        personHome.instance.contacts = new ArrayCollection();
        application.currentState = "CreatePerson";
    }

    [Observer]
    public function createPerson( event:CreatePersonEvent ):void
    {
        personHome.persist( personCreated );
    }

    private function personCreated( event:TideResultEvent ):void
    {
        application.currentState = "";
    }

    [Observer]
    public function editPerson( event:EditPersonEvent ):void
    {
        personHome.id = event.person.id;
        personHome.instance = event.person;
        application.currentState = "EditPerson";
    }

    [Observer]
    public function cancelPerson( event:CancelPersonEvent ):void
    {
        Managed.resetEntity( personHome.instance );
        application.currentState = "";
    }

    [Observer]
    public function modifyPerson( event:ModifyPersonEvent ):void
    {
        personHome.update( personModified );
    }

    private function personModified( event:TideResultEvent ):void
    {
        application.currentState = "";
    }

    [Observer]
    public function askDeletePerson( event:AskDeletePersonEvent ):void
    {
        Alert.show( 'Do you really want to delete this person ?', 'Confirmation', (Alert.YES | Alert.NO),
                    null, deletePerson );
    }

    private function deletePerson( event:CloseEvent ):void
    {
        if( event.detail == Alert.YES )
        {
            personHome.id = personHome.instance.id;
            personHome.remove( personDeleted );
        }
    }

    private function personDeleted( event:TideResultEvent ):void
    {
        personHome.id = null;
        personHome.instance = null;
        application.persons.selectedItems = [];
    }


    [Observer]
    public function newContact( event:NewContactEvent ):void
    {
        personHome.id = event.person.id;
        personHome.instance = event.person;
        var newContact:Contact = new Contact();
        newContact.address = new Address();
        newContact.person = personHome.instance;
        personHome.instance.contacts.addItem( newContact );
        contact = newContact;
        application.currentState = "CreateContact";
    }

    [Observer]
    public function editContact( event:EditContactEvent ):void
    {
        personHome.id = event.person.id;
        personHome.instance = event.person;
        contact = event.contact;
        application.currentState = "EditContact";
    }

    [Observer]
    public function cancelContact( event:CancelContactEvent ):void
    {
        Managed.resetEntity( personHome.instance );
        contact = null;
        application.currentState = "";
    }

    [Observer]
    public function askDeleteContact( event:AskDeleteContactEvent ):void
    {
        contact = event.contact;
        Alert.show( 'Do you really want to delete this contact ?', 'Confirmation', (Alert.YES | Alert.NO),
                    null, deleteContact );
    }

    private function deleteContact( event:CloseEvent ):void
    {
        if( event.detail == Alert.YES )
        {
            personHome.id = personHome.id;
            personHome.instance = personHome.instance;
            var contactIndex:int = personHome.instance.contacts.getItemIndex( contact );
            personHome.instance.contacts.removeItemAt( contactIndex );
            personHome.update();
        }
    }

    [Observer]
    public function createContact( event:CreateContactEvent ):void
    {
        personHome.update( contactCreated );
    }

    private function contactCreated( event:TideResultEvent ):void
    {
        application.currentState = "";
        contact = null;
    }

    [Observer]
    public function modifyContact( event:ModifyContactEvent ):void
    {
        personHome.update( contactModified );
    }

    private function contactModified( event:TideResultEvent ):void
    {
        application.currentState = "";
    }
}
}
