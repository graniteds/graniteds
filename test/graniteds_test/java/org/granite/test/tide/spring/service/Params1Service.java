package org.granite.test.tide.spring.service;

import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@DataEnabled(topic="testTopic", params=ObserveParams1.class, publish=PublishMode.ON_COMMIT, useInterceptor=true)
public class Params1Service {
	
	public void method1() {
	}
}
