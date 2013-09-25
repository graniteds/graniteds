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
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityEnum 
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
        public function testDirtyCheckEntityEnum():void {
        	var person:Person3 = new Person3();
        	person.version = 0;
        	person.salutation = Salutation.Dr;
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = _ctx.meta_mergeExternalData(person);
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.salutation = Salutation.Mr;
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        	
        	person.salutation = null;
        	
        	Assert.assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertTrue("Person dirty 2", personDirty);
        	Assert.assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	Assert.assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	Assert.assertFalse("Person not dirty 2", personDirty);
        	Assert.assertFalse("Context not dirty", ctxDirty);
        }
    }
}
