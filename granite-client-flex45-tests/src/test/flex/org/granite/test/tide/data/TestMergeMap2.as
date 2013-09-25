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
    import flash.utils.ByteArray;
    
    import org.flexunit.Assert;
    
    import org.granite.collections.BasicMap;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeMap2 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeMap2():void {
        	var ba:ByteArray = new ByteArray();
        	ba.writeObject([Salutation.Dr, Salutation.Mr, Salutation.Ms]);
        	ba.position = 0;
        	var s:Array = ba.readObject() as Array;
        	
        	var map:BasicMap = new BasicMap();
        	map.put(Salutation.Dr, new Person0(1, "A1", "B1"));
        	map.put(Salutation.Mr, new Person0(2, "A2", "B2"));
        	map.put(Salutation.Ms, new Person0(3, "A3", "B3"));
        	_ctx.meta_mergeExternalData(map);
        	
        	var map2:BasicMap = new BasicMap();
        	map2.put(s[0], new Person0(1, "A1", "B1"));
        	map2.put(s[1], new Person0(3, "A3", "B3"));
        	map2.put(s[2], new Person0(4, "A4", "B4"));
        	_ctx.meta_mergeExternalData(map2, map);
        	
        	Assert.assertEquals("Size", 3, map.length);
        	Assert.assertEquals("Element Dr", 1, map.get(Salutation.Dr).id);
        	Assert.assertEquals("Element Mr", 3, map.get(Salutation.Mr).id);
        	Assert.assertEquals("Element Ms", 4, map.get(Salutation.Ms).id);
        }
    }
}
