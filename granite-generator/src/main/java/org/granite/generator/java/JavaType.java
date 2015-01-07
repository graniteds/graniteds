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
package org.granite.generator.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PackageTranslator;

/**
 * @author Franck WOLFF
 */
public class JavaType implements ClientType {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    public static final JavaType BOOLEAN = new JavaType(null, "boolean", false);
    public static final JavaType INT = new JavaType(null, "int", Integer.valueOf(0));
    public static final JavaType LONG = new JavaType(null, "long", Long.valueOf(0));
    public static final JavaType FLOAT = new JavaType(null, "float", Float.valueOf(0.0f));
    public static final JavaType DOUBLE = new JavaType(null, "double", Double.valueOf(0.0));
    public static final JavaType STRING = new JavaType(null, "String", null);
    
    public static final JavaType PAGE_INFO = new JavaType("org.granite.tide.data.model", "PageInfo", null);
    public static final JavaType SORT_INFO = new JavaType("org.granite.tide.data.model", "SortInfo", null);
    
    public static final JavaType LAZY = new JavaType("org.granite.client.persistence", "Lazy", null);
    
    private final String packageName;
    private final String name;
    private final String qualifiedName;
    private final String propertyImplTypeName;
    private final Object nullValue;
    private final Set<String> imports = new HashSet<String>(); 

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public JavaType(String packageName, String simpleName) {
        this(packageName, simpleName, null, null);
    }
    public JavaType(String packageName, String name, Object nullValue) {
        this(packageName, name, null, nullValue);
    }
    public JavaType(String packageName, String name, String propertyImplTypeName, Object nullValue) {
        this.packageName = (packageName != null ? packageName : "");
        this.name = name;
        this.qualifiedName = (hasPackage() ? (packageName + '.' + name) : name);
        this.nullValue = nullValue;
        this.propertyImplTypeName = propertyImplTypeName;
    	if (hasPackage())
    		imports.add(ungenerify(qualifiedName));
        if (propertyImplTypeName != null)
            imports.add(ungenerify(propertyImplTypeName));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    @Override
	public boolean hasPackage() {
        return packageName.length() > 0;
    }

    @Override
	public String getPackageName() {
        return packageName;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public String getQualifiedName() {
        return qualifiedName;
    }
    
    public String getPropertyImplTypeName() {
        return propertyImplTypeName;
    }
    
    public String getSimplePropertyImplTypeName() {
        return propertyImplTypeName != null && propertyImplTypeName.indexOf(".") >= 0 
            ? propertyImplTypeName.substring(propertyImplTypeName.lastIndexOf(".")+1) : propertyImplTypeName;
    }

    @Override
	public Object getNullValue() {
        return nullValue;
    }

    public boolean isNumber() {
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods
    
    @Override
	public Set<String> getImports() {
    	return imports;
    }
    
    @Override
	public void addImports(Set<String> classNames) {
    	for (String className : classNames) {
    		if (className.indexOf(".") < 0 || className.startsWith("java.lang"))
    			continue;
    		imports.add(ungenerify(className));
    	}
    }
    
    private String ungenerify(String className) {
		if (className.indexOf("<") >= 0)
			return className.substring(0, className.indexOf("<"));
		return className;
    }
    
    @Override
	public JavaType toArrayType() {
    	return new JavaType(packageName, name + "[]", null);
    }
    
    @Override
	public JavaType translatePackage(PackageTranslator translator) {
    	return new JavaType(translator.translate(packageName), getName(), getPropertyImplTypeName(), getNullValue());
    }
    
    @Override
    public JavaType translatePackages(List<PackageTranslator> translators) {
    	boolean translate = false;
    	
        PackageTranslator translator = PackageTranslator.forPackage(translators, packageName);
        String translatedPackageName = packageName;
        if (translator != null) {
        	translate = true;
        	translatedPackageName = translator.translate(packageName);
        }
        
    	Set<String> translatedImports = new HashSet<String>();
    	for (String imp : imports) {
    		translator = PackageTranslator.forPackage(translators, imp);
    		if (translator != null) {
    			translate = true;
        		translatedImports.add(translator.translate(imp));
    		}
    		else
        		translatedImports.add(imp);
    	}
    	
    	if (!translate)
    		return this;
    	
    	JavaType translatedType = new JavaType(translatedPackageName, getName(), getPropertyImplTypeName(), getNullValue());
		translatedType.addImports(translatedImports);
    	return translatedType;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

    @Override
    public int hashCode() {
        return qualifiedName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof JavaType))
            return false;
        return qualifiedName.equals(((JavaType)obj).qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
