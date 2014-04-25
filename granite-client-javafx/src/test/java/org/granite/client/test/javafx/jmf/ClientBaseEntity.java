package org.granite.client.test.javafx.jmf;

import java.io.Serializable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;
import org.granite.messaging.annotations.Serialized;

@Entity
@Serialized(propertiesOrder={ "__initialized__", "__detachedState__", "id", "uid", "version" })
public abstract class ClientBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
    @Id
	private ObjectProperty<Long> id = new SimpleObjectProperty<Long>(this, "id");
    @Uid
	private StringProperty uid = new SimpleStringProperty(this, "uid");
    @Version
	private ObjectProperty<Integer> version = new SimpleObjectProperty<Integer>(this, "version");
	
	public ObjectProperty<Long> idProperty() {
		return id;
	}
    public void setId(Long value) {
        id.set(value);
    }
    public Long getId() {
        return id.get();
    }

	public StringProperty uidProperty() {
		return uid;
	}
    public void setUid(String value) {
    	uid.set(value);
    }
    public String getUid() {
        return uid.get();
    }
    
	public ObjectProperty<Integer> versionProperty() {
		return version;
	}
    public void setVersion(Integer value) {
        version.set(value);
    }
    public Integer getVersion() {
        return version.get();
    }
}
