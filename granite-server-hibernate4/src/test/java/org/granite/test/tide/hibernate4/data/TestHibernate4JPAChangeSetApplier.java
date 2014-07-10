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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.granite.hibernate4.ProxyFactory;
import org.granite.tide.data.JPAPersistenceAdapter;
import org.granite.tide.data.TidePersistenceAdapter;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;


public class TestHibernate4JPAChangeSetApplier extends AbstractTestChangeSetApplier {
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private EntityTransaction entityTransaction;
	
	@Override
	protected void initPersistence() {
		Map<String, String> props = new HashMap<String, String>();
		props.put("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName());
		props.put("hibernate.hbm2ddl.auto", "create-drop");
		props.put("hibernate.show_sql", "true");
		props.put("hibernate.connection.driver_class", org.h2.Driver.class.getName());
		props.put("hibernate.connection.url", "jdbc:h2:mem:test-changesetapplier");
		props.put("hibernate.connection.username", "sa");
		props.put("hibernate.connection.password", "");
		
		entityManagerFactory = Persistence.createEntityManagerFactory("hibernate4-changesetapplier-pu", props);
	}

	@Override
	protected void open() {
		entityManager = entityManagerFactory.createEntityManager();
		entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
	}
	@Override
	protected <T> T find(Class<T> entityClass, Serializable id) {
		return entityManager.find(entityClass, id);
	}
	@Override
	protected <T> T save(T entity) {
		return entityManager.merge(entity);
	}
	@Override
	protected void flush() {
		entityManager.flush();
	}
	@Override
	protected void close() {
		entityTransaction.commit();
		entityManager.close();
	}
	@Override
	protected TidePersistenceAdapter newPersistenceAdapter() {
		return new JPAPersistenceAdapter(entityManager);
	}
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T newProxy(Class<T> entityClass, Serializable id) {
		ProxyFactory proxyFactory = new ProxyFactory(JavassistLazyInitializer.class.getName());
		return (T)proxyFactory.getProxyInstance(entityClass.getName(), entityClass.getName(), id);
	}
}
