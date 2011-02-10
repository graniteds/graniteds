package org.granite.test.tide.seam.action;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("hello3")
@Scope(ScopeType.EVENT)
public class Hello3Action {
    
    public String setMap(Map<String, String[]> map) {
    	if (map.get("toto").getClass().getComponentType().equals(String.class))
    		return "ok";
        return "nok";
    }
}
