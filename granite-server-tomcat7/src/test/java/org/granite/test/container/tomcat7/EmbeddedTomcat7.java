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
package org.granite.test.container.tomcat7;

import org.apache.catalina.Host;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.Tomcat;
import org.granite.test.container.Utils;
import org.granite.test.container.EmbeddedContainer;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedTomcat7 implements Runnable, EmbeddedContainer {

    private File tomcatHome;
    private Tomcat tomcat;
    private Host host;
    private File appBase;
    private File warFile;
    private Thread serverThread;

    public EmbeddedTomcat7(WebArchive war) throws Exception {
        this(war, false);
    }

    public EmbeddedTomcat7(WebArchive war, boolean persistSessions) throws Exception {
        tomcatHome = File.createTempFile("emb-t7-", "");
        if (!tomcatHome.delete() || !tomcatHome.mkdirs())
            throw new RuntimeException("Could not create embedded tomcat 7 home");

        System.setProperty("catalina.base", tomcatHome.getAbsolutePath());
        // Trigger loading of catalina.properties.
        CatalinaProperties.getProperty("dummy");

        appBase = new File(tomcatHome, "webapps");
        if (!appBase.exists() && !appBase.mkdirs())
            throw new RuntimeException("Count not create tomcat appBase " + appBase.getAbsolutePath());

        tomcat = new Tomcat();
        tomcat.setPort(8787);
        tomcat.setBaseDir(tomcatHome.getAbsolutePath());
        host = tomcat.getHost();
        host.setAppBase(appBase.getAbsolutePath());
        host.setAutoDeploy(false);
        host.setDeployOnStartup(true);
        host.setConfigClass(EmbeddedContextConfig.class.getCanonicalName());

        war.addAsLibraries(new File("granite-server-tomcat7/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.setWebXML(new File("granite-server-tomcat7/src/test/resources/web-websocket.xml"));

        warFile = new File(appBase, war.getName());
        if (warFile.exists())
            warFile.delete();

        war.as(ZipExporter.class).exportTo(warFile, true);
        tomcat.addWebapp("/" + warFile.getName().substring(0, warFile.getName().lastIndexOf(".")), warFile.getAbsolutePath());
    }

    private CountDownLatch waitForStart = new CountDownLatch(1);

    public void run() {
        try {
            tomcat.start();

            waitForStart.countDown();

            tomcat.getServer().await();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start embedded tomcat", e);
        }
    }

    @Override
    public void start() {
        serverThread = new Thread(this);
        serverThread.start();
        try {
            if (!waitForStart.await(20, TimeUnit.SECONDS))
                throw new RuntimeException("tomcat start timeout");
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Could not start tomcat", e);
        }
    }

    @Override
    public void stop() {
        try {
            tomcat.getConnector().stop();
            tomcat.getServer().stop();
            tomcat.stop();
            serverThread.interrupt();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not stop embedded tomcat", e);
        }
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public void destroy() {
        try {
            tomcat.destroy();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not destroy embedded tomcat", e);
        }
        deleteDir(tomcatHome);
    }

    private void deleteDir(File path) {
        if (path == null)
            return;
        if (path.exists()) {
            for (File f : path.listFiles()) {
                if (f.isDirectory())
                    deleteDir(f);
                else
                    f.delete();
            }
            path.delete();
        }
    }}
