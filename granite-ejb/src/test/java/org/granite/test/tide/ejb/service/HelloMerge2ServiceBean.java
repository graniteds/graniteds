package org.granite.test.tide.ejb.service;

import javax.ejb.Local;
import javax.ejb.Stateless;


@Local(HelloMerge2Service.class)
@Stateless
public class HelloMerge2ServiceBean implements HelloMerge2Service {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
