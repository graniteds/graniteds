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
package org.granite.gravity.channels {

    import flash.events.Event;
	//import flash.events.IOErrorEvent;
    import flash.utils.ByteArray;
    
    import mx.logging.Log;
    import mx.logging.ILogger;

    import mx.messaging.MessageResponder;
    import mx.messaging.messages.IMessage;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.AsyncMessage;
    import mx.utils.ObjectUtil;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class GravityStreamCommand extends GravityStream {

        ///////////////////////////////////////////////////////////////////////
        // Fields.

        private static var log:ILogger = Log.getLogger("org.granite.gravity.channels.GravityStreamCommand");

        private static const DS_SUPPORTED_CONNECTION_TYPE_KEY:String = 'DSSupportedConnectionType';
        private static const CONNECTION_TYPE_LONG_POLLING:String = 'long-polling';
        private static const CONNECTION_TYPE_STREAMING:String = 'streaming';
        private static const DISCONNECT_OPERATION:int = 21;

        private static const RECONNECT_INTERVAL_MS_KEY:String = "reconnect-interval-ms";
        private static const RECONNECT_MAX_ATTEMPTS_KEY:String = "reconnect-max-attempts";
		private static const ENCODE_MESSAGE_BODY_KEY:String = "encode-message-body";

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function GravityStreamCommand(channel:GravityChannel) {
            super(channel);
        }

        ///////////////////////////////////////////////////////////////////////
        // Public operations.

        override public function connect(uri:String):void {
            super.connect(uri);

            var message:CommandMessage = createCommandMessage(CommandMessage.CLIENT_PING_OPERATION);
            message.headers[DS_SUPPORTED_CONNECTION_TYPE_KEY] = [
                /*CONNECTION_TYPE_STREAMING,*/
                CONNECTION_TYPE_LONG_POLLING
            ];

            internalQueue(new StreamMessageResponder(message, this));
        }

        override public function disconnect():void {
            var message:CommandMessage = createCommandMessage(DISCONNECT_OPERATION);
            internalQueue(new StreamMessageResponder(message, this));
        }

        override protected function internalQueue(messageResponder:MessageResponder, send:Boolean = true):void {
            var message:IMessage = messageResponder.message;
            if (message && !(message is CommandMessage) && message.body != null) {
				if (channel.encodeMessageBody) {
	            	message.headers[BYTEARRAY_BODY_HEADER] = true;
	                var data:ByteArray = new ByteArray();
	                data.writeObject(message.body);
	                message.body = data;
				}
            }
            super.internalQueue(messageResponder, send);
        }

		internal function forceDisconnect():void {
			super.disconnect();
		}

        internal function send(messageResponder:MessageResponder):void {
            internalQueue(messageResponder);
        }

        ///////////////////////////////////////////////////////////////////////
        // Package protected handlers.

        override internal function internalResult(request:IMessage, response:IMessage):void {
            if (request is CommandMessage) {
                var command:CommandMessage = (request as CommandMessage);
                if (command.operation == CommandMessage.CLIENT_PING_OPERATION) {
                	if (response.body != null) {
                		if (response.body[RECONNECT_INTERVAL_MS_KEY] != null)
                			channel.reconnectIntervalMs = Number(response.body[RECONNECT_INTERVAL_MS_KEY]);
						if (response.body[RECONNECT_MAX_ATTEMPTS_KEY] != null)
							channel.reconnectMaxAttempts = Number(response.body[RECONNECT_MAX_ATTEMPTS_KEY]);
						if (response.body[ENCODE_MESSAGE_BODY_KEY] != null)
							channel.encodeMessageBody = Boolean(response.body[ENCODE_MESSAGE_BODY_KEY]);
                	}
                    channel.streamConnectSuccess(this, response.clientId);
                }
                else
                    super.disconnect();
            }
        }

        override internal function internalStatus(request:IMessage, response:IMessage):void {
            if (request is CommandMessage) {
                var command:CommandMessage = (request as CommandMessage);
                if (command.operation == CommandMessage.CLIENT_PING_OPERATION)
                    channel.streamConnectFailed(this, "Client." + getUnqualifiedClassName(this) + ".ConnectFailed");
                else
                    channel.streamDisconnectFailed(this, "Client." + getUnqualifiedClassName(this) + ".DisconnectFailed");
            }
        }

//		override protected function streamIoErrorListener(event:IOErrorEvent):void {
//			requeueSentMessage();
//			super.streamIoErrorListener(event);
//		}
//		
//		protected function requeueSentMessage():void {
//			if (_sent != null && _sent.length > 0) {
//				for (var i:int = 0; i < _sent.length; i++)
//					_pending.unshift(_sent[i]);
//				_sent = new Array();
//			}
//		}

        ///////////////////////////////////////////////////////////////////////
        // Listeners.

        override protected function streamCompleteListener(event:Event):void {
            try {
                var responses:Array = (stream.readObject() as Array);
                for each(var response:IMessage in responses) {

                    if (!(response is AcknowledgeMessage))
                        throw new GravityChannelError("Invalid command response type: " + ObjectUtil.toString(response));

                    var correlationId:String = (response as AcknowledgeMessage).correlationId;
                    for (var i:int = 0; i < _sent.length; i++) {
                        var responder:MessageResponder = (_sent[i] as MessageResponder);
                        if (correlationId == (responder.message as AsyncMessage).messageId) {
                            channel.callResponder(responder, response);
                            _sent.splice(i, 1);
                            break;
                        }
                    }
                }
            }
            catch (e:Error) {
                dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".Read", ObjectUtil.toString(e), event);
            	log.debug("streamCompleteListener: {0}", ObjectUtil.toString(e));
            }
            finally {
                _state = STATE_IDLE;
                _sent = new Array();
                internalSendPending();
            }
            super.streamCompleteListener(event);
        }

        ///////////////////////////////////////////////////////////////////////
        // Utilities.

        private function createCommandMessage(operation:int):CommandMessage {
            var message:CommandMessage = new CommandMessage();
            message.operation = operation;
            message.timestamp = new Date().time;
            return message;
        }
    }
}
