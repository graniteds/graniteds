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

    import mx.messaging.Channel;
    import mx.messaging.MessageAgent;
    import mx.messaging.MessageResponder;
    import mx.messaging.messages.IMessage;
    import mx.utils.ObjectUtil;

    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.ErrorMessage;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class GravityMessageResponder extends MessageResponder {

        public function GravityMessageResponder(agent:MessageAgent, message:IMessage, channel:Channel = null):void {
            super(agent, message, channel);
        }

        override protected function resultHandler(ack:IMessage):void {
            agent.acknowledge(ack as AcknowledgeMessage, message);
        }

        override protected function statusHandler(fault:IMessage):void {
            agent.fault(fault as ErrorMessage, message);
        }
    }
}
