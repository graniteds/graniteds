/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.generator.javafx;

import java.util.HashSet;
import java.util.Set;

import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.PackageTranslator;

/**
 * @author Franck WOLFF
 */
public class JavaFXType implements ClientType {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    public static final JavaFXType BOOLEAN = new JavaFXType(null, "boolean", false);
    public static final JavaFXType INT = new JavaFXType(null, "int", Integer.valueOf(0));
    public static final JavaFXType LONG = new JavaFXType(null, "long", Long.valueOf(0));
    public static final JavaFXType FLOAT = new JavaFXType(null, "float", Float.valueOf(0.0f));
    public static final JavaFXType DOUBLE = new JavaFXType(null, "double", Double.valueOf(0.0));
    public static final JavaFXType STRING = new JavaFXType(null, "String", null);
    
    public static final JavaFXType PAGE_INFO = new JavaFXType("org.granite.tide.data.model", "PageInfo", null);
    public static final JavaFXType SORT_INFO = new JavaFXType("org.granite.tide.data.model", "SortInfo", null);
    
    public static final JavaFXType BOOLEAN_PROPERTY = new JavaFXType(null, "boolean", "javafx.beans.property.BooleanProperty", "javafx.beans.property.SimpleBooleanProperty", Boolean.FALSE);
    public static final JavaFXType INT_PROPERTY = new JavaFXType(null, "int", "javafx.beans.property.IntegerProperty", "javafx.beans.property.SimpleIntegerProperty", Integer.valueOf(0));
    public static final JavaFXType LONG_PROPERTY = new JavaFXType(null, "long", "javafx.beans.property.LongProperty", "javafx.beans.property.SimpleLongProperty", Long.valueOf(0));
    public static final JavaFXType FLOAT_PROPERTY = new JavaFXType(null, "float", "javafx.beans.property.FloatProperty", "javafx.beans.property.SimpleFloatProperty", Float.valueOf(0.0f));
    public static final JavaFXType DOUBLE_PROPERTY = new JavaFXType(null, "double", "javafx.beans.property.DoubleProperty", "javafx.beans.property.SimpleDoubleProperty", Double.valueOf(0.0));
    public static final JavaFXType STRING_PROPERTY = new JavaFXType(null, "String", "javafx.beans.property.StringProperty", "javafx.beans.property.SimpleStringProperty", null);

    private final String packageName;
    private final String name;
    private final String qualifiedName;
    private final String propertyTypeName;
    private final String propertyImplTypeName;
    private final Object nullValue;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public JavaFXType(String packageName, String simpleName) {
        this(packageName, simpleName, null);
    }
    public JavaFXType(String packageName, String name, Object nullValue) {
        this(packageName, name, null, null, nullValue);
    }
    public JavaFXType(String packageName, String name, String propertyTypeName, String propertyImplTypeName, Object nullValue) {
        this.packageName = (packageName != null ? packageName : "");
        this.name = name;
        this.qualifiedName = (hasPackage() ? (packageName + '.' + name) : name);
        this.nullValue = nullValue;
        this.propertyTypeName = propertyTypeName;
        this.propertyImplTypeName = propertyImplTypeName;
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
    
    public String getPropertyTypeName() {
    	return propertyTypeName;
    }
    
    public String getSimplePropertyTypeName() {
    	return propertyTypeName != null && propertyTypeName.indexOf(".") >= 0 
    		? propertyTypeName.substring(propertyTypeName.lastIndexOf(".")+1) : propertyTypeName;
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
    	Set<String> imports = new HashSet<String>();
    	if (hasPackage())
    		imports.add(ungenerify(qualifiedName));
    	if (propertyTypeName != null)
    		imports.add(ungenerify(propertyTypeName));
    	if (propertyImplTypeName != null)
    		imports.add(ungenerify(propertyImplTypeName));
    	return imports;
    }
    
    private String ungenerify(String className) {
		if (className.indexOf("<") >= 0)
			return className.substring(0, className.indexOf("<"));
		return className;
    }
    
    @Override
	public JavaFXType toArrayType() {
    	return new JavaFXType(packageName, name + "[]", null);
    }
    
    @Override
	public JavaFXType translatePackage(PackageTranslator translator) {
    	return new JavaFXType(translator.translate(packageName), getName(), getPropertyTypeName(), getPropertyImplTypeName(), getNullValue());
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
        if (!(obj instanceof JavaFXType))
            return false;
        return qualifiedName.equals(((JavaFXType)obj).qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
