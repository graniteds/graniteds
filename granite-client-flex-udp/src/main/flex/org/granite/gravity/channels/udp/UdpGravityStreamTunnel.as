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
package org.granite.gravity.channels.udp {

	import flash.events.DatagramSocketDataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.TimerEvent;
	import flash.net.DatagramSocket;
	import flash.utils.ByteArray;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.messaging.MessageResponder;
	import mx.messaging.events.ChannelEvent;
	import mx.messaging.messages.CommandMessage;
	import mx.messaging.messages.ErrorMessage;
	import mx.messaging.messages.IMessage;
	import mx.utils.ObjectUtil;
	
	import org.granite.gravity.channels.GravityStreamTunnel;
	import org.granite.gravity.channels.StreamMessageResponder;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class UdpGravityStreamTunnel extends GravityStreamTunnel {
        
        ///////////////////////////////////////////////////////////////////////
        // Fields.

		private static const log:ILogger = Log.getLogger("org.granite.enterprise.gravity.channels.UdpGravityStreamTunnel");

		private static const GDS_SERVER_UDP_PORT:String = "GDS_SERVER_UDP_PORT";
		private static const GDS_CLIENT_UDP_PORT:String = "GDS_CLIENT_UDP_PORT";

		private static const DEFAULT_HEART_BEAT_INTERVAL:Number = 60 * 1000; // 1 mn.
		
		private var _datagramSocket:DatagramSocket = null;
		private var _messageResponder:MessageResponder = null;
		
		private var _heartBeatClientInterval:Number = DEFAULT_HEART_BEAT_INTERVAL;
		private var _heartBeatClientTimer:Timer = null;

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function UdpGravityStreamTunnel(channel:UdpGravityChannel) {
            super(channel);
        }
		
		public function get heartBeatInterval():Number {
			return _heartBeatClientInterval;
		}
		
		public function set heartBeatInterval(value:Number):void {
			if (value < 1000)
				throw new Error("heart beat interval value must be >= 1000: " + value);
			_heartBeatClientInterval = value;
		}
		
		override public function get uri():String {
			if (_datagramSocket && _datagramSocket.connected)
				return "udp://" + _datagramSocket.remoteAddress + ":" + _datagramSocket.remotePort + "|" + super.uri;
			return super.uri;
		}
		
        ///////////////////////////////////////////////////////////////////////
        // Public operations.

        override public function connect(uri:String):void {
			if (!_datagramSocket) {
				_datagramSocket = new DatagramSocket();
				_datagramSocket.addEventListener(Event.CLOSE, datagramCloseEventHandler, false, 0, true);
				_datagramSocket.addEventListener(IOErrorEvent.IO_ERROR, datagramIOErrorEventHandler, false, 0, true);
				_datagramSocket.bind(
					(channel as UdpGravityChannel).defaultLocalPort,
					(channel as UdpGravityChannel).defaultLocalAddress
				);
			}
			
			super.connect(uri);
        }
		
		override protected function createConnectMessageResponder(connectMessage:CommandMessage):StreamMessageResponder {
			if (!_datagramSocket) {
				log.error("Trying to connect while datagram socket is null");
				return null;
			}

			connectMessage.headers[GDS_CLIENT_UDP_PORT] = _datagramSocket.localPort;
			return new UdpConnectMessageResponder(connectMessage, this);
		}
		
		override protected function reconnectOnStreamResult():Boolean {
			return false;
		}
		
		override public function disconnect():void {
			cancelHeartBeatTimer();
			closeDatagramSocket();
			
			super.disconnect();
				
			channel.dispatchEvent(UdpChannelEvent.createEvent(UdpChannelEvent.DISCONNECT, (channel as UdpGravityChannel)));
		}
		
		protected function cancelHeartBeatTimer():void {
			if (_heartBeatClientTimer) {
				try {
					_heartBeatClientTimer.removeEventListener(TimerEvent.TIMER_COMPLETE, sendHeartBeat);
					_heartBeatClientTimer.stop();
				}
				catch (e:Error) {
					log.error("Could not stop heart beat timer: " + e.message);
				}
				finally {
					_heartBeatClientTimer = null;
				}
			}
		}
		
		protected function sendHeartBeat(event:TimerEvent):void {
			connect(channel.uri);
			
			_heartBeatClientTimer.reset();
			_heartBeatClientTimer.start();
		}
		
		protected function closeDatagramSocket():void {
			if (_datagramSocket) {
				try {
					_datagramSocket.removeEventListener(Event.CLOSE, datagramCloseEventHandler);
					_datagramSocket.removeEventListener(IOErrorEvent.IO_ERROR, datagramIOErrorEventHandler);
					_datagramSocket.removeEventListener(DatagramSocketDataEvent.DATA, datagramDataEventHandler);
					_datagramSocket.close();
				}
				finally {
					_datagramSocket = null;
					_messageResponder = null;
				}
			}
		}
		
		protected function datagramCloseEventHandler(event:Event):void {
			dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".Closed", "Datagram socket closed", event);
		}
		
		protected function datagramIOErrorEventHandler(event:Event):void {
			dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".IOError", "Datagram socket IO error", event);
		}

		protected function datagramDataEventHandler(event:DatagramSocketDataEvent):void {
			var udpChannel:UdpGravityChannel = (channel as UdpGravityChannel),
				error:ErrorMessage = null;
			
			try {
                var responses:Array = (event.data.readObject() as Array);
				for each(var response:IMessage in responses) {
					if (response is ErrorMessage) {
						error = (response as ErrorMessage);
						break;
					}
					if (response.headers[BYTEARRAY_BODY_HEADER])
						response.body = ByteArray(response.body).readObject();
					udpChannel.callResponder(_messageResponder, response);
				}
            }
            catch (e:Error) {
				dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".Read", ObjectUtil.toString(e), event);
				log.debug("datagramEventHandler: {0}", ObjectUtil.toString(e));
            }
			
			if (error) {
				udpChannel.callResponder(_messageResponder, error);
				dispatchFaultEvent(error.faultCode, error.faultDetail, event);
			}
			else
				udpChannel.dispatchEvent(event);
		}

        override protected function streamIoErrorListener(event:IOErrorEvent):void {
			cancelHeartBeatTimer();
			
            super.streamIoErrorListener(event);
        }
		
        override protected function dispatchFaultEvent(code:String, description:String, rootCause:Object = null):void {
            var fault:UdpChannelFaultEvent = UdpChannelFaultEvent.createEvent((channel as UdpGravityChannel), code, null, description);
            if (rootCause != null)
            	fault.rootCause = rootCause;
            channel.dispatchEvent(fault);
        }

		
		///////////////////////////////////////////////////////////////////////
		// Package protected handlers.
		
		internal function get datagramSocket():DatagramSocket {
			return _datagramSocket;
		}
		
		internal function internalConnectResult(request:IMessage, response:IMessage):void {
			if (!_datagramSocket.connected) {
				var serverUdpPort:Number = response.headers[GDS_SERVER_UDP_PORT];
				
				if (isNaN(serverUdpPort) || serverUdpPort <= 0) {
					dispatchFaultEvent(
						"Client." + getUnqualifiedClassName(this) + ".Connect",
						"Invalid UDP server port: " + serverUdpPort,
						response
					);
					return;
				}
				
				_datagramSocket.connect((channel as UdpGravityChannel).serverIp, serverUdpPort);
				_datagramSocket.addEventListener(DatagramSocketDataEvent.DATA, datagramDataEventHandler, false, 0, true);
				_datagramSocket.receive();
	
				_messageResponder = new StreamMessageResponder(request, this);
			}
			
			if (!_heartBeatClientTimer) {
				_heartBeatClientTimer = new Timer(_heartBeatClientInterval, 1);
				_heartBeatClientTimer.addEventListener(TimerEvent.TIMER_COMPLETE, sendHeartBeat, false, 0, true);
				_heartBeatClientTimer.start();
			}

			channel.dispatchEvent(UdpChannelEvent.createEvent(UdpChannelEvent.CONNECT, (channel as UdpGravityChannel)));
		}
		
		internal function internalConnectStatus(request:IMessage, response:IMessage):void {
			try {
				dispatchFaultEvent("Client." + getUnqualifiedClassName(this) + ".Connect", "Cound not connect UDP channel", response);
			}
			finally {
				disconnect();
			}
		}
    }
}
