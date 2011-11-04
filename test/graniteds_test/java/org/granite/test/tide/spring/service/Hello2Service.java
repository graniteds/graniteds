package org.granite.test.tide.spring.service;

import org.granite.test.tide.spring.entity.Contact;
import org.springframework.stereotype.Service;


@Service
public class Hello2Service {
    
    public String hello(Contact contact) {
        return "Hello " + contact.getEmail();
    }
}
