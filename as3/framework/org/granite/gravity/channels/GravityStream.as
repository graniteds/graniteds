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

    import flash.events.Event;
    import flash.events.HTTPStatusEvent;
    import flash.events.IOErrorEvent;
    import flash.events.ProgressEvent;
    import flash.events.SecurityErrorEvent;
    import flash.events.EventDispatcher;
    import flash.net.URLStream;
    import flash.net.URLRequest;
    import flash.net.URLRequestMethod;
    import flash.net.ObjectEncoding;
    import flash.utils.ByteArray;
	import flash.utils.getQualifiedClassName;

    import mx.messaging.MessageResponder;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.IMessage;
    import mx.messaging.messages.AbstractMessage;
    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.CommandMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.utils.ObjectUtil;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class GravityStream extends EventDispatcher {

        ///////////////////////////////////////////////////////////////////////
        // Fields.
    	
    	protected static const BYTEARRAY_BODY_HEADER:String = "GDS_BYTEARRAY_BODY";

        public static const STATE_IDLE:int = 0;
        public static const STATE_LOADING:int = 1;

        private var _channel:GravityChannel = null;
        private var _uri:String = null;
        private var _stream:URLStream = null;

        protected var _state:int = STATE_IDLE;
        protected var _pending:Array = new Array();
        protected var _sent:Array = new Array();

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function GravityStream(channel:GravityChannel) {
            _channel = channel;
        }

        ///////////////////////////////////////////////////////////////////////
        // Properties.

        public function get state():int {
            return _state;
        }

        public function get channel():GravityChannel {
            return _channel;
        }

        public function get uri():String {
            return _uri;
        }

        public function get connected():Boolean {
            return _stream && _stream.connected;
        }

        protected function get stream():URLStream {
            return _stream;
        }

        protected function get bytesAvailable():uint {
            return (_stream != null ? _stream.bytesAvailable : 0);
        }

        ///////////////////////////////////////////////////////////////////////
        // Public operations.

        public function connect(uri:String):void {
            _uri = uri;

            _stream = new URLStream();
            _stream.objectEncoding = ObjectEncoding.AMF3;
            _stream.addEventListener(Event.OPEN, streamOpenListener);
            _stream.addEventListener(ProgressEvent.PROGRESS, streamProgressListener);
            _stream.addEventListener(Event.COMPLETE, streamCompleteListener);
            _stream.addEventListener(HTTPStatusEvent.HTTP_STATUS, streamHttpStatusListener);
            _stream.addEventListener(IOErrorEvent.IO_ERROR, streamIoErrorListener);
            _stream.addEventListener(SecurityErrorEvent.SECURITY_ERROR, streamSecurityErrorListener);
        }

        public function disconnect():void {
            try {
                if (_stream && _stream.connected) {
                    _stream.close();
                    _channel.streamDisconnectSuccess(this, true);
                }
            }
            catch (e:Error) {
                _channel.streamDisconnectFailed(this, "Client." + getUnqualifiedClassName(this) + ".DisconnectFailed", e);
            }
            finally {
                _stream = null;
                _pending = new Array();
                _sent = new Array();
                _state = STATE_IDLE;
                _uri = null;
            }
        }

        ///////////////////////////////////////////////////////////////////////
        // Internal operations.

        protected function internalQueue(messageResponder:MessageResponder, send:Boolean = true):void {
            if (!(messageResponder.message is AsyncMessage)) {
                throw new GravityChannelError(
                    'Invalid MessageResponder message (should be an AsyncMessage instance): ' +
                    messageResponder.message
                );
            }

            messageResponder.message.headers[AbstractMessage.ENDPOINT_HEADER] = _channel.id;

            if (messageResponder.message is CommandMessage &&
                CommandMessage(messageResponder.message).operation == CommandMessage.LOGIN_OPERATION)
                _pending.unshift(messageResponder);
            else
                _pending.push(messageResponder);

            if (send)
                internalSendPending();
        }

        protected function internalSendPending():void {

            function extractMessage(responder:MessageResponder, index:int, arr:Array):IMessage {
                responder.message.clientId = _channel.clientId;
                return responder.message;
            }

            if (_state == STATE_IDLE && _pending && _pending.length > 0) {
                _state = STATE_LOADING;
                try {
                    var messages:Array = _pending.map(extractMessage);
                    _sent = _pending;
                    _pending = new Array();

                    var request:URLRequest = createURLRequest(messages);
                    _stream.load(request);
                }
                catch (e:Error) {
                	_state = STATE_IDLE;
                    _pending = _sent;
                    _sent = new Array();
                    dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".SendError", ObjectUtil.toString(e), e);
                }
            }
        }

        protected function createURLRequest(messages:Array):URLRequest {
            var request:URLRequest = new URLRequest(_uri + '?m=' + (new Date()).time);

            request.method = URLRequestMethod.POST;
            request.contentType = 'application/x-amf';

            var data:ByteArray = new ByteArray();
            data.writeObject(messages);
            request.data = data;

            return request;
        }

        ///////////////////////////////////////////////////////////////////////
        // Package protected handlers.

        internal function internalResult(request:IMessage, response:IMessage):void {
            // Noop (should be overriden)...
        }

        internal function internalStatus(request:IMessage, response:IMessage):void {
            // Noop (should be overriden)...
        }

        ///////////////////////////////////////////////////////////////////////
        // Listeners.

        protected function streamOpenListener(event:Event):void {
            _channel.dispatchEvent(event);
        }

        protected function streamProgressListener(event:ProgressEvent):void {
            _channel.dispatchEvent(event);
        }

        protected function streamCompleteListener(event:Event):void {
            _channel.dispatchEvent(event);
        }

        protected function streamHttpStatusListener(event:HTTPStatusEvent):void {
            if (event.status != 0 && event.status != 200) {
            	try {
                	dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".HttpStatus", String(event.status), event);
                }
                finally {
	            	_state = STATE_IDLE;
                }
            }
            else
                _channel.dispatchEvent(event);
        }

        protected function streamIoErrorListener(event:IOErrorEvent):void {
            try {
            	dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".IOError", event.text, event);
            }
            finally {
            	_state = STATE_IDLE;
            }
        }

        protected function streamSecurityErrorListener(event:SecurityErrorEvent):void {
            try {
            	dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".SecurityError", event.text, event);
            }
            finally {
            	_state = STATE_IDLE;
            }
        }
        
        protected function dispatchFaultEvent(code:String, description:String, rootCause:Object = null):void {
            var fault:ChannelFaultEvent = ChannelFaultEvent.createEvent(_channel, false, code, null, description);
            if (rootCause != null)
            	fault.rootCause = rootCause;
            _channel.dispatchEvent(fault);
        }
		
		protected function getUnqualifiedClassName(o:Object):String {
			if (o == null)
				return "null";
			
			var name:String = o is String ? String(o) : getQualifiedClassName(o);
			
			var index:int = name.indexOf("::");
			if (index != -1)
				name = name.substr(index + 2);
			
			return name;
		}

    }
}
