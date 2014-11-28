package org.granite.test.tide.data;

import java.io.Serializable;

import org.granite.tide.hibernate.HibernateDataChangeMergeListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

@SuppressWarnings("deprecation")
public class Hibernate3ChangeSetMergeTest extends AbstractChangeSetMergeTest {
	
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
		configuration.setListener("merge", new HibernateDataChangeMergeListener());
		sessionFactory = configuration.buildSessionFactory();
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
