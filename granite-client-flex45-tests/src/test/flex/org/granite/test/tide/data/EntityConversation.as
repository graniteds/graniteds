/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.data {
	
	import mx.collections.ArrayCollection;
	
	import org.granite.persistence.PersistentSet;
	import org.granite.tide.BaseContext;
	import org.granite.test.tide.Contact;
	import org.granite.test.tide.Person;
	

	[Name("entityConversation", scope="conversation")]
    public class EntityConversation {
    	
    	[In]
    	public var context:BaseContext;
    	
    	[Observer("startConversation")]
    	public function start(person:Person):void {
    		context.person = person;
    	}
    	
    	[Observer("updateConversation")]
    	public function update():void {
    		var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new PersistentSet();
        	var contact1:Contact = new Contact();
        	contact1.person = person;
        	contact1.id = 1;
        	contact1.version = 0;
        	contact1.uid = "C1";
        	contact1.email = "jojo@jojo.net";
        	person.contacts.addItem(contact1);
    		var contact2:Contact = new Contact();
    		contact2.id = 2;
    		contact2.version = 0;
    		contact2.uid = "C2";
    		contact2.person = person;
    		contact2.email = "toto@toto.net";
        	person.contacts.addItem(contact2);
    		context.meta_mergeExternalData(person);
    	}
    	
    	[Observer("endConversation")]
    	public function end():void {
    		context.meta_end(true);
    	}
    	
    	[Observer("endMergeConversation")]
    	public function endMerge():void {
    		context.person.lastName = "Juju";
    		context.meta_end(true);
    	}
    	
    	[Observer("endMergeConversation2")]
    	public function endMerge2():void {
    		var person:Person = new Person();
        	person.id = 1;
        	person.version = 1;
        	person.uid = "P1";
        	person.lastName = "Juju";
        	person.contacts = new ArrayCollection();
        	context.meta_mergeExternalData(person);
    		context.meta_end(true);
    	}
    }
}
