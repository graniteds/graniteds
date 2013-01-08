package org.granite.test.tide.ejb.service;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.granite.tide.annotations.BypassTideMerge;


@Local(HelloMerge3Service.class)
@Stateless
public class HelloMerge3ServiceBean implements HelloMerge3Service {
    
	@BypassTideMerge
    public String hello(String name) {
        return "Hello " + name;
    }
}
