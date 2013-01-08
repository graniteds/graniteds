package org.granite.test.tide.ejb.service;

import javax.ejb.Local;
import javax.ejb.Stateless;


@Local(Hello2Service.class)
@Stateless
public class Hello2ServiceBean implements Hello2Service {
    
    public String hello2(String name) {
        return "Hello " + name;
    }
}
