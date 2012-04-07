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

package org.granite.gravity.channels {

    import flash.events.*;
    import flash.net.*;
    import flash.utils.ByteArray;
    import flash.utils.Dictionary;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.Channel;
    import mx.messaging.MessageAgent;
    import mx.messaging.MessageResponder;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.events.MessageEvent;
    import mx.messaging.messages.AbstractMessage;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.utils.URLUtil;
    
    import net.gimite.websocket.WebSocket;
    import net.gimite.websocket.WebSocketEvent;
    
    import org.granite.gravity.Consumer;

	
    /**
     *	Channel implementation for the Gravity Comet based communication with serlvet containers
     *  
     * 	@author Franck WOLFF
     */
    public class WebSocketChannel extends Channel {
        
        private static var log:ILogger = Log.getLogger("org.granite.gravity.channels.WebSocketChannel");

        ///////////////////////////////////////////////////////////////////////
        // Fields.

        private var _webSocket:WebSocket = null;
        private var _clientId:String = null;

        private var _consumers:Dictionary = new Dictionary();

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function WebSocketChannel(id:String, uri:String) {
            super(id, uri);
        }

        ///////////////////////////////////////////////////////////////////////
        // Properties.

        public function get clientId():String {
            return _clientId;
        }

        override public function get protocol():String {
            return 'ws';
        }

        ///////////////////////////////////////////////////////////////////////
        // Protected operations.

        override protected function getMessageResponder(agent:MessageAgent, message:IMessage):MessageResponder {
            return new WebSocketMessageResponder(agent, message, this);
        }

        override protected function internalConnect():void {
            _webSocket = new WebSocket(1, resolveUri(), [ "gravity" ], "", "", 0, "", "");
            _webSocket.addEventListener(WebSocketEvent.OPEN, onOpen);
			_webSocket.addEventListener(WebSocketEvent.ERROR, onError);
			_webSocket.addEventListener(WebSocketEvent.CLOSE, onClose);
			_webSocket.addEventListener(WebSocketEvent.BINARY, onBinary);
        }

        override protected function internalDisconnect(rejected:Boolean = false):void {
			if (_webSocket) {
            	try {
                	_webSocket.close(1000);
                } catch (e:Error) {
                }
                _webSocket = null;
            }

            _clientId = null;
            _consumers = new Dictionary();
        }
		
		private var _sent:Dictionary = new Dictionary();

        override protected function internalSend(messageResponder:MessageResponder):void {
			_sent[messageResponder.message.messageId] = messageResponder;
			
			var message:IMessage = messageResponder.message;
			if (message.clientId == null)
				message.clientId = _clientId;
			
			var data:ByteArray = new ByteArray();
			data.writeObject([ message ]);
			data.position = 0;
            _webSocket.send(data);
        }
		
		private function onOpen(event:WebSocketEvent):void {
			
		}
		
		private function onClose(event:WebSocketEvent):void {
			
		}
		
		private function onError(event:WebSocketEvent):void {
			if (_clientId == null)
				connectFailed(ChannelFaultEvent.createEvent(this, false, event.message as String));
		}
		
		private function onBinary(event:WebSocketEvent):void {
			var data:ByteArray = ByteArray(event.message);
			data.position = 0;
			var messages:Array = data.readObject() as Array;
			if (messages != null) {
				for each (var message:IMessage in messages) {
					onMessage(message);
				}
			}
		}
		
		private function onMessage(message:IMessage):void {
			var messageResponder:MessageResponder;
			if (message is AcknowledgeMessage && AcknowledgeMessage(message).correlationId == "OPEN_CONNECTION") {
				_clientId = message.clientId;
				connectSuccess();
			}
			else if (message is AcknowledgeMessage) {
				messageResponder = _sent[AcknowledgeMessage(message).correlationId] as MessageResponder;
				if (messageResponder != null) {
					if (messageResponder.message is CommandMessage) {
						var command:CommandMessage = (messageResponder.message as CommandMessage);
						if (command.operation == CommandMessage.SUBSCRIBE_OPERATION) {
							subscriptionId = message.headers[AbstractMessage.DESTINATION_CLIENT_ID_HEADER] as String;
							consumer = messageResponder.agent as Consumer;
							
							// Remove any previous subscription since a Consumer can subscribe only once. Avoid
							// multiple re-subscription when reconnecting (server restart).
							var previousSubscriptionId:String = null;
							for (var id:String in _consumers) {
								if (consumer === _consumers[id]) {
									previousSubscriptionId = id;
									break;
								}
							}
							if (previousSubscriptionId != null)
								delete _consumers[previousSubscriptionId];
							
							_consumers[subscriptionId] = consumer;
						}
						else if (command.operation == CommandMessage.UNSUBSCRIBE_OPERATION) {
							subscriptionId = message.headers[AbstractMessage.DESTINATION_CLIENT_ID_HEADER] as String;
							delete _consumers[subscriptionId];
						}
					}
					messageResponder.result(message);
					delete _sent[AcknowledgeMessage(message).correlationId];
				}
			}
			else if (message is ErrorMessage) {
				messageResponder = _sent[AcknowledgeMessage(message).correlationId] as MessageResponder;
				if (messageResponder != null) {
					messageResponder.status(message);
					delete _sent[AcknowledgeMessage(message).correlationId];
				}
			}
			else {
				var subscriptionId:String = message.headers[AbstractMessage.DESTINATION_CLIENT_ID_HEADER] as String;
				var dispatched:Boolean = false;
				if (subscriptionId) {
					var consumer:Consumer = _consumers[subscriptionId] as Consumer;
					if (consumer) {
						var messageEvent:MessageEvent = new MessageEvent(MessageEvent.MESSAGE, false, false, message);
						consumer.dispatchEvent(messageEvent);
						dispatched = true;
					}
				}
				if (!dispatched)
					log.debug("callResponder: message not dispatched: unknown subscription {0}", subscriptionId);
			}
		}

        ///////////////////////////////////////////////////////////////////////
        // Utilities.

        private function resolveUri():String {
            return URLUtil.replaceTokens(uri);
        }
    }
}
