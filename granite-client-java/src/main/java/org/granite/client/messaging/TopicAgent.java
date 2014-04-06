/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging;

import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;

/**
 * Interface for consumer/producer style message agents
 *
 * @author Franck WOLFF
 */
public interface TopicAgent {

    /**
     * Messaging channel to which the agent is attached
     * @return message channel
     */
	MessagingChannel getChannel();

    /**
     * Name of the remote destination for the topic
     * @return destination
     */
	String getDestination();

    /**
     * Subtopic for the remote topic
     * @return topic
     */
	String getTopic();

    /**
     * Default headers added to all messages sent by this agent
     * @return headers map
     */
	Map<String, Object> getDefaultHeaders();
}
