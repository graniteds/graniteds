/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.tide.seam {
	
    import org.granite.tide.Tide;
    import org.granite.tide.Component;
    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.seam.security.Identity;
    import org.granite.tide.seam.framework.StatusMessages;
    import org.granite.tide.seam.framework.ConversationList;
    import org.granite.tide.service.ServerSession;

    [Bindable]
    /**
     * 	Implementation of the Tide singleton for Seam services
     * 
     *  @author William DRAI
     */
	public class Seam extends Tide {

        public static function getInstance(destination:String = null):Seam {
            return Tide.getInstance(destination, Seam) as Seam;
        }
		
		/**
		 *	Clear Tide singleton (should be used only for testing)
		 */
		public static function resetInstance():void {
			Tide.resetInstance();
		}
		
		protected override function init(contextClass:Class, componentClass:Class):void {
		    super.init(Context, Component);
		    addComponent("identity", Identity);
			getDescriptor("identity").scope = Tide.SCOPE_SESSION;			// Default scope for remote proxies is EVENT
		    addComponent("statusMessages", StatusMessages);
			getDescriptor("statusMessages").scope = Tide.SCOPE_SESSION;		// Default scope for remote proxies is EVENT
			getDescriptor("statusMessages").remoteSync = Tide.SYNC_SERVER_TO_CLIENT;
		    getDescriptor("statusMessages").destroyMethodName = "destroy";
		    addComponent("conversationList", ConversationList);
			getDescriptor("conversationList").scope = Tide.SCOPE_SESSION;	// Default scope for remote proxies is EVENT
			getDescriptor("conversationList").remoteSync = Tide.SYNC_SERVER_TO_CLIENT;
		    getDescriptor("conversationList").destroyMethodName = "destroy";
		    
		    addContextEventListener(ConversationList.CONVERSATION_TIMEOUT, conversationTimeoutHandler, true); 
		}

        protected override function initServerSession():ServerSession {
            return new SeamServerSession();
        }

		public function getSeamContext(conversationId:String = null):Context {
		    return super.getContext(conversationId) as Context;
		}

        private function conversationTimeoutHandler(event:TideContextEvent):void {
        	var conversationId:String = event.params[0] as String;
        	if (conversationId != null) {
        		var ctx:Context = getSeamContext(conversationId);
        		if (ctx) {
	        		// Mark timed out context as finished
	        		ctx.meta_markAsFinished();
	        	}
	        }
        }
	}
}
