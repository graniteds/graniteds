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

package org.granite.test.tide.data {
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.data.Conflicts;
	import org.granite.tide.data.events.TideDataConflictsEvent;
	import org.granite.test.tide.Contact;
	import org.granite.test.tide.Person;
	

	[Name(scope="conversation")]
	[Bindable]
    public class MyComponentConflictConversation {
    	
    	[In]
    	public var context:BaseContext;
		
		public var conflicts:Conflicts;
		
		public var person:Person;
    	
    	[Observer("editPerson")]
    	public function start(person:Person):void {
			this.person = person;
			
			context.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, conflictsHandler);
			
			person.lastName = "toto";
			var addedContact:Contact = new Contact();
			addedContact.id = 2;
			addedContact.version = 0;
			addedContact.person = person;
			person.contacts.addItem(addedContact);
			var contact:Contact = Contact(person.contacts.getItemAt(0));
			contact.email = "test";
    	}
		
		private function conflictsHandler(event:TideDataConflictsEvent):void {
			conflicts = event.conflicts;
		}
    }
}
