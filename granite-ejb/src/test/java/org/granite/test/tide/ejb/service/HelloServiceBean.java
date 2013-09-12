package org.granite.test.tide.ejb.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;


@LocalBean
@Stateless
public class HelloServiceBean {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
