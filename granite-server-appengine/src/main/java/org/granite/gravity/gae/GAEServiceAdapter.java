/**
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
package org.granite.gravity.gae;

import java.util.concurrent.ConcurrentHashMap;

import org.granite.gravity.Channel;
import org.granite.gravity.adapters.ServiceAdapter;
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
public class GAEServiceAdapter extends ServiceAdapter {

    private static final Logger log = Logger.getLogger(GAEServiceAdapter.class);

    private final GAETopic rootTopic = new GAETopic("/", this);
    private transient ConcurrentHashMap<String, GAETopicId> _topicIdCache;
    
    private boolean noLocal = false;


    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
        _topicIdCache = new ConcurrentHashMap<String, GAETopicId>();
        
        if (Boolean.TRUE.toString().equals(destinationProperties.get("no-local")))
        	noLocal = true;
    }


    public GAETopic getTopic(GAETopicId id) {
        return rootTopic.getChild(id);
    }

    public GAETopic getTopic(String id) {
        GAETopicId cid = getTopicId(id);
        if (cid.depth() == 0)
            return null;
        return rootTopic.getChild(cid);
    }

    public GAETopic getTopic(String id, boolean create)  {
        synchronized (this) {
            GAETopic topic = getTopic(id);

            if (topic == null && create) {
                topic = new GAETopic(id, this);
                rootTopic.addChild(topic);
                log.debug("New Topic: %s", topic);
            }
            return topic;
        }
    }

    public GAETopicId getTopicId(String id) {
        GAETopicId tid = _topicIdCache.get(id);
        if (tid == null) {
            tid = new GAETopicId(id);
            GAETopicId tmpTid = _topicIdCache.putIfAbsent(id, tid); 
            if(tmpTid != null) 
            	tid = tmpTid;
        }
        return tid;
    }

    public boolean hasTopic(String id) {
        GAETopicId cid = getTopicId(id);
        return rootTopic.getChild(cid) != null;
    }

    @Override
    public Object invoke(Channel fromChannel, AsyncMessage message) {
    	String topicId = GAETopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

        AsyncMessage reply = null;

        if (message.getBody() != null && getSecurityPolicy().canPublish(fromChannel, topicId, message)) {
            GAETopicId tid = getTopicId(topicId);

            rootTopic.publish(tid, fromChannel, message);

            reply = new AcknowledgeMessage(message);
            reply.setMessageId(message.getMessageId());
        }
        else {
            reply = new ErrorMessage(message, null);
            ((ErrorMessage)reply).setFaultString("unknown channel");
        }

        return reply;
    }

    @Override
    public Object manage(Channel fromChannel, CommandMessage message) {
    	AsyncMessage reply = null;

    	if (message.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
            String subscribeTopicId = GAETopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

            if (getSecurityPolicy().canSubscribe(fromChannel, subscribeTopicId, message)) {
                GAETopic topic = getTopic(subscribeTopicId);
                if (topic == null && getSecurityPolicy().canCreate(fromChannel, subscribeTopicId, message))
                    topic = getTopic(subscribeTopicId, true);

                if (topic != null) {
                    String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
                    String selector = (String)message.getHeader(CommandMessage.SELECTOR_HEADER);
                    topic.subscribe(fromChannel, message.getDestination(), subscriptionId, selector, noLocal);

                    reply = new AcknowledgeMessage(message);
                }
                else {
                    reply = new ErrorMessage(message, null);
                    ((ErrorMessage)reply).setFaultString("cannot create");
                }
            }
            else {
                reply = new ErrorMessage(message, null);
                ((ErrorMessage)reply).setFaultString("cannot subscribe");
            }
        }
        else if (message.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            String unsubscribeTopicId = GAETopicId.normalize(((String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER)));

            GAETopic topic = getTopic(unsubscribeTopicId);
            String subscriptionId = null;
            if (topic != null) {
                subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
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
