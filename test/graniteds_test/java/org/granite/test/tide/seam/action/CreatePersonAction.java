package org.granite.test.tide.seam.action;

import org.granite.test.tide.seam.entity.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.international.StatusMessage.Severity;


@Name("createPerson")
@Scope(ScopeType.EVENT)
public class CreatePersonAction {
    
    @Out
    private Person person;
    
    public void create(String lastName) {
        person = new Person();
        person.initIdUid(12, null);
        person.setLastName(lastName);
        
        StatusMessages.instance().add(Severity.ERROR, "person created");
    }
}
