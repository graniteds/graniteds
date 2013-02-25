package org.granite.test.tide.seam.action;

import org.granite.test.tide.data.Person;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;


@Name("createPerson")
@Scope(ScopeType.EVENT)
public class CreatePersonAction {
    
    @Out
    private Person person;
    
    public void create(String lastName) {
        person = new Person();
        person.initIdUid(12L, null);
        person.setLastName(lastName);
        
        StatusMessages.instance().add(Severity.ERROR, "person created");
    }
}
