/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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
