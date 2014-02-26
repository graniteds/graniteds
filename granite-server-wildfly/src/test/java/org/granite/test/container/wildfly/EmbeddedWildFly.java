package org.granite.test.container.wildfly;

import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.StandaloneServer;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.util.Properties;

/**
 * Created by william on 20/02/14.
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

        Properties properties = new Properties();
        properties.put("jboss.home.dir", jbossHome.getAbsolutePath());
        properties.put("jboss.embedded.root", embeddedJbossHome.getAbsolutePath());
        properties.put("java.util.logging.manager", "org.jboss.logmanager.LogManager");
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
