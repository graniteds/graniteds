package org.granite.test.tide.cdi.service;

import javax.inject.Named;


@Named("helloService")
public class HelloService {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
