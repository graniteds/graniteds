package org.granite.client.test.javafx.jmf;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.messaging.annotations.Serialized;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@RemoteAlias("org.granite.client.test.javafx.jmf.ConcreteEntity")
@Serialized(propertiesOrder={ "bla", "cla" })
public class ClientConcreteEntity extends ClientModifiableBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private StringProperty bla = new SimpleStringProperty(this, "bla");
	private StringProperty cla = new SimpleStringProperty(this, "cla");
    
	public StringProperty blaProperty() {
		return bla;
	}
    public void setBla(String value) {
    	bla.set(value);
    }
    public String getBla() {
        return bla.get();
    }
    
	public StringProperty claProperty() {
		return cla;
	}
    public void setCla(String value) {
    	cla.set(value);
    }
    public String getCla() {
        return cla.get();
    }
}
