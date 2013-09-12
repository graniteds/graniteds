/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.spring;

import javax.inject.Inject;

import org.granite.test.tide.spring.service.Params1Service;
import org.granite.test.tide.spring.service.Params2Service;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-observe.xml" })
public class TestTideObserveDataPublish extends AbstractTideTestCase {

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
