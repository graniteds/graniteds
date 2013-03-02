package org.granite.test.tide.spring.service;

import org.granite.test.tide.data.AbstractEntity;
import org.granite.test.tide.data.Person;
import org.springframework.stereotype.Service;


@Service("testService")
public class TestService {
	
    public String create(AbstractEntity entity) {
    	return "entity";
    }
    
    public String create(Person entity) {
    	return "person";
    }
}
