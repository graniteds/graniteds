package org.granite.test.tide.data;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.granite.tide.hibernate.HibernateDataPublishListener;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;


@SuppressWarnings("deprecation")
public class Hibernate3DataPublishTest extends AbstractHibernate3DataPublishTest {
	
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
		
		initListener(configuration, "post-insert", HibernateDataPublishListener.class.getName());
		initListener(configuration, "post-update", HibernateDataPublishListener.class.getName());
		initListener(configuration, "post-delete", HibernateDataPublishListener.class.getName());
		
		sessionFactory = configuration.buildSessionFactory();
	}
	
	private static void initListener(AnnotationConfiguration configuration, String event, String listener) {
		try {
			Method m = configuration.getClass().getMethod("setListener", String.class, String.class);
			m.invoke(configuration, event, listener);
		} 
		catch (Exception e) {
			throw new RuntimeException("Could not set listener on session factory", e);
		}
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
