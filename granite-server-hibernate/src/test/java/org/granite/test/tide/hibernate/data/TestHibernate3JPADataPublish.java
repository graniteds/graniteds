/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.hibernate.data;

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
import org.granite.test.tide.data.AbstractEntity0;
import org.granite.test.tide.data.Alias5;
import org.granite.test.tide.data.Contact5;
import org.granite.test.tide.data.LineItem;
import org.granite.test.tide.data.Location5;
import org.granite.test.tide.data.Order3;
import org.hibernate.ejb.Ejb3Configuration;


public class TestHibernate3JPADataPublish extends AbstractTestHibernate3DataPublish {
	
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
