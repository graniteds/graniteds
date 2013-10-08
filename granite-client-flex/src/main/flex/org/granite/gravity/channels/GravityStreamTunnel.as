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
package org.granite.gravity.channels {

    import flash.events.Event;
    import flash.events.IOErrorEvent;
    import flash.events.TimerEvent;
    import flash.utils.ByteArray;
    import flash.utils.Timer;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.MessageResponder;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.utils.ObjectUtil;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class GravityStreamTunnel extends GravityStream {
        
        ///////////////////////////////////////////////////////////////////////
        // Fields.

        private static var log:ILogger = Log.getLogger("org.granite.gravity.channels.GravityStreamTunnel");

        protected static const CONNECT_OPERATION:uint = 20;

		private var _reconnectCount:Number = 0;
		private var _reconnectTimer:Timer = null;
		private var _connecting:Boolean = false;

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function GravityStreamTunnel(channel:GravityChannel) {
            super(channel);
        }

        ///////////////////////////////////////////////////////////////////////
        // Public operations.

        override public function connect(uri:String):void {
            super.connect(uri);
			internalConnect();
        }

        protected function reconnect(onError:Boolean = false):void {
        	if (!onError) {
        		cancelReconnectTimer();
        		internalConnect();
        	}
        	else if (_reconnectCount == 0 && channel.reconnectMaxAttempts > 0) {
        		_reconnectCount++;
        		internalConnect();
        	}
        	else if (_reconnectCount < channel.reconnectMaxAttempts) {
        		if (_reconnectTimer == null) {
	        		_reconnectCount++;
	        		_reconnectTimer = new Timer(channel.reconnectIntervalMs, 1);
	        		_reconnectTimer.addEventListener(TimerEvent.TIMER_COMPLETE, timerCompleteHandler, false, 0, true);
	        		_reconnectTimer.start();
        		}
            }
            else {
            	dispatchFaultEvent(
            		"Client." + getUnqualifiedClassName(this) + ".ReconnectErrorMax",
            		"Reconnect attempts reached maximum: " + channel.reconnectMaxAttempts + " (giving up)"
            	);
            	try {
            		// cleanup...
            		disconnect();
            	} catch (e:Error) {
            		// ignore...
            	}
            }
        }
        
        override public function disconnect():void {
        	cancelReconnectTimer();
        	super.disconnect();
        }
        
        private function timerCompleteHandler(e:TimerEvent):void {
        	_reconnectTimer = null;
			internalConnect();
        }
		
		protected function createConnectMessageResponder(connectMessage:CommandMessage):StreamMessageResponder {
			return new StreamMessageResponder(connectMessage, this);
		}
        
        private function internalConnect():void {
        	if (!_connecting) {
        		_connecting = true;

	            // clear any previous connect messages to avoid multiple
	            // reconnection after successive attempts.
	        	_pending = new Array();

	            var message:CommandMessage = createCommandMessage(CONNECT_OPERATION);
				var responder:StreamMessageResponder = createConnectMessageResponder(message);
				if (responder)
	            	internalQueue(responder);
        		
				_connecting = false;
            }
        }
        
        internal function cancelReconnectTimer():void {
        	_reconnectCount = 0;
    		if (_reconnectTimer != null) {
    			try {
    				if (_reconnectTimer.running)
    					_reconnectTimer.stop();
    			}
    			finally {
    				_reconnectTimer = null;
    			}
    		}
        }

        ///////////////////////////////////////////////////////////////////////
        // Listeners.

        /*
         * TODO: Streaming not working (flash bug with progress events)...
         *
        override protected function streamProgressListener(event:ProgressEvent):void {
            if (event.bytesLoaded >= event.bytesTotal) {
                try {
                    var responses:Array = (stream.readObject() as Array);
                    for each(var response:IMessage in responses) {
                        log.debug("streamProgressListener: {0}/{1}", ObjectUtil.toString(response), _sent.length);
                        var correlationId:String = (response as AsyncMessage).correlationId;
                        for (var i:int = 0; i < _sent.length; i++) {
                            var responder:MessageResponder = (_sent[i] as MessageResponder);
                            if (correlationId == (responder.message as AsyncMessage).messageId) {
                                channel.callResponder(responder, response);
                                //_sent.splice(i, 1);
                                break;
                            }
                        }
                    }
                }
                catch (e:Error) {
                    log.warn("streamProgressListener: {0}", ObjectUtil.toString(e));
                }
            }
            super.streamProgressListener(event);
        }
        */
		
		protected function reconnectOnStreamResult():Boolean {
			return true;
		}

        override protected function streamCompleteListener(event:Event):void {
            var error:Boolean = false;
            
            try {
	            if (bytesAvailable > 0) {
	                var responder:MessageResponder = (_sent.pop() as MessageResponder);
	                var sentMessageId:String = (responder.message as AsyncMessage).messageId;
	                var responses:Array = (stream.readObject() as Array);
	                for each(var response:IMessage in responses) {
	                    // var correlationId:String = (response as AsyncMessage).correlationId;
	                    // if (correlationId == sentMessageId)
	                    if (response is ErrorMessage) {
	                    	channel.callResponder(responder, response);
	                    	error = true;
	                    	break;
	                    }
	                    if (response.headers[BYTEARRAY_BODY_HEADER])
	                        response.body = ByteArray(response.body).readObject();
	                    channel.callResponder(responder, response);
	                }
	            }
            }
            catch (e:Error) {
            	dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".Read", ObjectUtil.toString(e), event);
                log.debug("streamCompleteListener: {0}", ObjectUtil.toString(e));
            }
            finally {
            	_state = STATE_IDLE;
            }

            super.streamCompleteListener(event);
            
            if (!error && reconnectOnStreamResult())
            	reconnect();
        }

        override protected function streamIoErrorListener(event:IOErrorEvent):void {
            super.streamIoErrorListener(event);
            reconnect(true);
        }

        ///////////////////////////////////////////////////////////////////////
        // Package protected handlers.

        override internal function internalResult(request:IMessage, response:IMessage):void {
            channel.streamConnectSuccess(this, response.clientId);
        }

        override internal function internalStatus(request:IMessage, response:IMessage):void {
            channel.streamConnectFailed(this, "Client." + getUnqualifiedClassName(this) + ".ConnectFailed");
        }

        ///////////////////////////////////////////////////////////////////////
        // Private utilities.

        private function createCommandMessage(operation:int):CommandMessage {
            var message:CommandMessage = new CommandMessage();
            message.operation = operation;
            message.timestamp = new Date().time;
            return message;
        }
    }
}
