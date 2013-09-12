package org.granite.test.tide.ejb.service;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.granite.tide.annotations.BypassTideMerge;


@Local(HelloMergeService.class)
@Stateless
@BypassTideMerge
public class HelloMergeServiceBean implements HelloMergeService {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
