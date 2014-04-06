/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam.framework {

    import flash.utils.flash_proxy;
    import flash.utils.Dictionary;
    import flash.events.Event;
    import flash.events.EventDispatcher;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.utils.ObjectProxy;
    import mx.collections.ArrayCollection;
    import mx.collections.ItemResponder;
    import mx.collections.IList;
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
    import org.granite.tide.IComponent;
    import org.granite.tide.IIdentity;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.TideMessage;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.seam.Seam;
    import org.granite.tide.seam.Context;
    import org.granite.tide.service.ServerSession;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
    /**
     * 	The StatusMessages component gives access to the current list of status messages.<br/>
     *  The messages are either received from the server or added locally by the addMessage or addMessageToControl method.<br/> 
     *  <br/>
     * 	The StatusMessages component is by default bound to the context with the name 'statusMessages'.<br/>
     *  <li>messages is an ArrayCollection of TideMessage that can be retrieved or bound from tideContext.statusMessages.messages.</li>
     *  <li>keyedMessages is an Map of ArrayCollections of TideMessages that can be retrieved or bound from tideContext.statusMessages.keyedMessages.</li>
     *  These message lists are bindable properties that can be used as providers for Repeaters or Lists.
     * 
     * 	@author William DRAI
     */
    public class StatusMessages extends EventDispatcher implements IComponent {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.framework.StatusMessages");
       	
        private var _messages:IList = null;
        private var _keyedMessages:ObjectProxy = new ObjectProxy();
        
        
        public function StatusMessages() {
        }
        
        public function meta_init(componentName:String, context:BaseContext):void {
        }

        public function set serverSession(serverSession:ServerSession):void {
        }
        
        
        public function get meta_name():String {
        	return "statusMessages";
        }
        
        /**
         * 	List of global messages
         */
        public function get messages():IList {
            return _messages;
        }
        public function set messages(messages:IList):void {
        	_messages = messages;
       	}
        
        /**
         * 	Return true is there is any available message
         *  
         *  @return true is at least one global message
         */ 
        public function hasMessage():Boolean {
        	return _messages && _messages.length > 0;
        }
        
        /**
         * 	Map of messages keyed by control id
         */
        public function get controlMessages():Object {
        	return _keyedMessages;
        }
        
        /**
         * 	Return true is there is any available message for the specified controlId
         *  
         *  @return true is at least one message
         */ 
        public function hasMessageForControl(controlId:String):Boolean {
        	return _keyedMessages[controlId] && _keyedMessages[controlId].length > 0;
        }
        
        private function initMessages():void {
        	if (_messages == null)
        		messages = new ArrayCollection();
        }
        
        private function initKeyedMessages(controlId:String):void {
        	if (_keyedMessages[controlId] == null)
        		_keyedMessages[controlId] = new ArrayCollection();
        }
        
        
        public function meta_clear():void {
        	destroy();
        }
        
        
        /**
         * 	@private
         */
        public function destroy():void {
        	messages = null;
        	for (var controlId:String in _keyedMessages)
        		controlMessages[controlId] = null;
        }
        
        
        /**
         * 	@private
         */
        public function setFromServer(result:Object):void {
        	if (result.messages !== undefined) {
	            // Must keep this to allow Flash VM to correctly deserialize TideMessages
	            for (var im:int = 0; im < result.messages.length; im++) {
	                var msg:TideMessage = result.messages.getItemAt(im) as TideMessage;
	                log.debug("result message {0}", msg.summary);
	            }
	            
	            messages = result.messages;
	        }
	        else
	        	messages = new ArrayCollection();
	        
	        var controlId:String;
        	for (controlId in _keyedMessages)
        		controlMessages[controlId] = null;
        	
	        if (result.keyedMessages !== undefined) {
	        	for (controlId in result.keyedMessages)
	        		controlMessages[controlId] = result.keyedMessages[controlId];
	        } 
        }

        
        /**
         *	Clear global messages
         */ 
        public function clearMessages():void {
        	initMessages();
        	_messages.removeAll();
        }
        
        /**
         *	Clear messages for specified control
         *  @param controlId control id
         */ 
        public function clearMessagesForControl(controlId:String):void {
        	_keyedMessages[controlId] = null;
        	delete _keyedMessages[controlId];
        }
        
        /**
         *	Clear messages for all controls
         */ 
        public function clearControlMessages():void {
        	for (var controlId:String in controlMessages) {
        		_keyedMessages[controlId] = null;
        		delete _keyedMessages[controlId];
        	}
        }
        
        /**
         *	Clear all messages (messages and control messages)
         */ 
        public function clearAllMessages():void {
        	clearMessages();
        	clearControlMessages();
        }
        
        
        /**
         * 	Add a global message in the current list
         * 	@param severity severity
         * 	@param summary summary
         * 	@param details details
         */
        public function addMessage(severity:String, summary:String, details:String = ""):void {
        	initMessages();
        	_messages.addItem(new TideMessage(severity, summary, details));
        }
        
        /**
         * 	Add a message in the current list of the specified control id
         *  @param controlId control id
         * 	@param severity severity
         * 	@param summary summary
         * 	@param details details
         */
        public function addMessageToControl(controlId:String, severity:String, summary:String, details:String = ""):void {
        	initKeyedMessages(controlId);
        	_keyedMessages[controlId].addItem(new TideMessage(severity, summary, details));
        }
                
        
        public static function instance():StatusMessages {
            return Seam.getInstance().getSeamContext().statusMessages as StatusMessages;
        }
    }
}
