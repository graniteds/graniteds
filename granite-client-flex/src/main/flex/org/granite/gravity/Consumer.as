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
package org.granite.gravity {

    import mx.core.mx_internal;
    import mx.logging.Log;
    import mx.messaging.events.ChannelEvent;
    import mx.messaging.events.MessageEvent;
    import mx.messaging.messages.AbstractMessage;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;

	
	[Event(name="message", type="mx.messaging.events.MessageEvent")]

    /**
     *	Gravity-specific implementation of the Consumer message agent
     *  
     * 	@author Franck WOLFF
     */
    public class Consumer extends TopicMessageAgent {

        private var _messageType:String = null;
        private var _selector:String = null;
        private var _subscriptionId:String = null;
        private var _timestamp:Number = 0;

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function Consumer(messageType:String = "flex.messaging.messages.AsyncMessage") {
            super();
            _messageType = messageType;
            _agentType = "org.granite.gravity.Consumer";
            _log = Log.getLogger("org.granite.gravity.Consumer");
        }

        ///////////////////////////////////////////////////////////////////////
        // Properties.

        public function get selector():String {
            return _selector;
        }
        public function set selector(value:String):void {
            _selector = value;
        }

        public function get subscribed():Boolean {
            return _subscriptionId != null;
        }

        public function get timestamp():Number {
            return _timestamp;
        }
        public function set timestamp(value:Number):void {
            _timestamp = value;
        }

        ///////////////////////////////////////////////////////////////////////
        // Methods.

        public override function channelConnectHandler(event:ChannelEvent):void {
            super.channelConnectHandler(event);
            event.channel.addEventListener(MessageEvent.MESSAGE, messageHandler);
            doAuthenticate();
        }

        public override function channelDisconnectHandler(event:ChannelEvent):void {
            super.channelDisconnectHandler(event);
            event.channel.removeEventListener(MessageEvent.MESSAGE, messageHandler);
            _subscriptionId = null;
        }


        private function messageHandler(event:MessageEvent):void {
            dispatchEvent(event);
        }

        public function receive(timestamp:Number = 0):void {
            throw new Error("Not implemented");
        }


        public function subscribe(clientId:String = null):void {
            var message:CommandMessage = createCommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
            if (topic != null)
                message.headers[AsyncMessage.SUBTOPIC_HEADER] = topic;
            if (selector != null)
                message.headers[CommandMessage.SELECTOR_HEADER] = selector;
            internalSend(message);
        }

        public function resubscribe(clientId:String = null):void {
	        subscribe(clientId);
		}

        public function unsubscribe():void {
            var message:CommandMessage = createCommandMessage(CommandMessage.UNSUBSCRIBE_OPERATION);
            if (topic != null)
                message.headers[AsyncMessage.SUBTOPIC_HEADER] = topic;
            if (_subscriptionId)
                message.headers[AbstractMessage.DESTINATION_CLIENT_ID_HEADER] = _subscriptionId;
            internalSend(message);
        }


        override public function acknowledge(ackMsg:AcknowledgeMessage, msg:IMessage):void {
            if (msg is CommandMessage) {
                switch ((msg as CommandMessage).operation) {
					
                case CommandMessage.SUBSCRIBE_OPERATION:
					mx_internal::setClientId(ackMsg.clientId);
					_subscriptionId = ackMsg.headers[AbstractMessage.DESTINATION_CLIENT_ID_HEADER] as String;
                    break;
				
                case CommandMessage.UNSUBSCRIBE_OPERATION:
					mx_internal::setClientId(null);
					_subscriptionId = null;
					ackMsg.clientId = null; // Force the ack's clientId to null as well before ack'ing it.
                    break;
                }
            }

			super.acknowledge(ackMsg, msg);
        }

        override public function fault(errMsg:ErrorMessage, msg:IMessage):void {
            super.fault(errMsg, msg);
        }

        private function createCommandMessage(operation:int):CommandMessage {
            var message:CommandMessage = new CommandMessage();
            message.operation = operation;
            message.destination = destination;
            message.timestamp = (new Date()).time;
            //message.messageRefType = _messageType;
            return message;
        }
    }
}
