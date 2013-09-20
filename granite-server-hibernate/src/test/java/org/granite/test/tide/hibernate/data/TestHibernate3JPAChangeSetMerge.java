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
package org.granite.test.tide.hibernate.data;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.granite.test.tide.data.*;
import org.granite.tide.hibernate.HibernateDataChangeMergeListener;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.event.MergeEventListener;

public class TestHibernate3JPAChangeSetMerge extends AbstractTestChangeSetMerge {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction tx;
	
	@Override
	protected void initPersistence() {
		Ejb3Configuration configuration = new Ejb3Configuration()
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
		configuration.setListeners("merge", new MergeEventListener[] { new HibernateDataChangeMergeListener() });
		entityManagerFactory = configuration.buildEntityManagerFactory();
	}

	@Override
	protected void open() {
		entityManager = entityManagerFactory.createEntityManager();
		tx = entityManager.getTransaction();
		tx.begin();
	}
	@Override
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return entityManager.find(entityClass, id);
	}
	@Override
	protected <T> T save(T entity) {
		entityManager.persist(entity);
		return entity;
	}
	@Override
	protected <T> T merge(T entity) {
		return entityManager.merge(entity);
	}
	@Override
	protected void flush() {
		entityManager.flush();
		tx.commit();
	}
	@Override
	protected void close() {
		entityManager.clear();
		entityManager.close();
	}
}
