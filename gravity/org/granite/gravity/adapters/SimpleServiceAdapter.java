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

package org.granite.gravity.adapters;

import java.util.concurrent.ConcurrentHashMap;

import org.granite.gravity.AsyncPublishedMessage;
import org.granite.gravity.Channel;
import org.granite.gravity.MessagePublishingException;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.util.XMap;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;

/**
 * @author William DRAI
 */
public class SimpleServiceAdapter extends ServiceAdapter {

    private static final Logger log = Logger.getLogger(SimpleServiceAdapter.class);

    private final Topic rootTopic = new Topic("/", this);
    private transient ConcurrentHashMap<String, TopicId> _topicIdCache;
    
    private boolean noLocal = false;

    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
    	super.configure(adapterProperties, destinationProperties);
    	
        _topicIdCache = new ConcurrentHashMap<String, TopicId>();
        
        if (Boolean.TRUE.toString().equals(destinationProperties.get("no-local")))
        	noLocal = true;
    }


    public Topic getTopic(TopicId id) {
        return rootTopic.getChild(id);
    }

    public Topic getTopic(String id) {
        TopicId cid = getTopicId(id);
        if (cid.depth() == 0)
            return null;
        return rootTopic.getChild(cid);
    }

    public Topic getTopic(String id, boolean create)  {
        synchronized (this) {
            Topic topic = getTopic(id);

            if (topic == null && create) {
                topic = new Topic(id, this);
                rootTopic.addChild(topic);
                log.debug("New Topic: %s", topic);
            }
            return topic;
        }
    }

    public TopicId getTopicId(String id) {
        TopicId tid = _topicIdCache.get(id);
        if (tid == null) {
            tid = new TopicId(id);
            _topicIdCache.put(id, tid);
        }
        return tid;
    }

    public boolean hasTopic(String id) {
        TopicId cid = getTopicId(id);
        return rootTopic.getChild(cid) != null;
    }

    @Override
    public Object invoke(Channel fromChannel, AsyncMessage message) {
    	String topicId = TopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

        AsyncMessage reply = null;

        if (getSecurityPolicy().canPublish(fromChannel, topicId, message)) {
            TopicId tid = getTopicId(topicId);

        	try {
				fromChannel.publish(new AsyncPublishedMessage(rootTopic, tid, message));
	            reply = new AcknowledgeMessage(message);
	            reply.setMessageId(message.getMessageId());
			}
        	catch (MessagePublishingException e) {
				log.error(e, "Error while publishing message: %s from channel %s to topic: %s", message, fromChannel, tid);
	            reply = new ErrorMessage(message, null);
	            ((ErrorMessage)reply).setFaultString("Server.Publish.Error");
			}
        }
        else {
        	log.warn("Channel %s tried to publish a message to topic %s", fromChannel, topicId);
            reply = new ErrorMessage(message, null);
            ((ErrorMessage)reply).setFaultString("Server.Publish.Denied");
        }

        return reply;
    }

    @Override
    public Object manage(Channel fromChannel, CommandMessage message) {
    	AsyncMessage reply = null;

    	if (message.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
            String subscribeTopicId = TopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

            if (getSecurityPolicy().canSubscribe(fromChannel, subscribeTopicId, message)) {
                Topic topic = getTopic(subscribeTopicId);
                if (topic == null && getSecurityPolicy().canCreate(fromChannel, subscribeTopicId, message))
                    topic = getTopic(subscribeTopicId, true);

                if (topic != null) {
                    String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
                    String selector = (String)message.getHeader(CommandMessage.SELECTOR_HEADER);
                    if (subscriptionId == null)
                    	log.warn("No subscriptionId for subscription message");
                    else
                    	topic.subscribe(fromChannel, message.getDestination(), subscriptionId, selector, noLocal);

                    reply = new AcknowledgeMessage(message);
                }
                else {
                    reply = new ErrorMessage(message, null);
                    ((ErrorMessage)reply).setFaultString("Server.CreateTopic.Denied");
                }
            }
            else {
                reply = new ErrorMessage(message, null);
                ((ErrorMessage)reply).setFaultString("Server.Subscribe.Denied");
            }
        }
        else if (message.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            String unsubscribeTopicId = TopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

            Topic topic = getTopic(unsubscribeTopicId);
            String subscriptionId = null;
            if (topic != null) {
                subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
                if (subscriptionId == null)
                	log.warn("No subscriptionId for unsubscription message");
                else
                	topic.unsubscribe(fromChannel, subscriptionId);
            }

            reply = new AcknowledgeMessage(message);
            reply.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
        }
        else {
            reply = new ErrorMessage(message, null);
            ((ErrorMessage)reply).setFaultString("unknown operation");

        }

        return reply;
    }
}
