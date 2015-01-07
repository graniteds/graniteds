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
package org.granite.gravity.adapters;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.granite.gravity.Channel;
import org.granite.gravity.Subscription;

import flex.messaging.messages.AsyncMessage;

/**
 * Adapted from Greg Wilkins code (Jetty).
 * 
 * @author William DRAI
 */
public class Topic {

    private final TopicId id;
    private final SimpleServiceAdapter serviceAdapter;

    private ConcurrentMap<String, Subscription> subscriptions = new ConcurrentHashMap<String, Subscription>();
    private ConcurrentMap<String, Topic> children = new ConcurrentHashMap<String, Topic>();
    private Topic wild;
    private Topic wildWild;


    public Topic(String topicId, SimpleServiceAdapter serviceAdapter) {
        this.id = new TopicId(topicId);
        this.serviceAdapter = serviceAdapter;
    }

    public String getId() {
        return id.toString();
    }

    public TopicId getTopicId() {
        return id;
    }

    public Topic getChild(TopicId topicId) {
        String next = topicId.getSegment(id.depth());
        if (next == null)
            return null;

        Topic topic = children.get(next);

        if (topic == null || topic.getTopicId().depth() == topicId.depth()) {
            return topic;
        }
        return topic.getChild(topicId);
    }

    public void addChild(Topic topic) {
        TopicId child = topic.getTopicId();
        if (!id.isParentOf(child))
            throw new IllegalArgumentException(id + " not parent of " + child);

        String next = child.getSegment(id.depth());

        if ((child.depth() - id.depth()) == 1) {
            // add the topic to this topics
            Topic old = children.putIfAbsent(next, topic);

            if (old != null)
                throw new IllegalArgumentException("Already Exists");

            if (TopicId.WILD.equals(next))
                wild = topic;
            else if (TopicId.WILDWILD.equals(next))
                wildWild = topic;
        }
        else {
            Topic branch = serviceAdapter.getTopic((id.depth() == 0 ? "/" : (id.toString() + "/")) + next, true);
            branch.addChild(topic);
        }
    }

    public void subscribe(Channel channel, String destination, String subscriptionId, String selector, boolean noLocal) {
        synchronized (this) {
            Subscription subscription = channel.addSubscription(destination, getId(), subscriptionId, noLocal);
            subscription.setSelector(selector);
            subscriptions.putIfAbsent(subscriptionId, subscription);
        }
    }

    public void unsubscribe(Channel channel, String subscriptionId) {
        synchronized(this) {
        	subscriptions.remove(subscriptionId);
            channel.removeSubscription(subscriptionId);
        }
    }


    public void publish(TopicId to, Channel fromChannel, AsyncMessage msg) {
        int tail = to.depth()-id.depth();

        switch(tail) {
            case 0:
                for (Subscription subscription : subscriptions.values()) {
                    AsyncMessage m = msg.clone();
                    subscription.deliver(fromChannel, m);
                }

                break;

            case 1:
                if (wild != null) {
                    for (Subscription subscription : wild.subscriptions.values()) {
                        AsyncMessage m = msg.clone();
                        subscription.deliver(fromChannel, m);
                    }
                }

            default: {
                if (wildWild != null) {
                    for (Subscription subscription : wildWild.subscriptions.values()) {
                        AsyncMessage m = msg.clone();
                        subscription.deliver(fromChannel, m);
                    }
                }
                String next = to.getSegment(id.depth());
                Topic topic = children.get(next);
                if (topic != null)
                    topic.publish(to, fromChannel, msg);
            }
        }
    }

    @Override
    public String toString() {
        return id.toString() + " {" + children.values() + "}";
    }
}
