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
import org.granite.client.test.server.chat.ChatApplication;
import org.granite.client.test.tide.server.remoting.RemotingApplication;
import org.granite.client.test.tide.server.remoting.TestService;
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
import org.granite.test.container.Utils;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestRemoting {

    private static final Logger log = Logger.getLogger(TestRemoting.class);

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}, channel: {2}")
    public static Iterable<Object[]> data() {
        return ContainerTestUtil.data();
    }

    private ContentType contentType;
    private String channelType;
    protected static EmbeddedContainer container;

    private Context context = new SimpleContextManager().getContext();

    private static final ServerApp SERVER_APP_APP = new ServerApp("/chat", false, "localhost", 8787);

    public TestRemoting(String containerClassName, ContentType contentType, String channelType) {
        this.contentType = contentType;
        this.channelType = channelType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "chat.war");
        war.addClass(TestService.class);
        war.addClass(RemotingApplication.class);
        war.addAsWebInfResource(new File("granite-client-java-advanced/src/test/resources/META-INF/services-config.properties"), "classes/META-INF/services-config.properties");
        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));

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
    public void testFailCall() throws Exception {
        ServerSession serverSession = ContainerTestUtil.buildServerSession(context, SERVER_APP_APP, contentType);

        Component testService = context.set("testService", new ComponentImpl(serverSession));
        final String[] message = new String[1];
        final CountDownLatch waitForCall = new CountDownLatch(1);
        testService.call("fail", new TideResponder<Void>() {
            @Override
            public void result(TideResultEvent<Void> event) {
                waitForCall.countDown();
            }

            @Override
            public void fault(TideFaultEvent event) {
                message[0] = event.getFault().getFaultDescription();
                waitForCall.countDown();
            }
        });
        waitForCall.await(10000, TimeUnit.MILLISECONDS);

        Assert.assertEquals("Fault message", "fail", message[0]);

        serverSession.stop();
    }
}
