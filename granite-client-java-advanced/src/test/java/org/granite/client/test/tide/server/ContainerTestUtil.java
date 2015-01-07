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
package org.granite.client.test.tide.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Created by william on 04/02/14.
 */
public class ContainerTestUtil {

    private static final Logger log = Logger.getLogger(ContainerTestUtil.class);

    public static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    public static String[] CHANNEL_TYPES_ALL = new String[] {
        ChannelType.LONG_POLLING, ChannelType.WEBSOCKET
    };
    public static String[] CHANNEL_TYPES_NO_WEBSOCKET = new String[] {
        ChannelType.LONG_POLLING
    };
    public static String[] CHANNEL_TYPES_WEBSOCKET = new String[] {
        ChannelType.WEBSOCKET
    };

    public static List<Object[]> data() {
        return data(null);
    }

    public static List<Object[]> data(String[] channelTypes) {
        if (channelTypes != null) {
            String ct = System.getProperty("channel.types");
            if (ct != null)
                channelTypes = ct.split(",");
        }
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            if (channelTypes == null)
                params.add(new Object[] { CONTAINER_CLASS_NAME, contentType });
            else {
                for (String channelType : channelTypes) {
                    params.add(new Object[] { CONTAINER_CLASS_NAME, contentType, channelType });
                }
            }
        }
        return params;
    }

    public static EmbeddedContainer newContainer(WebArchive war, boolean persistSessions) throws Exception {
        try {
            return (EmbeddedContainer)TypeUtil.newInstance(CONTAINER_CLASS_NAME, new Class<?>[] { WebArchive.class, boolean.class }, new Object[] { war, persistSessions });
        }
        catch (Exception e) {
            log.error(e, "Could not create container of type %s", CONTAINER_CLASS_NAME);
            throw new Exception("Could not create container", e);
        }
    }

    public static ServerSession buildServerSession(Context context, ServerApp serverApp, ContentType contentType) throws Exception {
        ServerSession serverSession = context.set("serverSession", new ServerSession(serverApp));
        serverSession.setContentType(contentType);

        for (String channelType : CHANNEL_TYPES_ALL) {
            if (System.getProperty(channelType + ".transport.className") != null) {
                try {
                    serverSession.setMessagingTransport(channelType, TypeUtil.newInstance(System.getProperty(channelType + ".transport.className"), Transport.class));
                }
                catch (Exception e) {
                    log.error("Cannot setup websocket transport");
                }
            }
        }
        serverSession.start();
        return serverSession;
    }
}
