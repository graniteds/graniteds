/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx.tide;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class EmbeddedLocation {
    
    private StringProperty city = new SimpleStringProperty(this, "city");
    private StringProperty zipcode = new SimpleStringProperty(this, "zipcode");
    
    public EmbeddedLocation() {            
    }
    
    public EmbeddedLocation(String city, String zipcode) {
        this.city.set(city);
        this.zipcode.set(zipcode);
    }
    
    public StringProperty cityProperty() {
        return city;
    }
    
    public String getCity() {
        return city.get();
    }
    
    public void setCity(String city) {
        this.city.set(city);
    }
    
    public StringProperty zipcodeProperty() {
        return zipcode;
    }
    
    public String getZipcode() {
        return zipcode.get();
    }
    
    public void setZipcode(String zipcode) {
        this.zipcode.set(zipcode);
    }
}