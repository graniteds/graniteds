/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.udp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.udp.UdpChannelListener;
import org.granite.client.messaging.udp.UdpMessagingChannel;
import org.granite.client.test.server.ContainerTestUtil;
import org.granite.client.test.server.TestMessagingFeed;
import org.granite.client.test.server.feed.FeedApplication;
import org.granite.client.test.server.feed.FeedListener;
import org.granite.client.test.server.feed.Info;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.granite.util.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestUdpMessagingFeed extends TestMessagingFeed {

    private static final Logger log = Logger.getLogger(TestUdpMessagingFeed.class);

    private static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}")
    public static Iterable<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            params.add(new Object[] { CONTAINER_CLASS_NAME, contentType });
        }
        return params;
    }

    private static EmbeddedContainer container;

    public TestUdpMessagingFeed(String containerClassName, ContentType contentType) {
        super(containerClassName, contentType, ChannelType.UDP);
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a feed server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "feed.war");
        war.addClasses(FeedApplication.class, FeedListener.class, Info.class);
        war.addAsLibraries(new File("granite-server-udp/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsWebInfResource(new File("granite-client-java-udp/src/test/resources/granite-config-udp.xml"), "granite/granite-config.xml");

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


    @Override
    protected void waitForChannel(final Channel channel, final CyclicBarrier barrier) {
        ((UdpMessagingChannel)channel).addListener(new UdpChannelListener() {
            @Override
            public void onBound(UdpMessagingChannel channel) {
            }

            @Override
            public void onConnected(UdpMessagingChannel channel) {
                try {
                    barrier.await();
                }
                catch (Exception e) {
                }
            }

            @Override
            public void onClosed(UdpMessagingChannel channel) {
            }
        });
    }
}
