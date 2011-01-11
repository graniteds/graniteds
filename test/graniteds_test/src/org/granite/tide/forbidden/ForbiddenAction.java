package org.granite.tide.forbidden;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("forbidden")
@Scope(ScopeType.STATELESS)
public class ForbiddenAction {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
