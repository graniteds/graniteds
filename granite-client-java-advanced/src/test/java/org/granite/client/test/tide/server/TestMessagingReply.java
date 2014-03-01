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
import org.granite.client.messaging.TopicMessageListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.test.tide.server.chat.ChatApplication;
import org.granite.client.test.tide.server.chat.ChatService;
import org.granite.client.test.tide.server.chat.ReplyService;
import org.granite.client.tide.BaseIdentity;
import org.granite.client.tide.Context;
import org.granite.client.tide.Identity;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResponders;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.util.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestMessagingReply {

    private static final Logger log = Logger.getLogger(TestMessagingReply.class);

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}, channel: {2}")
    public static Iterable<Object[]> data() {
        return ContainerTestUtil.data(ContainerTestUtil.CHANNEL_TYPES_ALL);
    }

    private ContentType contentType;
    private String channelType;
    protected static EmbeddedContainer container;

    private Context context = new SimpleContextManager().getContext();

    private static final ServerApp SERVER_APP_APP = new ServerApp("/reply", false, "localhost", 8787);

    public TestMessagingReply(String containerClassName, ContentType contentType, String channelType) {
        this.contentType = contentType;
        this.channelType = channelType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "reply.war");
        war.addClass(ChatApplication.class);
        war.addClass(ReplyService.class);
        war.addAsWebInfResource(new File("granite-client-java-advanced/src/test/resources/META-INF/services-config.properties"), "classes/META-INF/services-config.properties");

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
    public void testTextReply() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);
        Consumer consumer = serverSession.getConsumer("secureChat", "chat", channelType);
        Identity identity = context.set("identity", new BaseIdentity(serverSession));
        Component replyService = context.set("replyService", new ComponentImpl(serverSession));

        String user = identity.login("user", "user00", TideResponders.<String>noop()).get();
        Assert.assertEquals("Logged in", "user", user);

        final CountDownLatch waitForSubscribe = new CountDownLatch(1);
        final boolean[] subscribed = new boolean[1];
        consumer.subscribe(new ResultIssuesResponseListener() {
            @Override
            public void onIssue(IssueEvent event) {
                waitForSubscribe.countDown();
            }

            @Override
            public void onResult(ResultEvent event) {
                subscribed[0] = true;
                waitForSubscribe.countDown();
            }
        });
        consumer.addMessageListener(new TopicMessageListener() {
            @Override
            public void onMessage(TopicMessageEvent event) {
                event.reply("Hello " + event.getMessage().getData());
            }
        });
        waitForSubscribe.await(15000, TimeUnit.MILLISECONDS);

        Assert.assertTrue("Consumer subscribed", consumer.isSubscribed());

        String reply = (String)replyService.call("requestReply", "bob").get();
        Assert.assertEquals("Hello bob", reply);

        consumer.unsubscribe().get();

        final CountDownLatch waitForLogout = new CountDownLatch(1);
        identity.logout(new TideResponder<Void>() {
            @Override
            public void result(TideResultEvent<Void> event) {
                waitForLogout.countDown();
            }

            @Override
            public void fault(TideFaultEvent event) {
                waitForLogout.countDown();
            }
        });
        waitForLogout.await(10000, TimeUnit.MILLISECONDS);

        serverSession.stop();
    }
}
