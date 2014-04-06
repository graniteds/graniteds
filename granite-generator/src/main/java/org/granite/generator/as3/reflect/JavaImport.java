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
package org.granite.generator.as3.reflect;

import java.net.URL;

import org.granite.generator.as3.PropertyType;

/**
 * @author Franck WOLFF
 */
public class JavaImport extends JavaAbstractType {

    public JavaImport(JavaTypeFactory provider, Class<?> type, URL url) {
        super(provider, type, url);
    }
    
    public JavaImport(JavaTypeFactory provider, Class<?> type, URL url, PropertyType propertyType) {
        super(provider, type, url, propertyType);
    }
    
    public boolean hasImportPackage() {
    	return getClientType().hasPackage();
    }
    
    public String getImportPackageName() {
    	return getClientType().getPackageName();
    }
    
    public String getImportQualifiedName() {
    	return getClientType().getQualifiedName();
    }
}
