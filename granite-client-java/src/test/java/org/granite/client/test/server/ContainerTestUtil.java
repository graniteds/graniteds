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

import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.transport.Transport;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by william on 04/02/14.
 */
public class ContainerTestUtil {

    private static final Logger log = Logger.getLogger(ContainerTestUtil.class);

    public static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    public static Map<String, String> transports = new HashMap<String, String>();
    static {
        transports.put("websocket-jetty9", "org.granite.client.messaging.transport.jetty9.JettyWebSocketTransport");
        transports.put("websocket-std-jetty9", "org.granite.client.messaging.transport.jetty9.JettyStdWebSocketTransport");
        transports.put("websocket-std-tyrus", "org.granite.client.messaging.transport.tyrus.TyrusWebSocketTransport");
    }

    public static String[] CHANNEL_TYPES = new String[] {
        ChannelType.LONG_POLLING, ChannelType.WEBSOCKET
    };
    static {
        String channelTypes = System.getProperty("channel.types");
        if (channelTypes != null)
            CHANNEL_TYPES = channelTypes.split(",");
    }


    public static List<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            for (String channelType : CHANNEL_TYPES)
                params.add(new Object[] { CONTAINER_CLASS_NAME, contentType, channelType });
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

    public static ChannelFactory buildChannelFactory(ContentType contentType) {
        ChannelFactory channelFactory = contentType.equals(ContentType.JMF_AMF) ? new JMFChannelFactory() : new AMFChannelFactory();
        for (String channelType : CHANNEL_TYPES) {
            if (transports.containsKey(channelType)) {
                try {
                    channelFactory.setMessagingTransport(channelType, TypeUtil.newInstance(transports.get(channelType), Transport.class));
                }
                catch (Exception e) {
                    log.error("Cannot setup websocket transport");
                }
            }
        }
        channelFactory.start();
        return channelFactory;
    }
}
