package org.granite.test.tide.spring.data;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={ "/org/granite/test/tide/spring/data/test-context-data.xml" })
public class TestRepositoryFilter {

	@Inject
	private DataSetRepository repository;
	
	@Test
	public void testSimpleFilter() {
		DataSet dataSet = new DataSet();
		dataSet.setName("Bla*");
		Page<DataSet> list = repository.findByFilter(dataSet, new PageRequest(0, 25));
		
		Assert.assertEquals(0, list.getTotalElements());
	}
}
