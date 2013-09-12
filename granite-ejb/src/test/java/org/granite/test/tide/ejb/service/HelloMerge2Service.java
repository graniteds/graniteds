package org.granite.test.tide.ejb.service;

import org.granite.tide.annotations.BypassTideMerge;


@BypassTideMerge
public interface HelloMerge2Service {
    
    public String hello(String name);
}
