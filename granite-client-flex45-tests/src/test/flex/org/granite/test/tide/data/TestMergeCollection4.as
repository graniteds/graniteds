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
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollection4 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollection4():void {
        	var p1:Person0 = new Person0(1, "A1", "B1");
        	var p2:Person0 = new Person0(2, "A2", "B2");
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(p1);
        	coll.addItem(p2);
        	coll.addItem(p2);
        	coll.addItem(p1);
        	_ctx.meta_mergeExternalData(coll);
        	
        	var p1b:Person0 = new Person0(1, "A1", "B1");
        	var p2b:Person0 = new Person0(2, "A2", "B2");
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(p1b);
        	coll2.addItem(p2b);
        	coll2.addItem(p2b);
        	coll2.addItem(p1b);
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element 0", 1, coll.getItemAt(0).id);
        	Assert.assertEquals("Element 1", 2, coll.getItemAt(1).id);
        	Assert.assertEquals("Element 2", 2, coll.getItemAt(2).id);
        	Assert.assertEquals("Element 3", 1, coll.getItemAt(3).id);
        }
    }
}
