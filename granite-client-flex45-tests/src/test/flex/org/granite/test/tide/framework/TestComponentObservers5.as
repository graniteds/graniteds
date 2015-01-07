/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
    import org.granite.tide.events.TideUIEvent;
	import org.granite.tide.events.TideUIConversationEvent;
    
	import org.granite.test.tide.*;
    
    public class TestComponentObservers5
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversationObserver]);
        }
        
        
		[Test]
		public function testComponentObservers():void {
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent"));			
			var ctx:BaseContext = Tide.getInstance().getContext("test");					        	
			Assert.assertEquals("Observer untyped event", 1, ctx.myComponentObserver.untypedEvent);
			
			_ctx.dispatchEvent(new MyConversationEvent("toto"));			
			Assert.assertEquals("Observer typed event ", 1, ctx.myComponentObserver.typedEvent);			
		}
		
        [Test]
        public function testComponentObserversGDS840():void {
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent2", callback));
        }
		
		private function callback():void {
		}
		
		[Test]
		public function testComponentObserversGDS842():void {
			var person:Person0 = new Person0(10, "Toto", "Tutu");
			_ctx.person = person;
			_ctx.dispatchEvent(new TideUIConversationEvent("test", "someEvent3", [person]));			
			var ctx:BaseContext = Tide.getInstance().getContext("test");
			Assert.assertFalse("Person copied in conversation context", person === ctx.myComponentObserver.arg);
			Assert.assertStrictlyEquals("Person uid copied in conversation context", person.uid, ctx.myComponentObserver.arg.uid);
		}
    }
}
