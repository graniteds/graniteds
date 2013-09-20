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
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Classification;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityCircularRef
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testDirtyCheckEntityCircularRef():void {
        	var parent:Classification = new Classification();
			parent.id = 1;
			parent.version = 0;
			parent.uid = "P1";
			parent.subclasses = new PersistentSet(true);
			parent.superclasses = new PersistentSet(true);
			
			_ctx.parent = _ctx.meta_mergeExternal(parent);
			parent = Classification(_ctx.parent);
        	
			var child:Classification = new Classification();
			child.id = 2;
			child.version = 0;
			child.uid = "C1";
			child.subclasses = new PersistentSet(true);
			child.superclasses = new PersistentSet(true);
			_ctx.child = _ctx.meta_mergeExternal(child);
			child = Classification(_ctx.child);
			
			Assert.assertFalse("Classification not dirty", _ctx.meta_dirty);
			
			parent.subclasses.addItem(child);
			child.superclasses.addItem(parent);
			
			Assert.assertTrue("Classification dirty", _ctx.meta_dirty);
			
			var parent2:Classification = new Classification();
			parent2.id = 1;
			parent2.version = 1;
			parent2.uid = "P1";
			parent2.subclasses = new PersistentSet(true);
			parent2.superclasses = new PersistentSet(true);
			var child2:Classification = new Classification();
			child2.id = 2;
			child2.version = 1;
			child2.uid = "C1";
			child2.subclasses = new PersistentSet(true);
			child2.superclasses = new PersistentSet(true);
			parent2.subclasses.addItem(child2);
			parent2.superclasses.addItem(parent2);
			
			var res:ArrayCollection = new ArrayCollection();
			res.addItem(parent2);
			res.addItem(child2);
			
			_ctx.meta_mergeExternal(res);
			
			Assert.assertFalse("Classification merged not dirty", _ctx.meta_dirty);
        }
		
		[Test]
		public function testDirtyCheckEntityCircularRef2():void {
			var parent:Classification = new Classification();
			parent.id = 1;
			parent.version = 0;
			parent.uid = "P1";
			parent.subclasses = new PersistentSet(true);
			parent.superclasses = new PersistentSet(true);
			
			_ctx.parent = _ctx.meta_mergeExternal(parent);
			parent = Classification(_ctx.parent);
			
			var child:Classification = new Classification();
			child.id = 2;
			child.version = 0;
			child.uid = "C1";
			child.subclasses = new PersistentSet(true);
			child.superclasses = new PersistentSet(true);
			_ctx.child = _ctx.meta_mergeExternal(child);
			child = Classification(_ctx.child);
			
			Assert.assertFalse("Classification not dirty", _ctx.meta_dirty);
			
			parent.subclasses.addItem(child);
			child.superclasses.addItem(parent);
			child.name = "test";
			
			Assert.assertTrue("Parent deep dirty", _ctx.meta_deepDirty(parent));
			Assert.assertTrue("Child deep dirty", _ctx.meta_deepDirty(child));
			
			child.superclasses.removeItemAt(0);
			parent.subclasses.removeItemAt(0);
			
			Assert.assertFalse("Parent not dirty", _ctx.meta_deepDirty(parent));
		}
    }
}
