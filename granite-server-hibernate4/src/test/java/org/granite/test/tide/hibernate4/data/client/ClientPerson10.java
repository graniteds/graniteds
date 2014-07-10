package org.granite.test.tide.hibernate4.data.client;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@Entity
@RemoteAlias("org.granite.test.tide.hibernate4.data.Person10")
public class ClientPerson10 extends AbstractClientEntity {
    
    private static final long serialVersionUID = 1L;

    public ClientPerson10() {
    }
    
    public ClientPerson10(Long id, Long version, String uid) {
    	super(id, version, uid);
    	parameters = new ArrayList<ClientKeyValue>();
    }
    
    private String firstName;
    
    private String lastName;
    
	private List<ClientKeyValue> parameters = new ArrayList<ClientKeyValue>();

    
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
	public List<ClientKeyValue> getParameters() {
		return parameters;
	}
	public void setParameters(List<ClientKeyValue> parameters) {
		this.parameters = parameters;
	}
    
}
