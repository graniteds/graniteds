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

    import flash.utils.getQualifiedClassName;

    import mx.logging.Log;
    import mx.messaging.events.ChannelEvent;
    import mx.messaging.messages.IMessage;
    import mx.messaging.messages.AsyncMessage;
    import mx.utils.ObjectUtil;

    /**
     *	Gravity-specific implementation of the Producer message agent
     *  
     * 	@author Franck WOLFF
     */
    public class Producer extends TopicMessageAgent {

        private static const MESSAGE_TYPE:String = getQualifiedClassName(new AsyncMessage());

        private var _defaultHeaders:Object = new Object();

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function Producer() {
            super();
            _agentType = "org.granite.gravity.Producer";
            _log = Log.getLogger("org.granite.gravity.Producer");
        }

        ///////////////////////////////////////////////////////////////////////
        // Properties.

        public function get defaultHeaders():Object {
            return _defaultHeaders;
        }
        public function set defaultHeaders(value:Object):void {
            _defaultHeaders = value;
        }

        ///////////////////////////////////////////////////////////////////////
        // Other Public Methods.

        public function send(message:IMessage):void {

            if (message == null || getQualifiedClassName(message) != MESSAGE_TYPE)
                throw new Error("Invalid message type: " + ObjectUtil.toString(message));

            message.destination = destination;
            message.timestamp = (new Date()).time;
            if (_defaultHeaders != null) {
                for each (var key:Object in _defaultHeaders) {
                    if (!message.headers[key])
                        message.headers[key] = _defaultHeaders[key];
                }
            }
            if (topic)
                message.headers[AsyncMessage.SUBTOPIC_HEADER] = topic;

            internalSend(message);
        }

        public override function channelConnectHandler(event:ChannelEvent):void {
            super.channelConnectHandler(event);
            doAuthenticate();
        }

        public override function channelDisconnectHandler(event:ChannelEvent):void {
            super.channelDisconnectHandler(event);
        }
    }
}
