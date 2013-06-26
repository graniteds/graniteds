package org.granite.test.tide.spring.service;

import org.granite.tide.data.DataObserveParams;
import org.granite.tide.data.DataPublishParams;
import org.granite.tide.data.DataTopicParams;

public class ObserveParams2 implements DataTopicParams {

	@Override
	public void observes(DataObserveParams params) {
		params.addValue("site", "SITE");
	}
	
	@Override
	public void publishes(DataPublishParams params, Object entity) {
	}
	
}