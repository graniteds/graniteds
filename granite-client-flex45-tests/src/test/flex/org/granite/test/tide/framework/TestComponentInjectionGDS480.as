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
package org.granite.test.tide.framework
{
	import org.flexunit.Assert;
	
	import mx.core.Application;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.Tide;
	import org.granite.tide.seam.Seam;
    
    
    public class TestComponentInjectionGDS480
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
  		         
        
        [Before]
        public function setUp():void {
            Seam.resetInstance();
            _ctx = Seam.getInstance().getContext();
            Seam.getInstance().initApplication();
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS480():void {
        	// Create proxy
        	_ctx.myComponentInject2;
        	
        	Assert.assertTrue("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	Assert.assertTrue("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        	
        	// Replace by real component
        	_ctx.myComponentInject2 = new MyComponentInject2();
        	
        	Assert.assertTrue("Instance", _ctx.myComponentInject2 is MyComponentInject2);
        	Assert.assertFalse("Restricted", Seam.getInstance().isComponentRestrict("myComponentInject2"));
        	Assert.assertFalse("Remote", Seam.getInstance().getComponentRemoteSync("myComponentInject2") == Tide.SYNC_BIDIRECTIONAL);
        }
    }
}
