/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollection2 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollection2():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B2"));
        	coll.addItem(new Person0(3, "A3", "B3"));
        	coll.addItem(new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(coll);
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(5, "A5", "B5"));
        	coll2.addItem(new Person0(4, "A4", "B4"));
        	coll2.addItem(new Person0(3, "A3", "B3"));
        	coll2.addItem(new Person0(2, "A2", "B2"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element 0", 5, coll.getItemAt(0).id);
        	Assert.assertEquals("Element 2", 3, coll.getItemAt(2).id);
        	Assert.assertEquals("Element 3", 2, coll.getItemAt(3).id);
        }
    }
}
