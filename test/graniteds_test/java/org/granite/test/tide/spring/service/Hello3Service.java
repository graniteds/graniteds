package org.granite.test.tide.spring.service;

import java.util.Map;

import org.springframework.stereotype.Service;


@Service("hello3")
public class Hello3Service {
    
    public String setMap(Map<String, String[]> map) {
    	if (map.get("toto").getClass().getComponentType().equals(String.class))
    		return "ok";
        return "nok";
    }
}
