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

import org.granite.test.tide.data.*;
import org.granite.tide.hibernate4.HibernateDataChangeMergeListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;

@SuppressWarnings("deprecation")
public class TestHibernate4ChangeSetMerge extends AbstractTestChangeSetMerge {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction tx;
	
	@Override
	protected void initPersistence() {
		AnnotationConfiguration configuration = new AnnotationConfiguration()
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
		sessionFactory = configuration.buildSessionFactory();
		
		EventListenerRegistry registry = ((SessionFactoryImpl)sessionFactory).getServiceRegistry().getService(EventListenerRegistry.class);
		registry.setListeners(EventType.MERGE, new HibernateDataChangeMergeListener());
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
