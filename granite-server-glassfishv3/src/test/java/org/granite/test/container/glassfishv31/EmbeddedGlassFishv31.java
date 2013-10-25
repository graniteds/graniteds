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
package org.granite.test.container.glassfishv31;

import org.glassfish.embeddable.*;
import org.granite.test.container.Utils;
import org.granite.test.container.EmbeddedContainer;
import org.granite.logging.Logger;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedGlassFishv31 implements Runnable, EmbeddedContainer {

    private static final Logger log = Logger.getLogger(EmbeddedGlassFishv31.class);

    private GlassFishRuntime glassfishRuntime;
    private GlassFish glassfish;
    private String appName;
    private File warFile;
    private Thread serverThread;

    public EmbeddedGlassFishv31(WebArchive war) throws Exception {
        BootstrapProperties bootstrapProps = new BootstrapProperties();
        glassfishRuntime = GlassFishRuntime.bootstrap(bootstrapProps);

        GlassFishProperties serverProps = new GlassFishProperties();
        // serverProps.setPort("http-listener", 8787);
        String configFileURI = new File("granite-server-glassfishv3/src/test/resources/domain.xml").toURI().toString();
        serverProps.setConfigFileURI(configFileURI);

        glassfish = glassfishRuntime.newGlassFish(serverProps);

        war.addAsLibraries(new File("granite-server-glassfishv3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-eclipselink/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.setWebXML(new File("granite-server-glassfishv3/src/test/resources/web-websocket.xml"));

        appName = war.getName().substring(0, war.getName().lastIndexOf("."));
        File root = File.createTempFile("emb-gfv3", war.getName());
        root.delete();
        root.mkdirs();
        root.deleteOnExit();
        warFile = new File(root, war.getName());
        warFile.deleteOnExit();
        war.as(ZipExporter.class).exportTo(warFile, true);

        serverThread = new Thread(this);
    }

    private CountDownLatch waitForStart = new CountDownLatch(1);

    public void run() {
        try {
            glassfish.start();

            CommandResult result = glassfish.getCommandRunner().run("set", "configs.config.server-config.network-config.protocols.protocol.http-listener.http.websockets-support-enabled=true");

            glassfish.getDeployer().deploy(warFile.toURI(), "--name", appName);

            log.info("Deployed applications: " + glassfish.getDeployer().getDeployedApplications());

            waitForStart.countDown();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start embedded glassfish", e);
        }
    }

    public void start() {
        serverThread.start();
        try {
            if (!waitForStart.await(20, TimeUnit.SECONDS))
                throw new RuntimeException("glassfish start timeout");
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Could not start glassfish", e);
        }
    }

    public void stop() {
        try {
            glassfish.getDeployer().undeploy(appName);
            glassfish.stop();
            glassfishRuntime.shutdown();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not stop embedded glassfish", e);
        }
    }
}