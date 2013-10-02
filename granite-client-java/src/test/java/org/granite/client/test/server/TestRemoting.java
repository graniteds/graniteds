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

import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.messaging.*;
import org.granite.client.messaging.channel.*;
import org.granite.client.messaging.events.*;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.transport.jetty.JettyWebSocketTransport;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestRemoting {

    private static final Logger log = Logger.getLogger(TestRemoting.class);

    private static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}")
    public static Iterable<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            params.add(new Object[] { CONTAINER_CLASS_NAME, contentType });
        }
        return params;
    }

    private ContentType contentType;
    private String containerClassName;
    private static EmbeddedContainer container;

    private static final ServerApp SERVER_APP_APP = new ServerApp("/data", false, "localhost", 8787);

    public TestRemoting(String containerClassName, ContentType contentType) {
        this.containerClassName = containerClassName;
        this.contentType = contentType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "data.war");
        war.addClasses(DataApplication.class, Data.class, DataServiceBean.class);
        war.addAsWebInfResource(new File("granite-client-java/src/test/resources/META-INF/persistence.xml"), "classes/META-INF/persistence.xml");
        war.addAsWebInfResource(new File("granite-client-java/src/test/resources/META-INF/services-config.properties"), "classes/META-INF/services-config.properties");
        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsLibraries(new File("granite-server-ejb/build/libs/").listFiles(new Utils.ArtifactFileFilter()));

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
        SimpleConfiguration configuration = new SimpleConfiguration("org/granite/client/configuration/granite-config.xml", null);
        ChannelFactory channelFactory = contentType.equals(ContentType.JMF_AMF) ? new JMFChannelFactory() : new AMFChannelFactory(null, configuration);
        channelFactory.setMessagingTransport("websocket", new JettyWebSocketTransport());
        channelFactory.start();
        return channelFactory;
    }

    @Test
    public void testCallJPASync() throws Exception {
        ChannelFactory channelFactory = buildChannelFactory();
        RemotingChannel channel = channelFactory.newRemotingChannel("graniteamf", SERVER_APP_APP, 1);
        RemoteService remoteService = new RemoteService(channel, "dataService");

        ResponseMessage createResult = remoteService.newInvocation("create", new Data("dataSync" + contentType)).invoke().get();

        ResponseMessage findAllResult = remoteService.newInvocation("findAll").invoke().get();

        List<Data> results = (List<Data>)findAllResult.getData();
        boolean found = false;
        for (Data result : results) {
            if (result.getValue().equals("dataSync" + contentType)) {
                found = true;
            }
        }
        Assert.assertTrue("Created data for sync call found", found);
    }

    @Test
    public void testCallJPAAsync() throws Exception {
        ChannelFactory channelFactory = buildChannelFactory();
        RemotingChannel channel = channelFactory.newRemotingChannel("graniteamf", SERVER_APP_APP, 1);
        final RemoteService remoteService = new RemoteService(channel, "dataService");

        final CountDownLatch waitForResult = new CountDownLatch(1);
        final List<Data> results = new ArrayList<Data>();

        remoteService.newInvocation("create", new Data("dataAsync" + contentType)).addListener(new ResultFaultIssuesResponseListener() {
            @Override
            public void onResult(ResultEvent event) {
                remoteService.newInvocation("findAll").addListener(new ResultFaultIssuesResponseListener() {
                    @Override
                    public void onResult(ResultEvent event) {
                        results.addAll((List<Data>)event.getResult());
                        waitForResult.countDown();
                    }

                    @Override
                    public void onFault(FaultEvent event) {
                    }

                    @Override
                    public void onIssue(IssueEvent event) {
                    }
                }).invoke();
            }

            @Override
            public void onFault(FaultEvent event) {
            }

            @Override
            public void onIssue(IssueEvent event) {
            }
        }).invoke();

        waitForResult.await(5, TimeUnit.SECONDS);

        boolean found = false;
        for (Data result : results) {
            if (result.getValue().equals("dataAsync" + contentType)) {
                found = true;
            }
        }
        Assert.assertTrue("Created data for async call found", found);
    }
}
