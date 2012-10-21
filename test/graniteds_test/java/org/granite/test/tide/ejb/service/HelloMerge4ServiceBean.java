package org.granite.test.tide.ejb.service;

import javax.ejb.Local;
import javax.ejb.Stateless;


@Local(HelloMerge4Service.class)
@Stateless
public class HelloMerge4ServiceBean implements HelloMerge4Service {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
