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
package org.granite.test.container.wildfly;

import java.io.File;

import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.StandaloneServer;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Created by william on 20/02/14.
 * 
 * Note:
 * 
 * (1) Edit [wildfly.home]/modules/system/layers/base/org/jboss/as/embedded/main/module.xml
 * and fix module dependencies as follow:
 * 
 * <![CDATA[
 * <dependencies>
 *      <module name="javax.ejb.api"/>
 *      <module name="org.wildfly.security.manager"/>
 *      <module name="org.jboss.as.server"/>
 *      <module name="org.jboss.as.controller"/>
 *      <module name="org.jboss.as.controller-client"/>
 *      <module name="org.jboss.as.protocol"/>
 *      <module name="org.jboss.msc"/>
 *      <module name="org.jboss.jandex"/>
 *      <module name="org.jboss.logging"/>
 *      <module name="org.jboss.modules"/>
 *      <module name="org.jboss.staxmapper"/>
 *      <module name="org.jboss.vfs"/>
 * </dependencies>
 * ]]>
 * 
 * (2) Edit [wildfly.home]/standalone/configuration/application-roles.properties and append:
 * 
 * user=user
 * 
 * (3) Edit [wildfly.home]/standalone/configuration/application-users.properties and append:
 * 
 * user=6202072911c699f7abeb7fae03232794
 */
public class EmbeddedWildFly implements EmbeddedContainer {
	
    private File jbossHome = new File(System.getProperty("jboss.home"));
    private File embeddedJbossHome;
    private StandaloneServer jboss;
    private File warFile;

    public EmbeddedWildFly(WebArchive war, boolean persistSessions) throws Exception {
        embeddedJbossHome = File.createTempFile("emb-wildfly-", "");
        embeddedJbossHome.delete();
        embeddedJbossHome.mkdirs();

        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("jboss.http.port", "8787");
        System.setProperty("jboss.embedded.root", embeddedJbossHome.getAbsolutePath());

        File modulesHome = new File(jbossHome, "modules");
        File bundlesHome = new File(jbossHome, "bundles");
        
        jboss = EmbeddedServerFactory.create(jbossHome.getAbsolutePath(), modulesHome.getAbsolutePath(), bundlesHome.getAbsolutePath());

        war.addAsLibraries(new File("granite-server-core/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-servlet3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-jboss/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-wildfly/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.setWebXML(new File("granite-server-wildfly/src/test/resources/web-websocket-std.xml"));

        File tmp = File.createTempFile("exp-wildfly-", "");
        tmp.delete();
        tmp.mkdirs();
        warFile = new File(tmp, war.getName());
        war.as(ZipExporter.class).exportTo(warFile, true);
    }

    @SuppressWarnings("deprecation")
	@Override
    public void start() {
        try {
            jboss.start();
            jboss.deploy(warFile);
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start embedded jboss", e);
        }
    }

    @Override
    public void stop() {
        jboss.stop();
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
