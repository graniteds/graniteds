package org.granite.client.test.tide.server;

import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.util.ContentType;
import org.granite.util.TypeUtil;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by william on 04/02/14.
 */
public class ContainerTestUtil {

    private static final Logger log = Logger.getLogger(ContainerTestUtil.class);

    public static String CONTAINER_CLASS_NAME = System.getProperty("container.className");

    public static String[] CHANNEL_TYPES = new String[] {
        // ChannelType.LONG_POLLING, ChannelType.WEBSOCKET
        ChannelType.WEBSOCKET
    };

    public static List<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for (ContentType contentType : Arrays.asList(ContentType.JMF_AMF, ContentType.AMF)) {
            for (String channelType : CHANNEL_TYPES) {
                params.add(new Object[] { CONTAINER_CLASS_NAME, contentType, channelType });
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

        for (String channelType : CHANNEL_TYPES) {
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
