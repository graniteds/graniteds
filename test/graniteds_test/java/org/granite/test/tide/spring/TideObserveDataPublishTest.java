package org.granite.test.tide.spring;

import javax.inject.Inject;

import org.granite.test.tide.spring.service.Params1Service;
import org.granite.test.tide.spring.service.Params2Service;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-observe.xml" })
public class TideObserveDataPublishTest extends AbstractTideTestCase {

	@Inject
	private Params1Service params1Service;
	
	@Inject
	private Params2Service params2Service;
	
    
	@Test
    public void testObserveGDS1122() {
				
		params1Service.method1();
		
		params2Service.method2();
    }
}
