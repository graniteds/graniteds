/**
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
package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Conflict;
    import org.granite.tide.data.Conflicts;
    import org.granite.tide.data.events.TideDataConflictsEvent;
    
    
    public class TestMergeConflictEntityCollection
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var _conflicts:Conflicts;
        
        
        [Test]
        public function testMergeConflictEntityCollection():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
			person.lastName = "jojo";
        	person.contacts = new ArrayCollection();
        	var contact:Contact = new Contact();
        	contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	_ctx.person = _ctx.meta_mergeExternalData(person, null, null);
        	person = _ctx.person;
        	
			// User A changes some simple property
        	person.lastName = "toto";
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	
			// User A received changes from another user B
        	var person2:Person = new Person();
        	person2.contacts = new ArrayCollection();
        	person2.id = person.id;
        	person2.version = 1;
        	person2.uid = person.uid;
        	person2.lastName = "tutu";
			var contact1:Contact = new Contact();
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person2;
			person2.contacts.addItem(contact1);
        	var contact2:Contact = new Contact();
        	contact2.id = 2;
        	contact2.version = 0;
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);
        	
        	_ctx.addEventListener(TideDataConflictsEvent.DATA_CONFLICTS, conflictsHandler);
        	
        	_ctx.meta_mergeExternalData(person2, null, "S2");
        	
        	Assert.assertEquals("Conflicts after merge", 1, _conflicts.conflicts.length);
			Assert.assertEquals("Contacts collections not yet merged", 1, person.contacts.length);
			Assert.assertTrue("Person still dirty after merge", _ctx.meta_isEntityChanged(person));
			
			_conflicts.conflicts[0].acceptClient();
			
			Assert.assertEquals("Person last name", "toto", person.lastName);
			Assert.assertTrue("Person still dirty after accept client", _ctx.meta_isEntityChanged(person));
			
			_ctx.meta_mergeExternalData(person2, null, "S2");
			
			Assert.assertEquals("Conflicts after merge 2", 1, _conflicts.conflicts.length);
			Assert.assertEquals("Contacts collections not yet merged 2", 1, person.contacts.length);
			Assert.assertTrue("Person still dirty after merge 2", _ctx.meta_isEntityChanged(person));
			
			_conflicts.conflicts[0].acceptServer();
			
			Assert.assertEquals("Person last name", "tutu", person.lastName);
			Assert.assertEquals("Contacts collections merges after accept server", 2, person.contacts.length);
			Assert.assertFalse("Person not dirty after accept server", _ctx.meta_isEntityChanged(person));
        }
        
        private function conflictsHandler(event:TideDataConflictsEvent):void {
        	_conflicts = event.conflicts;
        }
    }
}
