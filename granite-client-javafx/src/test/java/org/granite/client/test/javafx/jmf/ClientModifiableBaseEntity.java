package org.granite.client.test.javafx.jmf;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.messaging.annotations.Serialized;

@Entity
@Serialized(propertiesOrder={ "createDate", "createUser", "modifyDate", "modifyUser" })
public abstract class ClientModifiableBaseEntity extends ClientBaseEntity {

    private static final long serialVersionUID = 1L;

	private ObjectProperty<Date> createDate = new SimpleObjectProperty<Date>(this, "createDate");
	private StringProperty createUser = new SimpleStringProperty(this, "createUser");
	private ObjectProperty<Date> modifyDate = new SimpleObjectProperty<Date>(this, "modifyDate");
	private StringProperty modifyUser = new SimpleStringProperty(this, "modifyUser");
	
	public ObjectProperty<Date> createDateProperty() {
		return createDate;
	}
    public void setCreateDate(Date value) {
        createDate.set(value);
    }
    public Date getCreateDate() {
        return createDate.get();
    }
    
	public StringProperty createUserProperty() {
		return createUser;
	}
    public void setCreateUser(String value) {
        createUser.set(value);
    }
    public String getCreateUser() {
        return createUser.get();
    }
    
	public ObjectProperty<Date> modifyDateProperty() {
		return modifyDate;
	}
    public void setModifyDate(Date value) {
        modifyDate.set(value);
    }
    public Date getModifyDate() {
        return modifyDate.get();
    }
    
	public StringProperty modifyUserProperty() {
		return modifyUser;
	}
    public void setModifyUser(String value) {
        modifyUser.set(value);
    }
    public String getModifyUser() {
        return modifyUser.get();
    }
}
