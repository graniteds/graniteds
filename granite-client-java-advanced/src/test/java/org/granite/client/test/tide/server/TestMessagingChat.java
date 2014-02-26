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
package org.granite.client.test.tide.server;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.ResultIssuesResponseListener;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.test.tide.server.chat.ChatApplication;
import org.granite.client.tide.BaseIdentity;
import org.granite.client.tide.Context;
import org.granite.client.tide.Identity;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResponders;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.granite.util.ContentType;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestMessagingChat {

    private static final Logger log = Logger.getLogger(TestMessagingChat.class);

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}, channel: {2}")
    public static Iterable<Object[]> data() {
        return ContainerTestUtil.data(ContainerTestUtil.CHANNEL_TYPES_ALL);
    }

    private ContentType contentType;
    private String channelType;
    protected static EmbeddedContainer container;

    private Context context = new SimpleContextManager().getContext();

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
        File gfweb = new File("granite-server-glassfishv3/src/test/resources/glassfish-web.xml");
        war.addAsWebInfResource(gfweb, "glassfish-web.xml");

        container = ContainerTestUtil.newContainer(war, false);
        container.start();
        log.info("Container started");
    }

    @AfterClass
    public static void stopContainer() throws Exception {
        container.stop();
        container.destroy();
        log.info("Container stopped");
    }

    @Test
    public void testTextProducer() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);
        Producer producer = serverSession.getProducer("chat", "chat", channelType);
        ResponseMessage message = producer.publish("test").get();

        Assert.assertNotNull("Message has clientId", message.getClientId());

        serverSession.stop();
    }

    @Test
    public void testTextSecureProducer() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);
        Identity identity = context.set("identity", new BaseIdentity(serverSession));

        String user = identity.login("user", "user00", TideResponders.<String>noop()).get();
        Assert.assertEquals("Logged in", "user", user);

        Producer producer = serverSession.getProducer("secureChat", "chat", channelType);
        ResponseMessage message = producer.publish("test").get();

        Assert.assertNotNull("Message has clientId", message.getClientId());

        final CountDownLatch waitForLogout = new CountDownLatch(1);

        identity.logout(new TideResponder<Void>() {
            @Override
            public void result(TideResultEvent<Void> event) {
                waitForLogout.countDown();
            }

            @Override
            public void fault(TideFaultEvent event) {

            }
        });
        waitForLogout.await(10000, TimeUnit.MILLISECONDS);

        serverSession.stop();
    }

    @Test
    public void testTextSecureConsumerNotLoggedIn() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);
        Consumer consumer = serverSession.getConsumer("secureChat", "chat", channelType);

        final CountDownLatch waitForSubscribe = new CountDownLatch(1);
        final boolean[] notLoggedIn = new boolean[1];
        consumer.subscribe(new ResultIssuesResponseListener() {
            @Override
            public void onIssue(IssueEvent event) {
                if (event instanceof FaultEvent && ((FaultEvent)event).getCode() == FaultMessage.Code.NOT_LOGGED_IN)
                    notLoggedIn[0] = true;
                waitForSubscribe.countDown();
            }

            @Override
            public void onResult(ResultEvent event) {
                waitForSubscribe.countDown();
            }
        });
        waitForSubscribe.await(15000, TimeUnit.MILLISECONDS);

        Assert.assertTrue("Consumer not logged in", notLoggedIn[0]);
        Assert.assertFalse("Consumer not subscribed", consumer.isSubscribed());

        serverSession.stop();
    }

    @Test
    public void testTextSecureConsumer() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);
        Consumer consumer = serverSession.getConsumer("secureChat", "chat", channelType);
        Identity identity = context.set("identity", new BaseIdentity(serverSession));

        String user = identity.login("user", "user00", TideResponders.<String>noop()).get();
        Assert.assertEquals("Logged in", "user", user);

        final CountDownLatch waitForSubscribe2 = new CountDownLatch(1);
        final boolean[] subscribed = new boolean[1];
        final boolean[] notLoggedIn = new boolean[1];
        consumer.subscribe(new ResultIssuesResponseListener() {
            @Override
            public void onIssue(IssueEvent event) {
                if (event instanceof FaultEvent && ((FaultEvent)event).getCode() == FaultMessage.Code.NOT_LOGGED_IN)
                    notLoggedIn[0] = true;
                waitForSubscribe2.countDown();
            }

            @Override
            public void onResult(ResultEvent event) {
                subscribed[0] = true;
                waitForSubscribe2.countDown();
            }
        });
        waitForSubscribe2.await(15000, TimeUnit.MILLISECONDS);

        Assert.assertFalse("Consumer not logged in", notLoggedIn[0]);
        Assert.assertTrue("Consumer subscribed", consumer.isSubscribed());

        consumer.unsubscribe().get();

        identity.logout(TideResponders.<Void>noop());

        serverSession.stop();
    }
}
