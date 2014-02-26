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
package org.granite.test.container.jetty8;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.granite.test.container.Utils;
import org.granite.test.container.EmbeddedContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.jetty_7.api.ShrinkWrapWebAppContext;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedJetty8 implements Runnable, EmbeddedContainer {

    private Server jetty;
    private WebAppContext webAppContext;
    private Thread serverThread;

    public static final String[] CONFIGURATION_CLASSES = {
        "org.eclipse.jetty.webapp.WebInfConfiguration",
        "org.eclipse.jetty.webapp.WebXmlConfiguration",
        "org.eclipse.jetty.webapp.MetaInfConfiguration",
        "org.eclipse.jetty.webapp.FragmentConfiguration",
        "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
        "org.eclipse.jetty.annotations.AnnotationConfiguration"
    };

    public EmbeddedJetty8(WebArchive war, boolean persistSessions) throws Exception {
        jetty = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setHost("localhost");
        connector.setPort(8787);
        jetty.setConnectors(new Connector[]{connector});
        jetty.setHandler(new HandlerCollection(true));

        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-jetty8/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        // Not sure why these libs muse be added as they are already in the classpath
        war.addAsLibrary(Utils.findJarContainingResource("META-INF/maven/org.eclipse.jetty/jetty-websocket/pom.xml"));
        war.addAsLibrary(Utils.findJarContainingResource("META-INF/maven/org.eclipse.jetty/jetty-server/pom.xml"));
        war.addAsLibrary(Utils.findJarContainingResource("META-INF/maven/org.eclipse.jetty/jetty-util/pom.xml"));
        war.addAsLibrary(Utils.findJarContainingResource("META-INF/maven/org.eclipse.jetty/jetty-plus/pom.xml"));
        war.addAsLibrary(Utils.findJarContainingResource("META-INF/maven/org.eclipse.jetty/jetty-annotations/pom.xml"));
        war.setWebXML(new File("granite-server-jetty8/src/test/resources/web-websocket.xml"));

        webAppContext = war.as(ShrinkWrapWebAppContext.class);
        webAppContext.setExtractWAR(true);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setContextPath("/" + war.getName().substring(0, war.getName().lastIndexOf(".")));
        webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
        if (persistSessions) {
            HashSessionManager sessionManager = new HashSessionManager();
            sessionManager.setStoreDirectory(new File("granite-server-jetty8/build/tmp/jetty/sessions"));
            sessionManager.setLazyLoad(false);
            webAppContext.setSessionHandler(new SessionHandler(sessionManager));
        }
        ((HandlerCollection)jetty.getHandler()).addHandler(webAppContext);
    }

    private CountDownLatch waitForStart = new CountDownLatch(1);

    public void run() {
        try {
            jetty.start();
            webAppContext.start();

            waitForStart.countDown();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start embedded jetty", e);
        }
    }

    @Override
    public void start() {
        serverThread = new Thread(this);
        serverThread.start();
        try {
            if (!waitForStart.await(20, TimeUnit.SECONDS))
                throw new RuntimeException("jetty start timeout");
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Could not start jetty", e);
        }
    }

    @Override
    public void stop() {
        try {
            jetty.stop();
            serverThread.interrupt();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not stop embedded jetty", e);
        }
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public void destroy() {
    }
}