package org.granite.test.tide.data;

import java.io.Serializable;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.tide.MockServletContext;
import org.hibernate.ejb.Ejb3Configuration;


public class Hibernate3JPADataPublishTest extends AbstractHibernate3DataPublishTest {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	protected void initPersistence() throws Exception {
		Ejb3Configuration configuration = new Ejb3Configuration()
			.addAnnotatedClass(AbstractEntity0.class)
			.addAnnotatedClass(Order3.class)
			.addAnnotatedClass(LineItem.class)
			.addAnnotatedClass(Contact5.class)
			.addAnnotatedClass(Alias5.class)
			.addAnnotatedClass(Location5.class)
            .setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
            .setProperty("hibernate.hbm2ddl.auto", "create-drop")
            .setProperty("hibernate.show_sql", "true")
            .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
            .setProperty("hibernate.connection.url", "jdbc:h2:mem:test-publish")
            .setProperty("hibernate.connection.username", "sa")
            .setProperty("hibernate.connection.password", "");
		
		entityManagerFactory = configuration.buildEntityManagerFactory();
		
		ServletContext servletContext = new MockServletContext();
        Configuration cfg = new ConfigurationImpl();
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-hibernate.xml");
        cfg.setFlexServicesConfig("/WEB-INF/flex/services-config-hibernate.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
        SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
	}
	
	protected void open() {
		entityManager = entityManagerFactory.createEntityManager();
		tx = entityManager.getTransaction();
		tx.begin();
	}
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return entityManager.find(entityClass, id);
	}
	protected <T> T save(T entity) {
		return entityManager.merge(entity);
	}
	protected <T> void remove(T entity) {
		entityManager.remove(entity);
	}
	protected void flush() {
		flush(true);
	}
	protected void flush(boolean commit) {
		entityManager.flush();
		if (commit)
			tx.commit();
	}
	protected void flushOnly() {
		entityManager.flush();
	}
	protected void close() {
		entityManager.clear();
		entityManager.close();
	}
}
