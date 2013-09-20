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
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
	import mx.events.PropertyChangeEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityArray 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeEntityArray():void {
			var a1:EmbeddedAddress = new EmbeddedAddress();
			a1.address1 = "12 Main Street";
        	var p1:Person5 = new Person5();
			p1.id = 1;
			p1.uid = "P1";
			p1.version = 0;
			p1.address = [ a1 ];
			_ctx.meta_mergeExternalData(p1);
			
        	var p2:Person5 = new Person5();
			p2.id = 1;
			p2.uid = "P1";
			p2.version = 1;
			p2.address = null;
        	var p:Person5 = _ctx.meta_mergeExternalData(p2) as Person5;
			
			Assert.assertNull("Array address merged", p.address);
			
			var a2a:EmbeddedAddress = new EmbeddedAddress();
			a2a.address1 = "12 Main Street";
			var a2b:EmbeddedAddress = new EmbeddedAddress();
			a2b.address1 = "13 Main Street";
			var p3:Person5 = new Person5();
			p3.id = 1;
			p3.uid = "P1";
			p3.version = 2;
			p3.address = [ a2a, a2b ];
			_ctx.meta_mergeExternalData(p3);
			
			Assert.assertEquals("Array address merged 2", 2, p.address.length);
        }
    }
}
