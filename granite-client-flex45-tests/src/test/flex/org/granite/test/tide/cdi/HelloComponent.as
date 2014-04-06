/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
    /**
     * 	Implementation of the identity component for Seam services.<br/>
     *  Integrates with the server-side identity component of Seam.<br/>
     *  <br/>
     *  The basic way of using it is by populating username/password and calling login :<br/>
     *  <pre>
     *  identity.username = 'john';
     *  identity.password = 'doe';
     *  identity.login();
     *  </pre>
     * 
     * 	@author William DRAI
     */
	[RemoteClass(alias="org.granite.tide.test.cdi.HelloComponent")]
    public class HelloComponent extends Component {
        
        public function get name():String {
            return super.getProperty("name") as String;
        }
        public function set name(name:String):void {
            super.setProperty("name", name);
        }
		
		public function hello(resultHandler:Object = null, faultHandler:Function = null):void {
			if (faultHandler == null)
				super.callProperty("hello", resultHandler);
			else
				super.callProperty("hello", resultHandler, faultHandler);
		}
    }
}
