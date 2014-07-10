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
package org.granite.test.tide.hibernate4.data;

import java.io.Serializable;

import org.granite.test.tide.data.AbstractEntity;
import org.granite.test.tide.data.Address;
import org.granite.test.tide.data.Contact2;
import org.granite.test.tide.data.Country;
import org.granite.test.tide.data.Person2;
import org.granite.test.tide.data.Phone2;
import org.granite.tide.hibernate4.Hibernate4ChangeSetIntegrator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


public class TestHibernate4ChangeSetMerge extends AbstractTestChangeSetMerge {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction tx;
	
	@Override
	protected void initPersistence() {
		Configuration configuration = new Configuration()
			.addAnnotatedClass(AbstractEntity.class)
			.addAnnotatedClass(Address.class)
			.addAnnotatedClass(Contact1.class)
			.addAnnotatedClass(Country.class)
			.addAnnotatedClass(Person1.class)
			.addAnnotatedClass(Phone.class)
			.addAnnotatedClass(Contact2.class)
			.addAnnotatedClass(Person2.class)
			.addAnnotatedClass(Phone2.class)
			.setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
			.setProperty("hibernate.hbm2ddl.auto", "create-drop")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.connection.driver_class", org.h2.Driver.class.getName())
			.setProperty("hibernate.connection.url", "jdbc:h2:mem:test-loader")
			.setProperty("hibernate.connection.username", "sa")
			.setProperty("hibernate.connection.password", "");
		
		BootstrapServiceRegistryBuilder bsrb = new BootstrapServiceRegistryBuilder().with(new Hibernate4ChangeSetIntegrator());
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder(bsrb.build()).applySettings(configuration.getProperties());
		ServiceRegistry serviceRegistry = ssrb.build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
	}
	
	@Override
	protected void open() {
		session = sessionFactory.openSession();
		tx = session.beginTransaction();
	}
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return (T)session.load(entityClass, id);
	}
	@Override
	protected <T> T save(T entity) {
		session.save(entity);
		return entity;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T merge(T entity) {
		return (T)session.merge(entity);
	}
	@Override
	protected void flush() {
		session.flush();
		tx.commit();
	}
	@Override
	protected void close() {
		session.clear();
		session.close();
	}
}
