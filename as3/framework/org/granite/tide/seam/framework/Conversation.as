/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.tide.seam.framework {

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
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IEntity;
    import org.granite.tide.IIdentity;
    import org.granite.tide.seam.Seam;
    
    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
    /**
     * 	Implementation of the conversation component for Seam services.<br/>
     *  Integrates with the server-side conversation component of Seam.<br/>
     *  <br/>
     *  It is used to define the current conversation description :<br/>
     *  <pre>
     *  conversation.description = 'Current task: ';
     *  someComponent.someConversationalMethod();
     *  </pre>
     * 
     * 	@author William DRAI
     */
    public class Conversation extends Component {
        
        public override function meta_init(componentName:String, context:BaseContext):void {
            super.meta_init(componentName, context);
        }
        
        
        /**
         *  Description
         */
        public function get description():String {
            return super.getProperty("description") as String;
        }
        /**
         * 	Description
         */
        public function set description(description:String):void {
            super.setProperty("description", description);
            _context.conversationList.updateConversationDescription(_context.contextId, description);
        }
        
        
        public function setDescription(description:String):* {
        	this.description = description;
        	return super.callProperty("setDescription", description);
        }
    }
}
