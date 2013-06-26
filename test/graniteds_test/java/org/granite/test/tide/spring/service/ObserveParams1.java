package org.granite.test.tide.spring.service;

import org.granite.tide.data.DataObserveParams;
import org.granite.tide.data.DataPublishParams;
import org.granite.tide.data.DataTopicParams;

public class ObserveParams1 implements DataTopicParams {

	@Override
	public void observes(DataObserveParams params) {
		params.setSelector("user = 'USER'");
	}
	
	@Override
	public void publishes(DataPublishParams params, Object entity) {
	}
	
}