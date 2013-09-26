/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.util.List;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PackageTranslator;


/**
 * @author Franck WOLFF
 */
public class ClientImport extends JavaImport {

	private final String className;
	private final ClientType clientType = new ClientImportType();
	
    public ClientImport(JavaTypeFactory provider, String className, boolean property) {
        super(provider, Object.class, null);
        this.className = className;
    }
    
    @Override
    public boolean hasImportPackage() {
    	return true;
    }
    
    @Override
    public String getImportQualifiedName() {
    	return className;
    }
    
    @Override
    public String getImportPackageName() {
    	return className.indexOf(".") > 0 ? className.substring(0, className.lastIndexOf(".")) : "";
    }
    
    @Override
    public ClientType getAs3Type() {
    	return clientType;
    }
    
    @Override
    public ClientType getClientType() {
    	return clientType;
    }

    private final class ClientImportType implements ClientType {

		@Override
		public boolean hasPackage() {
			return hasImportPackage();
		}

		@Override
		public String getPackageName() {
			return getImportPackageName();
		}

		@Override
		public String getName() {
			return getImportQualifiedName();
		}

		@Override
		public String getQualifiedName() {
			return getImportQualifiedName();
		}

		@Override
		public Object getNullValue() {
			return null;
		}

		@Override
		public Set<String> getImports() {
			return null;
		}
	    
		@Override
	    public void addImports(Set<String> imports) {	    	
	    }

		@Override
		public ClientType toArrayType() {
			return null;
		}

		@Override
		public ClientType translatePackage(PackageTranslator translator) {
			return null;
		}

		@Override
		public ClientType translatePackages(List<PackageTranslator> translators) {
			return null;
		}
    	
    }
}
