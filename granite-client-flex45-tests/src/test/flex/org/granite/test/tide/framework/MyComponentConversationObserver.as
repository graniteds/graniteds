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
package org.granite.test.tide.framework {
	
	import org.flexunit.Assert;
	import org.granite.tide.events.TideContextEvent;
	

	[Bindable]
	[Name("myComponentObserver")]
    public class MyComponentConversationObserver {
    	
    	public var untypedEvent:int = 0;
    	public var typedEvent:int = 0;
		public var arg:Object = null;
    	
    	
    	[Observer]
    	public function myHandler(event:MyConversationEvent):void {
			Assert.assertEquals("Conversation event param", "toto", event.obj);
    		typedEvent++;
    	}
    	
    	[Observer("someEvent")]
    	public function myHandler2(event:TideContextEvent):void {
    		untypedEvent++;
    	}
		
		[Observer("someEvent2")]
		public function myHandler3(callback:Function):void {
			Assert.assertNotNull("Callback function passed to conversation", callback);
		}
		
		[Observer("someEvent3")]
		public function myHandler4(args:Array):void {
			Assert.assertEquals("Args array passed to conversation", 1, args.length);
			this.arg = args[0];
		}
    }
}
