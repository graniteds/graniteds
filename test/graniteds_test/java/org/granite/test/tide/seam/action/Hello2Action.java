package org.granite.test.tide.seam.action;

import org.granite.test.tide.data.Contact;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("hello2")
@Scope(ScopeType.EVENT)
public class Hello2Action {
    
    @In
    private Contact contact;
    
    public String hello() {
        return "Hello " + contact.getEmail();
    }
}
