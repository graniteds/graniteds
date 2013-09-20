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
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeMerger;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    
    
    public class TestChangeSetEntityRemote 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testChangeSetEntityRemote():void {
        	var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.meta::detachedState = "bla";
        	person.contacts = new PersistentSet(false);
			_ctx.person = _ctx.meta_mergeExternal(person);
			
			person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;        	
			person.meta::detachedState = "bla";
			person.contacts = new PersistentSet(true);
        	var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.length);
			
        	person.lastName = 'toto';
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			
			person.contacts.removeItemAt(0);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 2", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", -1, coll.getChange(0).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(0).key);
			Assert.assertEquals("ChangeSet collection value", "C1", coll.getChange(0).value.uid);
			
			var contact2a:Contact = new Contact();
			contact2a.email = "test@truc.net";
			contact2a.person = person;
			person.contacts.addItem(contact2a);
			var contact2b:Contact = new Contact();
			contact2b.email = "test@truc.com";
			contact2b.person = person;
			person.contacts.addItem(contact2b);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 3", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 3, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(1).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(1).key);
			Assert.assertFalse("ChangeSet collection element uninitialized", coll.getChange(1).value.meta::isInitialized("person"));
			Assert.assertEquals("ChangeSet collection element reference", coll.getChange(1).value.person.id, coll.getChange(2).value.person.id);
			
			var contact3:Contact = new Contact();
			contact3.email = "tutu@tutu.net";
			contact3.version = 0;
			contact3.uid = "C3";
			contact3.id = 3;
			_ctx.contact3 = _ctx.meta_mergeExternal(contact3);
			contact3 = _ctx.contact3;
			
			person.contacts.addItem(contact3);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 4, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(3).type);
			Assert.assertEquals("ChangeSet collection index", 2, coll.getChange(3).key);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(3).value is ChangeRef);
			Assert.assertStrictlyEquals("ChangeSet collection value", contact3.uid, coll.getChange(3).value.uid);
			
			changeSet = _ctx.meta_mergeExternal(changeSet) as ChangeSet;
			
			Assert.assertEquals("ChangeSet count 4", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);
			coll = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 4, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(3).type);
			Assert.assertEquals("ChangeSet collection index", 2, coll.getChange(3).key);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(3).value is ChangeRef);
			Assert.assertStrictlyEquals("ChangeSet collection value", contact3.uid, coll.getChange(3).value.uid);
			
			Tide.getInstance().addComponents([ChangeMerger]);
			
			changeSet = new ChangeSetBuilder(_ctx, false).buildChangeSet();
			var object:Object = _ctx.meta_mergeExternalData(changeSet);
			
			Assert.assertStrictlyEquals("Local ChangeSet merge", person, object);
        }
    }
}
