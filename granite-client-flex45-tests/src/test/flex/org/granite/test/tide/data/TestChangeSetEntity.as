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
package org.granite.test.tide.data
{
    import flash.utils.getQualifiedClassName;
    
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentMap;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    
    
    public class TestChangeSetEntity 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        [Test]
        public function testChangeSetEntity():void {
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
        }


        [Test]
        public function testLocalChangeSetEntity():void {
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

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet empty", 1, changeSet.length);
            Assert.assertTrue("ChangeSet empty", changeSet.changes[0].empty);    // Only version property

        	person.lastName = 'toto';

			changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
			Assert.assertEquals("ChangeSet property value", "toto", changeSet.getChange(0).changes.lastName);

			person.contacts.removeItemAt(0);

			changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet count after remove contact", 1, changeSet.length);
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

			changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

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
        }

        [Test]
        public function testLocalChangeSetEntity2():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
        	var contact:Contact = new Contact();
			contact.uid = "C1";
			contact.id = 1;
        	contact.version = 0;
        	contact.person = person;
        	person.contacts.addItem(contact);

        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
            contact = Contact(person.contacts.getItemAt(0));

            contact.email = "test@test.com";

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet count 1", 2, changeSet.length);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Person), changeSet.getChange(0).className);
            Assert.assertTrue("ChangeSet no property", changeSet.getChange(0).empty);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Contact), changeSet.getChange(1).className);
			Assert.assertEquals("ChangeSet property value", "test@test.com", changeSet.getChange(1).changes.email);

            person.lastName = "Toto";

            changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

            Assert.assertEquals("ChangeSet count 2", 2, changeSet.length);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Person), changeSet.getChange(0).className);
            Assert.assertEquals("ChangeSet property value", "Toto", changeSet.getChange(0).changes.lastName);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Contact), changeSet.getChange(1).className);
            Assert.assertEquals("ChangeSet property value", "test@test.com", changeSet.getChange(1).changes.email);

			person.contacts.removeItemAt(0);

			changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet count after remove contact", 1, changeSet.length);
			Assert.assertEquals("ChangeSet type", getQualifiedClassName(Person), changeSet.getChange(0).className);
			var coll:CollectionChanges = changeSet.getChange(0).changes.contacts as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", -1, coll.getChange(0).type);
			Assert.assertEquals("ChangeSet collection index", 0, coll.getChange(0).key);
			Assert.assertEquals("ChangeSet collection value", "C1", coll.getChange(0).value.uid);
        }

        [Test]
        public function testLocalChangeSetEntity3():void {
			var person:Person9 = new Person9();
            person.id = 1;
            person.version = 0;
			person.uid = "P1";
			person.meta::detachedState = "bla";
            person.testMap = new PersistentMap(true);
            var contact:Contact3 = new Contact3();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            contact.person = person;
            person.contacts = new PersistentSet(true);
            person.contacts.addItem(contact);

            _ctx.person = Person9(_ctx.meta_mergeExternalData(person));
            person = Person9(_ctx.person);
            contact = Contact3(person.contacts.getItemAt(0));

        	var key:Key = new Key();
            key.id = 1;
            key.version = 0;
            key.uid = "K1";
            key = Key(_ctx.meta_mergeExternalData(key));

            person.testMap.put(key, contact);

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

			Assert.assertEquals("ChangeSet count 1", 1, changeSet.length);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Person9), changeSet.getChange(0).className);
            Assert.assertTrue("ChangeSet map", changeSet.getChange(0).changes.testMap is CollectionChanges);
            var ccs:CollectionChanges = CollectionChanges(changeSet.getChange(0).changes.testMap);
            Assert.assertEquals("ChangeSet add", 1, ccs.changes[0].type);
            Assert.assertTrue("ChangeSet add key", ccs.changes[0].key is ChangeRef);
            Assert.assertEquals("ChangeSet add key", getQualifiedClassName(Key), ccs.changes[0].key.className);
			Assert.assertTrue("ChangeSet add value", ccs.changes[0].value is ChangeRef);
            Assert.assertEquals("ChangeSet add value", getQualifiedClassName(Contact3), ccs.changes[0].value.className);

            var contact2:Contact3 = new Contact3();
            contact2.uid = "C2";
            contact2.email = "test@test.com";
            contact2.person = person;
            var key2:Key = new Key();
            key2.uid = "K2";

            person.testMap.put(key2, contact2);

            changeSet = new ChangeSetBuilder(_ctx).buildEntityChangeSet(person);

            Assert.assertEquals("ChangeSet count 2", 1, changeSet.length);
            Assert.assertEquals("ChangeSet type", getQualifiedClassName(Person9), changeSet.getChange(0).className);
            Assert.assertTrue("ChangeSet map", changeSet.getChange(0).changes.testMap is CollectionChanges);
            ccs = CollectionChanges(changeSet.getChange(0).changes.testMap);
            Assert.assertEquals("ChangeSet add", 1, ccs.changes[0].type);
            Assert.assertEquals("ChangeSet add", 1, ccs.changes[1].type);
            Assert.assertEquals("ChangeSet add key", "K2", ccs.changes[1].key.uid);
			Assert.assertTrue("ChangeSet add value", "test@test.com", ccs.changes[1].value.email);
            Assert.assertFalse("ChangeSet add value uninit", ccs.changes[1].value.meta::isInitialized("person"));
        }
		
		
		[Test]
		public function testChangeSetEntityCollectionSort():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);

            var collSnapshot:Array = person.contacts.toArray();
			
			var c:Contact = person.contacts.removeItemAt(0) as Contact;
			person.contacts.addItemAt(c, 2);
			c = person.contacts.removeItemAt(1) as Contact;
			person.contacts.addItemAt(c, 0);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count", 1, changeSet.length);
			var collChanges:CollectionChanges = changeSet.changes[0].changes.contacts as CollectionChanges;

            TestDataUtils.checkListChangeSet(person.contacts, collChanges, collSnapshot);
		}
		
		
		[Test]
		public function testChangeSetEntityCollectionSort2():void {
			var person:Person = new Person();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
			person.contacts = new PersistentSet(true);
			var contact1:Contact = new Contact();
			contact1.uid = "C1";
			contact1.id = 1;
			contact1.version = 0;
			contact1.person = person;
			person.contacts.addItem(contact1);
			var contact2:Contact = new Contact();
			contact2.uid = "C2";
			contact2.id = 2;
			contact2.version = 0;
			contact2.person = person;
			person.contacts.addItem(contact2);
			var contact3:Contact = new Contact();
			contact3.uid = "C3";
			contact3.id = 3;
			contact3.version = 0;
			contact3.person = person;
			person.contacts.addItem(contact3);			
			person = _ctx.person = _ctx.meta_mergeExternal(person);

            var collSnapshot:Array = person.contacts.toArray();

            var contact4:Contact = new Contact();
			contact4.uid = "C4";
			contact4.id = 4;
			contact4.version = 0;
			contact4.person = person;
			person.contacts.addItem(contact4);
			
			var c:Contact = person.contacts.removeItemAt(2) as Contact;
			person.contacts.addItemAt(c, 1);
			c = person.contacts.removeItemAt(1) as Contact;
			person.contacts.addItemAt(c, 0);

			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count", 1, changeSet.length);
			var collChanges:CollectionChanges = changeSet.changes[0].changes.contacts as CollectionChanges;

            TestDataUtils.checkListChangeSet(person.contacts, collChanges, collSnapshot);
		}
    }
}
