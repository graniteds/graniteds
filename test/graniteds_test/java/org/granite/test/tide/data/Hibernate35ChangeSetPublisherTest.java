package org.granite.test.tide.data;

import org.granite.tide.hibernate.Hibernate35DataChangePublishListener;
import org.hibernate.cfg.Configuration;

public class Hibernate35ChangeSetPublisherTest extends AbstractHibernate3ChangeSetPublisherTest {
	
	@Override
	protected void setListeners(Configuration configuration) {
		configuration.setListener("flush-entity", new Hibernate35DataChangePublishListener());
		configuration.setListener("post-insert", new Hibernate35DataChangePublishListener());
		configuration.setListener("post-update", new Hibernate35DataChangePublishListener());
		configuration.setListener("post-delete", new Hibernate35DataChangePublishListener());
		configuration.setListener("pre-collection-update", new Hibernate35DataChangePublishListener());
	}
}