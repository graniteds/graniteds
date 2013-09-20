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
	import org.granite.test.tide.Contact;
    
    
    public class TestComponentInjectionGDS482
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentInjectGDS482]);
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS482():void {
        	var contact:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact;
        	
        	_ctx.subcontext.myComponentInject;	// Initialize component
        	
        	Assert.assertStrictlyEquals("Injected 1", contact, _ctx.subcontext.myComponentInject.injectedSetter);
        	
        	var contact2:Contact = new Contact();
        	
        	_ctx.subcontext.injectedSetter = contact2;
        	
        	Assert.assertStrictlyEquals("Injected 2", contact2, _ctx.subcontext.myComponentInject.injectedSetter);
        }
    }
}
