/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.test.server.data.Data;
import org.granite.client.test.server.data.DataApplication;
import org.granite.client.test.server.data.DataServiceBean;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.granite.util.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestRemotingData {

    private static final Logger log = Logger.getLogger(TestRemotingData.class);

    @Parameterized.Parameters(name = "container: {0}, encoding: {1}")
    public static Iterable<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            params.add(new Object[] { ContainerTestUtil.CONTAINER_CLASS_NAME, contentType });
        }
        return params;
    }

    private ContentType contentType;
    private static EmbeddedContainer container;

    private static final ServerApp SERVER_APP_APP = new ServerApp("/data", false, "localhost", 8787);

    public TestRemotingData(String containerClassName, ContentType contentType) {
        this.contentType = contentType;
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a EJB/JPA data server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "data.war");
        war.addClasses(DataApplication.class, Data.class, DataServiceBean.class);
        war.addAsWebInfResource(new File("granite-client-java/src/test/resources/META-INF/persistence.xml"), "classes/META-INF/persistence.xml");
        war.addAsWebInfResource(new File("granite-client-java/src/test/resources/META-INF/services-config.properties"), "classes/META-INF/services-config.properties");
        war.addAsLibraries(new File("granite-server-ejb/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));

        container = ContainerTestUtil.newContainer(war, false);
        container.start();
        log.info("Container started");
    }

    @AfterClass
    public static void stopContainer() throws Exception {
        container.stop();
        log.info("Container stopped");
    }

    private ChannelFactory channelFactory;
    private RemotingChannel channel;

    @Before
    public void before() throws Exception {
        channelFactory = contentType.equals(ContentType.JMF_AMF) ? new JMFChannelFactory() : new AMFChannelFactory();
        channelFactory.start();
        channel = channelFactory.newRemotingChannel("graniteamf", SERVER_APP_APP, 1);
    }

    @After
    public void after() throws Exception {
        channel.stop();
        channelFactory.stop();
    }

    @Test
    public void testCallJPASync() throws Exception {
        RemoteService remoteService = new RemoteService(channel, "dataService");

        @SuppressWarnings("unused")
		ResponseMessage createResult = remoteService.newInvocation("create", new Data("dataSync" + contentType)).invoke().get();

        ResponseMessage findAllResult = remoteService.newInvocation("findAll").invoke().get();

        @SuppressWarnings("unchecked")
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
        final RemoteService remoteService = new RemoteService(channel, "dataService");

        final CountDownLatch waitForResult = new CountDownLatch(1);
        final List<Data> results = new ArrayList<Data>();

        remoteService.newInvocation("create", new Data("dataAsync" + contentType)).addListener(new ResultFaultIssuesResponseListener() {
            @Override
            public void onResult(ResultEvent event) {
                remoteService.newInvocation("findAll").addListener(new ResultFaultIssuesResponseListener() {
                    @SuppressWarnings("unchecked")
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

    @Test
    public void testCallFailedRuntimeException() throws Exception {
        RemoteService remoteService = new RemoteService(channel, "dataService");

        ResponseMessage failResult = remoteService.newInvocation("fail").invoke().get();

        Assert.assertTrue("Call failed", failResult instanceof FaultMessage);
    }
}
