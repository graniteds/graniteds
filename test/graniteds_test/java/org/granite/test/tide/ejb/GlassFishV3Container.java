package org.granite.test.tide.ejb;

import java.lang.reflect.Field;
import java.util.Map;

import javax.naming.InitialContext;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.embedded.EmbeddedDeployer;
import org.glassfish.internal.embedded.Server;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.glassfish.api.ShrinkwrapReadableArchive;


public class GlassFishV3Container implements EJBContainer {
	
	private ClassLoader classLoader = null;
	private Server server = null;
	private EmbeddedDeployer deployer = null;
	private String appName = null;
	private InitialContext ctx = null;
	
	public void start(JavaArchive archive) throws Exception {
		Server.Builder builder = new Server.Builder("test");
		server = builder.build();
		server.start();
		deployer = server.getDeployer();
		appName = deployer.deploy(archive.as(ShrinkwrapReadableArchive.class), new DeployCommandParameters());
		Field field = deployer.getClass().getDeclaredField("deployedApps");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)field.get(deployer);
		Object deployedInfo = map.get(appName);
		field = deployedInfo.getClass().getDeclaredField("appInfo");
		field.setAccessible(true);
		ApplicationInfo appInfo = (ApplicationInfo)field.get(deployedInfo);
		field = appInfo.getClass().getDeclaredField("appClassLoader");
		field.setAccessible(true);
		ClassLoader appClassLoader = (ClassLoader)field.get(appInfo);
		
		classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(appClassLoader);
		
		ctx = new InitialContext();     
	}
	
	public InitialContext getInitialContext() {
		return ctx;
	}

	public void stop() throws Exception {
		deployer.undeploy(appName, null);		
		server.stop();
        
        Thread.currentThread().setContextClassLoader(classLoader);
	}
}
