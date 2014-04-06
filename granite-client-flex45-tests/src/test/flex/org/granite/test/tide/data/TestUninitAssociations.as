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
package org.granite.test.tide.data
{
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;


    public class TestUninitAssociations {

        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function TestUninitializeAssociations():void {
        	var person:Person = new Person();
            person.id = 1;
            person.version = 0;
            person.uid = "P1";
            person.contacts = new PersistentSet(false);

            person = Person(_ctx.meta_mergeExternalData(person));

            var contact:Contact = new Contact();
            contact.id = 1;
            contact.version = 0;
            contact.uid = "C1";
            contact.person = new Person();
			contact.person.id = 1;
            contact.person.meta::defineProxy3();

            _ctx.meta_mergeExternalData(contact);

        	var person2:Person = new Person();
            person2.id = 1;
        	person2.version = 0;
            person2.uid = "P1";
			person2.meta::detachedState = "P1bla";
            person2.contacts = new PersistentSet(true);
        	var contact2:Contact = new Contact();
            contact2.id = 1;
        	contact2.version = 0;
            contact2.uid = "C1";
        	contact2.person = person2;
        	person2.contacts.addItem(contact2);

        	person2 = Person(_ctx.meta_mergeExternalData(person2));
            contact2 = Contact(person2.contacts.getItemAt(0));

            var tmpctx:BaseContext = _ctx.newTemporaryContext();
            var p:Person = Person(tmpctx.meta_mergeFromContext(_ctx, person2, false, true));

        	Assert.assertFalse("Contacts coll uninitialized", Object(p.contacts).isInitialized());

            tmpctx = _ctx.newTemporaryContext();
            var c:Contact = Contact(tmpctx.meta_mergeFromContext(_ctx, contact2, false, true));

        	Assert.assertFalse("Person assoc proxied", c.meta::isInitialized("person"));
			Assert.assertEquals("Person assoc detachedState", "P1bla", c.person.meta::detachedState);
		}
		
		[Test]
		public function TestUninitializeSimpleAssociations():void {
			var contact:Contact3 = new Contact3();
			contact.id = 1;
			contact.version = 0;
			contact.uid = "C1";
			contact.email = "toto@toto.com";
			var person:Person9 = new Person9();
			person.id = 1;
			person.version = 0;
			person.uid = "P1";
			person.lastName = "Toto";
			person.meta::detachedState = "blabla";
			contact.person = person;
			
			contact = Contact3(_ctx.meta_mergeExternalData(contact));
			
			contact.email = "tutu@toto.com";
			
			var tmpctx:BaseContext = _ctx.newTemporaryContext();
			var c:Contact3 = Contact3(tmpctx.meta_mergeFromContext(_ctx, contact, false, true));
			
			Assert.assertFalse("Person assoc proxied", c.meta::isInitialized("person"));
			Assert.assertEquals("Person detachedState", "blabla", c.person.meta::detachedState);		
		}
		
		[Test]
		public function TestUninitializeToManyAssociations1():void {
			var person:Person9 = new Person9();
			person.id = 1;
			person.version = 0;
			person.uid = "P1";
			person.lastName = "Toto";
			person.contacts = new PersistentSet();
			var contact:Contact3 = new Contact3();
			contact.id = 1;
			contact.version = 0;
			contact.uid = "C1";
			contact.email = "toto@toto.com";
			contact.person = person;
			person.contacts.addItem(contact);
			
			person = Person9(_ctx.meta_mergeExternalData(person));
			
			person.contacts.removeItemAt(0);
			
			var tmpctx:BaseContext = _ctx.newTemporaryContext();
			var p:Person9 = Person9(tmpctx.meta_mergeFromContext(_ctx, person, false, true));
			
			Assert.assertTrue("Person contacts initialized", IPersistentCollection(p.contacts).isInitialized());
			Assert.assertTrue("Person contacts set dirty", PersistentSet(PersistentCollection(p.contacts).list).isDirty());
		}
		
		[Test]
		public function TestUninitializeToManyAssociations2():void {
			var cl1:Classification = new Classification();
			cl1.id = 1;
			cl1.uid = "CL1";
			cl1.version = 0;
			cl1.name = "CL1";
			cl1.subclasses = new PersistentSet();
			cl1.superclasses = new PersistentSet();
			
			cl1 = Classification(_ctx.meta_mergeExternalData(cl1));
			
			var cl2:Classification = new Classification();
			cl2.uid = "CL2";
			cl2.subclasses = new ArrayCollection();
			cl2.superclasses = new ArrayCollection();
			cl2.superclasses.addItem(cl1);
			cl1.subclasses.addItem(cl2);
			
			var tmpctx:BaseContext = _ctx.newTemporaryContext();
			var cl:Classification = Classification(tmpctx.meta_mergeFromContext(_ctx, cl2, false, true));
			
			Assert.assertTrue("Classification not uninitialized", cl.superclasses.getItemAt(0).meta::isInitialized());
		}
    }
}
