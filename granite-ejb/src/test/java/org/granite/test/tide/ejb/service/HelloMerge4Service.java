package org.granite.test.tide.ejb.service;

import org.granite.tide.annotations.BypassTideMerge;


public interface HelloMerge4Service {
    
	@BypassTideMerge
    public String hello(String name);
}
