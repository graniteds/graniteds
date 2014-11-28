package org.granite.test.tide.data;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.granite.tide.hibernate.HibernateDataChangeMergeListener;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.event.MergeEventListener;

public class Hibernate3JPAChangeSetMergeTest extends AbstractChangeSetMergeTest {
	
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
		return (T)entityManager.merge(entity);
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
