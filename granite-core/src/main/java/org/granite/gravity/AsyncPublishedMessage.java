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

package org.granite.gravity;

import org.granite.gravity.adapters.Topic;
import org.granite.gravity.adapters.TopicId;

import flex.messaging.messages.AsyncMessage;

/**
 * @author Franck WOLFF
 */
public class AsyncPublishedMessage {

	private final Topic rootTopic;
	private final TopicId topicId;
	private final AsyncMessage message;

	public AsyncPublishedMessage(Topic rootTopic, TopicId topicId, AsyncMessage message) {
		if (rootTopic == null || topicId == null || message == null)
			throw new NullPointerException("rootTopic, topicId and meessage canoot be null");
		
		this.rootTopic = rootTopic;
		this.topicId = topicId;
		this.message = message;
	}
	
	public void publish(Channel fromChannel) {
		rootTopic.publish(topicId, fromChannel, message);
	}

	@Override
	public String toString() {
		return getClass().getName() + " {topicId=" + topicId + ", message=" + message + "}";
	}
}
