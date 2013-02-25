package org.granite.test.tide.spring;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.test.tide.data.Person;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-jpa.xml" })
public class AbstractTideLazyLoadingJPATest extends AbstractTideTestCase {
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	@Inject
	private PlatformTransactionManager txManager;
    
	@Test
    public void testLazyLoading() {
		TransactionDefinition def = new DefaultTransactionDefinition();
		
		TransactionStatus tx = txManager.getTransaction(def);
		Person person = new Person();
		entityManager.persist(person);
		txManager.commit(tx);
		
		tx = txManager.getTransaction(def);
		person = entityManager.find(Person.class, person.getId());
		txManager.commit(tx);
		
        Object result = initializeObject(person, new String[] { "contacts" });
        
        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        Assert.assertTrue("Collection initialized", classGetter.isInitialized(result, "contacts", null));
        
        checkOpenSessions();
    }
	
	protected void checkOpenSessions() {
	}
}
