/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.hibernate.data;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.granite.test.tide.data.AbstractEntity0;
import org.granite.test.tide.data.Alias5;
import org.granite.test.tide.data.Contact5;
import org.granite.test.tide.data.LineItem;
import org.granite.test.tide.data.Location5;
import org.granite.test.tide.data.Order3;
import org.granite.tide.hibernate.HibernateDataPublishListener;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;


@SuppressWarnings("deprecation")
public class TestHibernate3DataPublish extends AbstractTestHibernate3DataPublish {
	
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction tx;
	
	protected void initPersistence() throws Exception {
		AnnotationConfiguration configuration = new AnnotationConfiguration()
			.addAnnotatedClass(AbstractEntity0.class)
			.addAnnotatedClass(Order3.class)
			.addAnnotatedClass(LineItem.class)
			.addAnnotatedClass(Contact5.class)
			.addAnnotatedClass(Location5.class)
			.addAnnotatedClass(Alias5.class)
            .setProperty("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName())
            .setProperty("hibernate.hbm2ddl.auto", "create-drop")
            .setProperty("hibernate.show_sql", "true")
            .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
            .setProperty("hibernate.connection.url", "jdbc:h2:mem:test-publish")
            .setProperty("hibernate.connection.username", "sa")
            .setProperty("hibernate.connection.password", "");
		
		configuration.setListener("post-insert", HibernateDataPublishListener.class.getName());
		configuration.setListener("post-update", HibernateDataPublishListener.class.getName());
		configuration.setListener("post-delete", HibernateDataPublishListener.class.getName());
		
		sessionFactory = configuration.buildSessionFactory();
	}
	
	protected void open() {
		Method m;
		try {
			m = sessionFactory.getClass().getMethod("openSession");
			session = (Session)m.invoke(sessionFactory);
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not open session", e);
		}
		tx = session.beginTransaction();
	}
	@SuppressWarnings("unchecked")
	protected <T> T find(Class<T> entityClass, Serializable id) {
		Criteria c = session.createCriteria(entityClass);
		c.add(Restrictions.idEq(id));
		return (T)c.uniqueResult();
	}
	@SuppressWarnings("unchecked")
	protected <T> T save(T entity) {
		return (T)session.merge(entity);
	}
	protected <T> void remove(T entity) {
		session.delete(entity);
	}
	protected void flush() {
		flush(true);
	}
	protected void flush(boolean commit) {
		session.flush();
		if (commit)
			tx.commit();
	}
	protected void flushOnly() {
		session.flush();
	}
	protected void close() {
		session.clear();
		session.close();
	}
}
