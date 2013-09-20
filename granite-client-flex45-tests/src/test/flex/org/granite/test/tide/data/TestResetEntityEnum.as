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
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityEnum 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityEnum():void {
        	var person:Person6 = new Person6();
        	person.version = 0;
        	person.salutation = Salutation.Mr;
        	person.salutations = new ArrayCollection([ Salutation.Dr ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
        	person.salutation = null;
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertStrictlyEquals("Person reset", Salutation.Mr, person.salutation);
			
			person.salutation = Salutation.Dr;
			_ctx.meta_resetEntity(person);
			
			Assert.assertStrictlyEquals("Person reset 2", Salutation.Mr, person.salutation);
			
			person.salutations.removeItemAt(0);
			person.salutations.addItem(Salutation.Mr);
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person reset coll", 1, person.salutations.length);
			Assert.assertStrictlyEquals("Person reset coll", Salutation.Dr, person.salutations.getItemAt(0));
        }
    }
}
