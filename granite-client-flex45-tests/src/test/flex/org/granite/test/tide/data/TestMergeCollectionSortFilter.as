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
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
	import mx.collections.Sort;
	import mx.collections.SortField;

import org.granite.test.tide.Person;

import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollectionSortFilter
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollectionSort():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B1"));
        	coll.addItem(new Person0(3, "A3", "B1"));
			coll.addItem(new Person0(4, "A4", "B1"));
			coll.sort = new Sort();
			coll.sort.fields = [ new SortField("lastName") ];
			coll.refresh();
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(4, "A4", "B1"));
        	coll2.addItem(new Person0(1, "A1", "B1"));
        	coll2.addItem(new Person0(2, "A2", "B1"));
        	coll2.addItem(new Person0(3, "A3", "B1"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element count", 4, coll.length);
        }

        [Test]
        public function testMergeCollectionFilter():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B1"));
        	coll.addItem(new Person0(3, "A3", "B2"));
			coll.addItem(new Person0(4, "A4", "B2"));
			coll.filterFunction = function(item:Person0):Boolean { return item.lastName == "B1"; };
			coll.refresh();
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
            var p3:Person0 = Person0(coll.list.getItemAt(2));
            var p4:Person0 = Person0(coll.list.getItemAt(3));

        	var coll2:ArrayCollection = new ArrayCollection();
            coll2.addItem(new Person0(1, "Z1", "B1"));
            coll2.addItem(new Person0(2, "Z2", "B1"));
            coll2.addItem(new Person0(3, "Z3", "B2"));
            coll2.addItem(new Person0(4, "Z4", "B2"));
        	_ctx.meta_mergeExternalData(coll2, coll);

        	Assert.assertEquals("Element count", 4, coll.list.length);
            Assert.assertEquals("Element 3 value", "Z3", p3.firstName);
            Assert.assertEquals("Element 4 value", "Z4", p4.firstName);
        }
    }
}
