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
import org.granite.client.messaging.channel.*;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler;
import org.granite.client.messaging.transport.jetty.JettyWebSocketTransport;
import org.granite.client.test.server.chat.ChatApplication;
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
import java.util.concurrent.*;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestMessagingChat {

    private static final Logger log = Logger.getLogger(TestMessagingChat.class);

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
    private String channelType;
    protected static EmbeddedContainer container;

    private static final ServerApp SERVER_APP_APP = new ServerApp("/chat", false, "localhost", 8787);

    public TestMessagingChat(String containerClassName, ContentType contentType, String channelType) {
        this.contentType = contentType;
        this.channelType = channelType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "chat.war");
        war.addClass(ChatApplication.class);
        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));

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
        if (channelType.equals("websocket"))
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

        channel.stop();
        channelFactory.stop();
    }

    private static final int MSG_COUNT = 500;

    @Test
    public void testChatTextSingleConsumer() throws Exception {
        CyclicBarrier[] barriers = new CyclicBarrier[3];
        barriers[0] = new CyclicBarrier(2);
        barriers[1] = new CyclicBarrier(2);
        barriers[2] = new CyclicBarrier(2);

        String[] messages = new String[MSG_COUNT];
        for (int i = 0; i < MSG_COUNT; i++)
            messages[i] = (i+1) + "::" + UUID.randomUUID().toString();

        ConsumerThread consumer = new ConsumerThread("C", messages, barriers);
        consumer.setTimeout(10);
        consumer.start();

        try {
            barriers[0].await(5, TimeUnit.SECONDS);
            log.info("Consumer subscribed");
        }
        catch (TimeoutException e) {
            log.error(e, "Consumer subscription timeout");
            Assert.fail("Consumer subscription failed");
        }

        ChannelFactory channelFactory = buildChannelFactory();
        MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
        Producer producer = new Producer(channel, "chat", "chat");

        for (int i = 0; i < MSG_COUNT; i++) {
            log.info("Producer sent message " + messages[i]);
            // TODO: Test does not always pass with a too small delay between message ???
            Thread.sleep(5);
            producer.publish(messages[i]);
        }

        try {
            barriers[1].await(10, TimeUnit.SECONDS);
            log.info("Consumer received messaged");
        }
        catch (TimeoutException e) {
            log.error(e, "Consumer reception timeout");
            Assert.fail("Consumer receive messages failed");
        }

        try {
            barriers[2].await(10, TimeUnit.SECONDS);
            log.info("Consumer unsubscribed and stopped");
        }
        catch (TimeoutException e) {
            log.error(e, "Consumer unsubscription timeout");
            Assert.fail("Consumer unsubscription failed");
        }

        channel.stop();
        channelFactory.stop();
    }

    private static final int CONSUMER_COUNT = 5;

    @Test
    public void testChatTextMultiConsumer() throws Exception {
        CyclicBarrier[] barriers = new CyclicBarrier[3];
        barriers[0] = new CyclicBarrier(CONSUMER_COUNT+1);
        barriers[1] = new CyclicBarrier(CONSUMER_COUNT+1);
        barriers[2] = new CyclicBarrier(CONSUMER_COUNT+1);

        String[] messages = new String[MSG_COUNT];
        for (int i = 0; i < MSG_COUNT; i++)
            messages[i] = (i+1) + "::" + UUID.randomUUID().toString();

        for (int i = 0; i < CONSUMER_COUNT; i++) {
            ConsumerThread consumer = new ConsumerThread("C" + (i+1), messages, barriers);
            consumer.setTimeout(10);
            consumer.start();
        }

        try {
            barriers[0].await(5, TimeUnit.SECONDS);
            log.info("All consumers subscribed");
        }
        catch (TimeoutException e) {
            log.error(e, "Consumers subscription timeout");
            Assert.fail("Consumers not subscribed");
        }

        ChannelFactory channelFactory = buildChannelFactory();
        MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
        Producer producer = new Producer(channel, "chat", "chat");

        for (int i = 0; i < MSG_COUNT; i++) {
            log.info("Producer sent message " + messages[i]);
            // TODO: Test does not always pass with a too small delay between message ???
            Thread.sleep(5);
            producer.publish(messages[i]);
        }
        log.info("All messages sent, wait for consumers");

        boolean success = false;
        try {
            barriers[1].await(10, TimeUnit.SECONDS);
            log.info("All messages received");
            success = true;
        }
        catch (TimeoutException e) {
            log.error(e, "Consumers reception timeout, wait for unsubcription/stop");
        }

        try {
            barriers[2].await(10, TimeUnit.SECONDS);
            log.info("All consumers unsubscribed and stopped");
            Assert.assertTrue("All messages received by all consumers", success);
        }
        catch (TimeoutException e) {
            log.error(e, "Consumers unsubscription timeout");
            Assert.fail("Consumers unsubscription/stop failed");
        }
    }


    protected void waitForChannel(Channel channel, CyclicBarrier barrier) {
        try {
            barrier.await();
        }
        catch (Exception e) {
        }
    }

    private class ConsumerThread implements Runnable {

        private String id;
        private List<String> messages;
        private List<String> received = new ArrayList<String>();
        private CyclicBarrier[] barriers;
        private Thread thread = new Thread(this);
        private ChannelFactory channelFactory;
        private Consumer consumer;
        private int timeout = 5;

        public ConsumerThread(String id, String[] messages, CyclicBarrier[] barriers) {
            this.id = id;
            this.messages = Arrays.asList(messages);
            this.barriers = barriers;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public void start() {
            thread.start();
        }

        private CountDownLatch waitToStop = new CountDownLatch(1);

        @Override
        public void run() {
            channelFactory = buildChannelFactory();
            MessagingChannel channel = channelFactory.newMessagingChannel(channelType, "messagingamf", SERVER_APP_APP);
            channel.getTransport().setStatusHandler(new TransportStatusHandler() {
                @Override
                public void handleIO(boolean active) {
                }

                @Override
                public void handleException(TransportException e) {
                    log.error(e, "Consumer " + id + " transport exception");
                }
            });

            consumer = new Consumer(channel, "chat", "chat");
            consumer.addMessageListener(new ConsumerMessageListener());
            consumer.subscribe(new ResultIssuesResponseListener() {
                @Override
                public void onResult(ResultEvent event) {
                    log.info("Consumer %s: subscribed %s", id, event.getResult());
                    waitForChannel(consumer.getChannel(), barriers[0]);
                }

                @Override
                public void onIssue(IssueEvent event) {
                    log.error("Consumer %s: subscription failed %s", id, event.toString());
                    try {
                        barriers[0].await();
                    }
                    catch (Exception e) {
                    }
                }
            });

            try {
                waitToStop.await(timeout, TimeUnit.SECONDS);
            }
            catch (Exception e) {
                log.error(e, "Consumer %s time out", id);
            }
            try {
                channel.stop();
                channelFactory.stop();
                barriers[2].await();
            }
            catch (Exception e) {
                log.error("Consumer did not terminate correctly", e);
            }
        }

        private class ConsumerMessageListener implements TopicMessageListener {
            @Override
            public void onMessage(TopicMessageEvent event) {
                if (messages.contains(event.getData())) {
                    received.add((String)event.getData());
                    log.info("Consumer %s: received message %s (%d)", id, event.getData(), received.size());

                    if (received.size() == messages.size() && received.containsAll(messages)) {
                        log.info("Consumer %s received all messages, wait for others", id);
                        try {
                            barriers[1].await();
                        }
                        catch (InterruptedException e) {
                            log.info(e, "Consumer %s interrupted during message reception", id);
                        }
                        catch (BrokenBarrierException e) {
                            log.info(e, "Consumer %s broken barrier during message reception", id);
                        }

                        consumer.unsubscribe(new ResultIssuesResponseListener() {
                            @Override
                            public void onResult(ResultEvent event) {
                                log.info("Consumer %s: unsubscribed %s", id, event.getResult());
                                waitToStop.countDown();
                            }

                            @Override
                            public void onIssue(IssueEvent event) {
                                log.error("Consumer %s: unsubscription failed %s", id, event.toString());
                            }
                        });
                    }
                }
                else
                    log.warn("Consumer %s: unexpected received message %s", id, event.getData());
            }
        }
    }
}
