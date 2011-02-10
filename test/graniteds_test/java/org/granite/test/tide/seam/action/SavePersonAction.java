package org.granite.test.tide.seam.action;

import org.granite.test.tide.seam.entity.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;


@Name("savePerson")
@Scope(ScopeType.EVENT)
public class SavePersonAction {
    
    @In
    private Person person;
    
    public boolean save() {
        return "test".equals(person.getLastName());
    }
}
