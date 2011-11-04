package org.granite.test.tide.cdi.service;

import org.granite.test.tide.cdi.entity.Contact;


public class Hello2Service {
    
    public String hello(Contact contact) {
        return "Hello " + contact.getEmail();
    }
}
