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
