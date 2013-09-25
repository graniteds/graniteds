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
    
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.meta;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChange;
    import org.granite.tide.data.CollectionChanges;
    
    
    public class TestChangeSetEntityAssociation
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
        public function testChangeSetEntityAssociation():void {
			
        	var person:Person = new Person();
        	person.contacts = new ArrayCollection();
			person.id = 1;
        	person.version = 0;
			person.uid = "P1";
        	var contact:Contact = new Contact();
			contact.id = 1;
        	contact.version = 0;
			contact.uid = "C1";
        	contact.person = person;
        	person.contacts.addItem(contact);
			var contact2:Contact = new Contact();
			contact2.id = 2;
			contact2.version = 0;
			contact2.uid = "C2";
			contact2.person = person;
			person.contacts.addItem(contact2);
        	_ctx.person = _ctx.meta_mergeExternal(person);
			
        	person.lastName = "Zozo";
			contact.email = "zozo@zozo.net";
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(contact.person));
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			Assert.assertEquals("Changes", 2, changeSet.length);
			var change:Change = null;
			for each (var ch:Change in changeSet.changes) {
				if (ch.className == getQualifiedClassName(Contact)) {
					change = ch;
					break;
				}
			}
			var count:uint = 0;
			for (var c:String in change.changes) {
				if (c == "version")
					continue;
				count++;
			}
			Assert.assertEquals("Person change", 1, count);
        }
		
		[Test]
		public function testChangeSetEntityProxy():void {
			
			var cl2:Classification = new Classification();
			cl2.id = 2;
			cl2.version = 0;
			cl2.meta::detachedState = "CL2DS";
			cl2.uid = "CL2";
			cl2.subclasses = new PersistentSet();
			cl2.superclasses = new PersistentSet();
			
			cl2 = Classification(_ctx.meta_mergeExternalData(cl2));
			
			var cl1:Classification = new Classification();
			cl1.uid = "CL1";
			cl1.subclasses = new ArrayCollection();
			cl1.superclasses = new ArrayCollection();
			
			cl1.subclasses.addItem(cl2);
			cl2.superclasses.addItem(cl1);
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("Changes", 1, changeSet.length);
			var change:Change = null;
			for each (var ch:Change in changeSet.changes) {
				if (ch.className == getQualifiedClassName(Classification)) {
					change = ch;
					break;
				}
			}
			var cc:CollectionChange = change.changes.superclasses.changes[0] as CollectionChange;
			Assert.assertEquals("Add", 1, cc.type);
			Assert.assertTrue("Classification", cc.value is Classification);
			Assert.assertEquals("Classification uid", "CL1", cc.value.uid);
			var cl:Classification = cc.value.subclasses.getItemAt(0) as Classification;
			Assert.assertFalse("Child class uninitialized", cl.meta::isInitialized());
			Assert.assertEquals("Child class detached state", "CL2DS", cl.meta::detachedState);
		}
    }
}
