/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import org.granite.client.messaging.*;
import org.granite.client.messaging.udp.UdpChannelBuilder;
import org.granite.client.test.server.ChatApplication;
import org.granite.client.test.server.TestMessaging;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by william on 30/09/13.
 */
@RunWith(Parameterized.class)
public class TestUdpMessaging extends TestMessaging {

    private static final Logger log = Logger.getLogger(TestUdpMessaging.class);

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

    private static final ServerApp SERVER_APP_APP = new ServerApp("/chat", false, "localhost", 8787);

    public TestUdpMessaging(String containerClassName, ContentType contentType) {
        super(containerClassName, contentType, UdpChannelBuilder.UDP_CHANNEL_TYPE);
    }

    @BeforeClass
    public static void startContainer() throws Exception {
        // Build a chat server application
        WebArchive war = ShrinkWrap.create(WebArchive.class, "chat.war");
        war.addClass(ChatApplication.class);
        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsLibraries(new File("granite-server-udp/build/libs/").listFiles(new Utils.ArtifactFileFilter()));
        war.addAsWebInfResource(new File("granite-client-java-udp/src/test/resources/granite-config-udp.xml"), "granite/granite-config.xml");

        container = (EmbeddedContainer) TypeUtil.newInstance(CONTAINER_CLASS_NAME, new Class<?>[]{WebArchive.class}, new Object[]{war});
        container.start();
        log.info("Container started");
    }

    @AfterClass
    public static void stopContainer() throws Exception {
        container.stop();
        log.info("Container stopped");
    }
}
