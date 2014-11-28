package org.granite.test.tide.data;

import org.granite.tide.hibernate.HibernateDataChangePublishListener;
import org.hibernate.cfg.Configuration;

public class Hibernate3ChangeSetPublisherTest extends AbstractHibernate3ChangeSetPublisherTest {
	
	@Override
	protected void setListeners(Configuration configuration) {
		configuration.setListener("flush-entity", new HibernateDataChangePublishListener());
		configuration.setListener("post-insert", new HibernateDataChangePublishListener());
		configuration.setListener("post-update", new HibernateDataChangePublishListener());
		configuration.setListener("post-delete", new HibernateDataChangePublishListener());
		configuration.setListener("pre-collection-update", new HibernateDataChangePublishListener());
	}
}