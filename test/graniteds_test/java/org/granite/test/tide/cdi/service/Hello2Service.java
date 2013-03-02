package org.granite.test.tide.cdi.service;

import org.granite.test.tide.data.Contact;


public class Hello2Service {
    
    public String hello(Contact contact) {
        return "Hello " + contact.getEmail();
    }
}
