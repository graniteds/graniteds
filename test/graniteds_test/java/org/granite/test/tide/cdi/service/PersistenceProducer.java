package org.granite.test.tide.cdi.service;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.granite.tide.data.DataEnabled;


@DataEnabled(topic="testTopic")
public class PersistenceProducer {
		
	@Produces
	public EntityManagerFactory initEntityManagerFactory() {
        return Persistence.createEntityManagerFactory("cdi-pu");
	}
}
