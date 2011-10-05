/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2010 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.test.tide.cdi {

    import flash.utils.flash_proxy;
    import flash.utils.Dictionary;
    import flash.events.Event;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ItemResponder;
    import mx.core.Application;
    import mx.events.PropertyChangeEvent;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.object_proxy;
    
    import org.granite.events.SecurityEvent;
    import org.granite.tide.Tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IEntity;
    import org.granite.tide.IIdentity;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.seam.Seam;
    
    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
	[RemoteClass(alias="org.granite.tide.test.cdi.HelloBean")]
    public class HelloBean extends Component {

        [Bindable]
        public function get name():String {
            return super.getProperty("name") as String;
        }
        public function set name(name:String):void {
            super.meta_internalSetProperty("name", name, true, false);
        }

        [Bindable(event="propertyChange")]
        public function get message():String {
            return super.getProperty("message") as String;
        }
		
		public function hello(resultHandler:Object = null, faultHandler:Function = null):void {
			if (faultHandler == null)
				super.callProperty("hello", resultHandler);
			else
				super.callProperty("hello", resultHandler, faultHandler);
		}

        public function helloEvent(resultHandler:Object = null, faultHandler:Function = null):void {
            if (faultHandler == null)
                super.callProperty("helloEvent", resultHandler);
            else
                super.callProperty("helloEvent", resultHandler, faultHandler);
        }
    }
}
