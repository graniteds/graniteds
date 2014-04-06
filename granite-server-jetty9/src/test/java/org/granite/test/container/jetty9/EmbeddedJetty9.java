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
package org.granite.test.container.jetty9;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.jetty_7.api.ShrinkWrapWebAppContext;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedJetty9 implements Runnable, EmbeddedContainer {

    private static final Logger log = Logger.getLogger(EmbeddedJetty9.class);

    private WebArchive war;
    private boolean persistSessions;
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

    public EmbeddedJetty9(WebArchive war, boolean persistSessions) throws Exception {
        try {
            jetty = new Server();
            ServerConnector connector = new ServerConnector(jetty);
            connector.setHost("localhost");
            connector.setPort(8787);
            jetty.setConnectors(new Connector[]{connector});
            jetty.setHandler(new HandlerCollection(true));

            this.war = war;
            war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
            war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
            war.addAsLibraries(new File("granite-server-jetty9/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
            initWar(war);
            this.persistSessions = persistSessions;
        }
        catch (Exception e) {
            log.error(e, "Could not create Embedded Jetty 9");
        }
    }

    protected void initWar(WebArchive war) {
        // Force session creation with CreateSessionFilter
        // and register jetty websocket servlet
        war.setWebXML(new File("granite-server-jetty9/src/test/resources/web-websocket.xml"));
    }

    private CountDownLatch waitForStart = new CountDownLatch(1);

    public void run() {
        try {
            jetty.start();
            webAppContext = war.as(ShrinkWrapWebAppContext.class);
            webAppContext.setExtractWAR(true);
            webAppContext.setParentLoaderPriority(true);
            webAppContext.setContextPath("/" + war.getName().substring(0, war.getName().lastIndexOf(".")));
            webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
            webAppContext.setPersistTempDirectory(true);
            if (persistSessions) {
                HashSessionManager sessionManager = new HashSessionManager();
                sessionManager.setStoreDirectory(new File("granite-server-jetty9/build/tmp/jetty/sessions"));
                sessionManager.setLazyLoad(false);
                webAppContext.setSessionHandler(new SessionHandler(sessionManager));
            }
            HashLoginService loginService = new HashLoginService();
            loginService.putUser("user", Credential.getCredential("user00"), new String[] { "user" });
            webAppContext.getSecurityHandler().setLoginService(loginService);
            ((HandlerCollection)jetty.getHandler()).addHandler(webAppContext);
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
            webAppContext.stop();
            ((HandlerCollection)jetty.getHandler()).removeHandler(webAppContext);
            webAppContext.destroy();
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