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
package org.granite.generator.javafx;

import java.util.HashSet;
import java.util.List;
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
    
    public static final JavaFXType LOCALDATE = new JavaFXType("java.time", "LocalDate", null);
    public static final JavaFXType LOCALDATETIME = new JavaFXType("java.time", "LocalDateTime", null);
    public static final JavaFXType LOCALTIME = new JavaFXType("java.time", "LocalTime", null);
    
    public static final JavaFXType PAGE_INFO = new JavaFXType("org.granite.tide.data.model", "PageInfo", null);
    public static final JavaFXType SORT_INFO = new JavaFXType("org.granite.tide.data.model", "SortInfo", null);
    
    public static final JavaFXType LAZY = new JavaFXType("org.granite.client.persistence", "Lazy", null);
    
    public static final JavaFXType BOOLEAN_PROPERTY = new JavaFXType(null, "boolean", "javafx.beans.property.BooleanProperty", "javafx.beans.property.SimpleBooleanProperty", Boolean.FALSE);
    public static final JavaFXType INT_PROPERTY = new JavaFXType(null, "int", "javafx.beans.property.IntegerProperty", "javafx.beans.property.SimpleIntegerProperty", Integer.valueOf(0));
    public static final JavaFXType LONG_PROPERTY = new JavaFXType(null, "long", "javafx.beans.property.LongProperty", "javafx.beans.property.SimpleLongProperty", Long.valueOf(0));
    public static final JavaFXType FLOAT_PROPERTY = new JavaFXType(null, "float", "javafx.beans.property.FloatProperty", "javafx.beans.property.SimpleFloatProperty", Float.valueOf(0.0f));
    public static final JavaFXType DOUBLE_PROPERTY = new JavaFXType(null, "double", "javafx.beans.property.DoubleProperty", "javafx.beans.property.SimpleDoubleProperty", Double.valueOf(0.0));
    public static final JavaFXType STRING_PROPERTY = new JavaFXType(null, "String", "javafx.beans.property.StringProperty", "javafx.beans.property.SimpleStringProperty", null);

    public static final JavaFXType LOCALDATE_PROPERTY = new JavaFXType("java.time", "LocalDate", "javafx.beans.property.ObjectProperty<LocalDate>", "javafx.beans.property.SimpleObjectProperty<LocalDate>", null);
    public static final JavaFXType LOCALDATETIME_PROPERTY = new JavaFXType("java.time", "LocalDateTime", "javafx.beans.property.ObjectProperty<LocalDateTime>", "javafx.beans.property.SimpleObjectProperty<LocalDateTime>", null);
    public static final JavaFXType LOCALTIME_PROPERTY = new JavaFXType("java.time", "LocalTime", "javafx.beans.property.ObjectProperty<LocalTime>", "javafx.beans.property.SimpleObjectProperty<LocalTime>", null);
    
    public static final JavaFXType BOOLEAN_READONLY_PROPERTY = new JavaFXType(null, "boolean", "javafx.beans.property.ReadOnlyBooleanProperty", "javafx.beans.property.ReadOnlyBooleanWrapper", null, Boolean.FALSE, true);
    public static final JavaFXType INT_READONLY_PROPERTY = new JavaFXType(null, "int", "javafx.beans.property.ReadOnlyIntegerProperty", "javafx.beans.property.ReadOnlyIntegerWrapper", null, Integer.valueOf(0), true);
    public static final JavaFXType LONG_READONLY_PROPERTY = new JavaFXType(null, "long", "javafx.beans.property.ReadOnlyLongProperty", "javafx.beans.property.ReadOnlyLongWrapper", null, Long.valueOf(0), true);
    public static final JavaFXType FLOAT_READONLY_PROPERTY = new JavaFXType(null, "float", "javafx.beans.property.ReadOnlyFloatProperty", "javafx.beans.property.ReadOnlyFloatWrapper", null, Float.valueOf(0.0f), true);
    public static final JavaFXType DOUBLE_READONLY_PROPERTY = new JavaFXType(null, "double", "javafx.beans.property.ReadOnlyDoubleProperty", "javafx.beans.property.ReadOnlyDoubleWrapper", null, Double.valueOf(0.0), true);
    public static final JavaFXType STRING_READONLY_PROPERTY = new JavaFXType(null, "String", "javafx.beans.property.ReadOnlyStringProperty", "javafx.beans.property.ReadOnlyStringWrapper", null, null, true);

    public static final JavaFXType LOCALDATE_READONLY_PROPERTY = new JavaFXType("java.time", "LocalDate", "javafx.beans.property.ReadOnlyObjectProperty<LocalDate>", "javafx.beans.property.ReadOnlyObjectWrapper<LocalDate>", null);
    public static final JavaFXType LOCALDATETIME_READONLY_PROPERTY = new JavaFXType("java.time", "LocalDateTime", "javafx.beans.property.ReadOnlyObjectProperty<LocalDateTime>", "javafx.beans.property.ReadOnlyObjectWrapper<LocalDateTime>", null);
    public static final JavaFXType LOCALTIME_READONLY_PROPERTY = new JavaFXType("java.time", "LocalTime", "javafx.beans.property.ReadOnlyObjectProperty<LocalTime>", "javafx.beans.property.ReadOnlyObjectWrapper<LocalTime>", null);
    
    private final String packageName;
    private final String name;
    private final String qualifiedName;
    private final String propertyTypeName;
    private final String propertyImplTypeName;
    private final String propertyFactoryName;
    private final Object nullValue;
    private final boolean readOnly;
    private final Set<String> imports = new HashSet<String>(); 

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public JavaFXType(String packageName, String simpleName) {
        this(packageName, simpleName, null);
    }
    public JavaFXType(String packageName, String name, Object nullValue) {
        this(packageName, name, null, null, nullValue);
    }
    public JavaFXType(String packageName, String name, String propertyTypeName, String propertyImplTypeName, Object nullValue) {
    	this(packageName, name, propertyTypeName, propertyImplTypeName, null, nullValue, false);
    }
    public JavaFXType(String packageName, String name, String propertyTypeName, String propertyImplTypeName, String propertyFactoryName, Object nullValue, boolean readOnly) {
        this.packageName = (packageName != null ? packageName : "");
        this.name = name;
        this.qualifiedName = (hasPackage() ? (packageName + '.' + name) : name);
        this.nullValue = nullValue;
        this.propertyTypeName = propertyTypeName;
        this.propertyImplTypeName = propertyImplTypeName;
        this.propertyFactoryName = propertyFactoryName;
        this.readOnly = readOnly;
    	if (hasPackage())
    		imports.add(ungenerify(qualifiedName));
    	if (propertyTypeName != null)
    		imports.add(ungenerify(propertyTypeName));
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
    
    public String getPropertyFactoryName() {
    	return propertyFactoryName != null ? propertyFactoryName : "new " + getSimplePropertyImplTypeName();
    }

    @Override
	public Object getNullValue() {
        return nullValue;
    }
    
    public boolean isReadOnly() {
    	return readOnly;
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
	public JavaFXType toArrayType() {
    	return new JavaFXType(packageName, name + "[]", null);
    }
    
    @Override
	public JavaFXType translatePackage(PackageTranslator translator) {
    	return new JavaFXType(translator.translate(packageName), getName(), getPropertyTypeName(), getPropertyImplTypeName(), getPropertyFactoryName(), getNullValue(), isReadOnly());
    }
    
    @Override
    public JavaFXType translatePackages(List<PackageTranslator> translators) {
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
    	
    	JavaFXType translatedType = new JavaFXType(translatedPackageName, getName(), getPropertyTypeName(), getPropertyImplTypeName(), getPropertyFactoryName(), getNullValue(), isReadOnly());
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
        if (!(obj instanceof JavaFXType))
            return false;
        return qualifiedName.equals(((JavaFXType)obj).qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
