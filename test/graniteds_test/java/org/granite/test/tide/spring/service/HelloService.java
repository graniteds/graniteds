package org.granite.test.tide.spring.service;

import org.springframework.stereotype.Service;


@Service("hello")
public class HelloService {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
