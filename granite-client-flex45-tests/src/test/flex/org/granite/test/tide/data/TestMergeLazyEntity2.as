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
    
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Group;
    import org.granite.test.tide.User;
    
    
    public class TestMergeLazyEntity2 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeLazyEntity2():void {
        	var user:User = new User();
        	user.username = 'toto';
        	user.meta::setInitialized(false);
        	
        	var group:Group = new Group();
        	group.name = 'tutu';
        	group.user = user;
        	
        	_ctx.group = _ctx.meta_mergeExternal(group);
        	group = _ctx.group;
        	
        	Assert.assertFalse("User not initialized", group.user.meta::isInitialized());
        	
        	Assert.assertNull("User not init 2", group.user.name);
        	
        	var user2:User = new User();
        	user2.username = 'toto';
        	user2.name = 'Jean Richard';
        	var group2:Group = new Group();
        	group2.name = 'tutu'
        	group2.user = user2;
        	
        	_ctx.meta_mergeExternal(group2);
        	
        	Assert.assertTrue("User initialized", group.user.meta::isInitialized());
        }
    }
}
