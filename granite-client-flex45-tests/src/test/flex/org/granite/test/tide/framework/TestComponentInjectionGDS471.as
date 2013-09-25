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
package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    
    
    public class TestComponentInjectionGDS471
    {
        private var _ctx:BaseContext;
        
  		[In]
  		public var application:Object;
            
  		[In(create="false")]
  		public var globalOutjectedVariable:String;
  		         
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        	Tide.getInstance().addComponents([MyComponentGlobalGDS471, MyComponentLocalGDS471]);
        	_ctx.test = this;
        }
        
        
        [Test]
        public function testComponentInjectionGDS471():void {
        	application.dispatchEvent(new TideUIEvent("step1Global"));
        	
        	application.dispatchEvent(new TideUIConversationEvent("someConv", "step1Conv"));
        	
			Assert.assertEquals("Global", "global string", globalOutjectedVariable);
			Assert.assertStrictlyEquals("Global", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        	
        	application.dispatchEvent(new TideUIEvent("step2Global"));
        	
			Assert.assertEquals("Global", "global 2 string", globalOutjectedVariable);
			Assert.assertStrictlyEquals("Global 2", globalOutjectedVariable, Tide.getInstance().getContext("someConv").myComponentLocal.globalOutjectedVariable);
        }
    }
}
