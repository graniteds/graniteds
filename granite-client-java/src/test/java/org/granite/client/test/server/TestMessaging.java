/**
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
package org.granite.client.test.server;

import org.granite.client.messaging.*;
import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.transport.jetty.JettyWebSocketTransport;
import org.granite.test.container.Utils;
import org.granite.test.container.EmbeddedContainer;
import org.granite.logging.Logger;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestMessaging {

    private static final Logger log = Logger.getLogger(TestMessaging.class);

    private static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    private static String[] CHANNEL_TYPES = new String[] {
        "long-polling", "websocket"
    };

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}, channel: {2}")
    public static Iterable<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            for (String channelType : CHANNEL_TYPES)
                params.add(new Object[] { CONTAINER_CLASS_NAME, contentType, channelType });
        }
        return params;
    }

    private ContentType contentType;
    private String containerClassName;
    private String channelType;
    private static EmbeddedContainer container;

    private static final ServerApp SERVER_APP_APP = new ServerApp("/chat", false, "localhost", 8787);

    public TestMessaging(String containerClassName, ContentType contentType, String channelType) {
        this.containerClassName = containerClassName;
        this.contentType = contentType;
        this.channelType = channelType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Show classpath
//        URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
//        for (URL url : urls)
//            System.out.println("cp: " + url.getFile());

        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "chat.war");
        war.addClass(ChatApplication.class);
        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFileFilter()));

        container = (EmbeddedContainer)TypeUtil.newInstance(CONTAINER_CLASS_NAME, new Class<?>[] { WebArchive.class }, new Object[] { war });
        container.start();
        log.info("Container started");
    }

    @AfterClass
    public static void stopContainer() throws Exception {
        container.stop();
        log.info("Container stopped");
    }

    private ChannelFactory buildChannelFactory() {
        ChannelFactory channelFactory = contentType.equals(ContentType.JMF_AMF) ? new JMFChannelFactory() : new AMFChannelFactory();
        channelFactory.setMessagingTransport("websocket", new JettyWebSocketTransport());
        channelFactory.start();
        return channelFactory;
    }

    @Test
    public void testTextProducer() throws Exception {
        ChannelFactory channelFactory = buildChannelFactory();
        MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
        Producer producer = new Producer(channel, "chat", "chat");
        ResponseMessage message = producer.publish("test").get();

        Assert.assertNotNull("Message has clientId", message.getClientId());
    }

    private static final int MSG_COUNT = 10;

    @Test
    public void testChatTextSingleConsumer() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);

        String[] messages = new String[MSG_COUNT];
        for (int i = 0; i < MSG_COUNT; i++)
            messages[i] = UUID.randomUUID().toString();

        ConsumerThread consumer = new ConsumerThread("C", messages, barrier);
        consumer.start();

        ChannelFactory channelFactory = buildChannelFactory();
        MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
        Producer producer = new Producer(channel, "chat", "chat");

        try {
            barrier.await(5, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            Assert.fail("Consumer subscription failed");
        }
        barrier.reset();

        for (int i = 0; i < MSG_COUNT; i++) {
            messages[i] = UUID.randomUUID().toString();
            producer.publish(messages[i]);
        }

        try {
            barrier.await(5, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            Assert.fail("Consumer receive messages failed");
        }
        barrier.reset();

        try {
            barrier.await(5, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            Assert.fail("Consumer unsubscription failed");
        }
    }

    private static final int CONSUMER_COUNT = 5;

    @Test
    public void testChatTextMultiConsumer() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(CONSUMER_COUNT+1);

        String[] messages = new String[MSG_COUNT];
        for (int i = 0; i < MSG_COUNT; i++)
            messages[i] = UUID.randomUUID().toString();

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            ConsumerThread consumer = new ConsumerThread("C" + (i+1), messages, barrier);
            consumer.start();
        }

        ChannelFactory channelFactory = buildChannelFactory();
        MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
        Producer producer = new Producer(channel, "chat", "chat");

        try {
            barrier.await(5, TimeUnit.SECONDS);
            log.info("All consumers subscribed");
        }
        catch (Exception e) {
            Assert.fail("Consumers not subscribed");
        }

        for (int i = 0; i < MSG_COUNT; i++) {
            messages[i] = UUID.randomUUID().toString();
            log.info("Producer sent message " + messages[i]);
            producer.publish(messages[i]);
        }

        try {
            barrier.await(10, TimeUnit.SECONDS);
            log.info("All messages received");
        }
        catch (Exception e) {
            Assert.fail("Consumers receive messages failed");
        }
        barrier.reset();

        try {
            barrier.await(5, TimeUnit.SECONDS);
            log.info("All consumers unsubscribed");
        }
        catch (Exception e) {
            Assert.fail("Consumers unsubscription failed");
        }
    }


    private class ConsumerThread implements Runnable {

        private String id;
        private List<String> messages;
        private List<String> received = new ArrayList<String>();
        private CyclicBarrier barrier;
        private Thread thread = new Thread(this);
        private ChannelFactory channelFactory;
        private Consumer consumer;

        public ConsumerThread(String id, String[] messages, CyclicBarrier barrier) {
            this.id = id;
            this.messages = Arrays.asList(messages);
            this.barrier = barrier;
        }

        public void start() {
            thread.start();
        }

        @Override
        public void run() {
            channelFactory = buildChannelFactory();
            MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);

            consumer = new Consumer(channel, "chat", "chat");
            consumer.addMessageListener(new ConsumerMessageListener());
            consumer.subscribe(new ResultIssuesResponseListener() {
                @Override
                public void onResult(ResultEvent event) {
                    log.info("Consumer " + id + ": subscribed " + event.getResult());
                    try {
                        barrier.await();
                    }
                    catch (Exception e) {
                        Thread.currentThread().interrupt();
                    }
                }

                @Override
                public void onIssue(IssueEvent event) {
                    log.error("Consumer " + id + ": subscription failed " + event.toString());
                    Thread.currentThread().interrupt();
                }
            });
        }

        private class ConsumerMessageListener implements TopicMessageListener {
            @Override
            public void onMessage(TopicMessageEvent event) {
                if (messages.contains(event.getData())) {
                    log.info("Consumer " + id + ": received message " + event.getData());
                    received.add((String)event.getData());

                    if (received.size() == messages.size() && received.containsAll(messages)) {
                        try {
                            barrier.await();
                        }
                        catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }

                        consumer.unsubscribe(new ResultIssuesResponseListener() {
                            @Override
                            public void onResult(ResultEvent event) {
                                log.info("Consumer " + id + ": unsubscribed " + event.getResult());

                                channelFactory.stop();
                                try {
                                    barrier.await();
                                }
                                catch (Exception e) {
                                }
                                Thread.currentThread().interrupt();
                            }

                            @Override
                            public void onIssue(IssueEvent event) {
                                log.error("Consumer " + id + ": unsubscription failed " + event.toString());
                                Thread.currentThread().interrupt();
                            }
                        });
                    }
                }
                else
                    log.warn("Consumer " + id + ": unexpected received message " + event.getData());
            }
        }
    }
}
