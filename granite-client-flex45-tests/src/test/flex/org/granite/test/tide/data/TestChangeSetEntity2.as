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
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.collections.BasicMap;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    import org.granite.tide.data.CollectionChanges;
    
    
    public class TestChangeSetEntity2 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testChangeSetEntity2():void {
        	var person:Person9 = new Person9();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
        	person.testMap = new BasicMap();
			var key:Key = new Key();
			key.uid = "K1";
			key.id = 1;
			key.version = 0;
			key.name = "K1";
			var value:Value = new Value();
			value.uid = "V1";
			value.id = 1;
			value.version = 0;
			value.name = "V1";
			person.testMap.put(key, value);
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.length);
			
			var key2:Key = new Key();
			key2.uid = "K2";
			key2.id = 2;
			key2.version = 0;
			key2.name = "K2";
			key2 = _ctx.meta_mergeExternal(key2) as Key;
			
			var value2:Value = new Value();
			value2.uid = "V2";
			value2.name = "V2";
			person.testMap.put(key2, value2);
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count", 1, changeSet.length);
			var coll:CollectionChanges = changeSet.getChange(0).changes.testMap as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(0).type);
			Assert.assertTrue("ChangeSet collection key", coll.getChange(0).key is ChangeRef);
			Assert.assertEquals("ChangeSet collection key", key2.uid, coll.getChange(0).key.uid);
			Assert.assertEquals("ChangeSet collection value", value2.uid, coll.getChange(0).value.uid);

			var value3:Value = new Value();
			value3.uid = "V3";
			value3.id = 3;
			value3.version = 0;
			value3.name = "V3";
			value3 = _ctx.meta_mergeExternal(value3) as Value;
			person.testMap.put(key2, value3);
						
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count", 1, changeSet.length);
			coll = changeSet.getChange(0).changes.testMap as CollectionChanges;
			Assert.assertEquals("ChangeSet collection", 1, coll.length);
			Assert.assertEquals("ChangeSet collection type", 1, coll.getChange(0).type);
			Assert.assertTrue("ChangeSet collection key", coll.getChange(0).key is ChangeRef);
			Assert.assertEquals("ChangeSet collection key", key2.uid, coll.getChange(0).key.uid);
			Assert.assertTrue("ChangeSet collection value", coll.getChange(0).value is ChangeRef);
			Assert.assertEquals("ChangeSet collection value", value3.uid, coll.getChange(0).value.uid);
		}
    }
}
