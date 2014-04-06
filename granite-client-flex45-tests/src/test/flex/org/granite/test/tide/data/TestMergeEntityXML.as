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
    import flash.utils.ByteArray;
    
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestMergeEntityXML 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeEntityXML():void {
        	var s:ByteArray = new ByteArray();
        	s.writeInt(38);
        	s.position = 0;
        	var p:Person8 = new Person8();
        	p.id = 1;
        	p.version = 0;
        	p.uid = "P01";
        	p.lastName = "test";
        	p.contacts = <contacts><emails><email value="toto@tutu.net"/><email value="tutu@tyty.com"/></emails></contacts>;
        	p.salutation = s;
        	p = _ctx.meta_mergeExternalData(p) as Person8;
        	
        	var s2:ByteArray = new ByteArray();
        	s2.writeInt(89);
        	s2.position = 0;
        	var p2:Person8 = new Person8();
        	p2.id = 1;
        	p2.version = 1;
        	p2.uid = "P01";
        	p2.lastName = "test";
        	p2.contacts = <contacts><emails><email value="tutu@tyty.com"/></emails></contacts>;
        	p2.salutation = s2;
        	_ctx.meta_mergeExternalData(p2, p);
        	
        	Assert.assertEquals("XML merged", 1, p.contacts.emails.email.length());
        	
			p.salutation.position = 0;
        	Assert.assertEquals("ByteArray merged", 89, p.salutation.readInt());
        }
    }
}
