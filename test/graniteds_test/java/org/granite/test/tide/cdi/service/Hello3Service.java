package org.granite.test.tide.cdi.service;

import java.util.Map;


public class Hello3Service {
    
    public String setMap(Map<String, String[]> map) {
    	if (map.get("toto").getClass().getComponentType().equals(String.class))
    		return "ok";
        return "nok";
    }
}
