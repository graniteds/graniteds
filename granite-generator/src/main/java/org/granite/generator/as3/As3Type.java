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

package org.granite.generator.as3;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Franck WOLFF
 */
public class As3Type implements ClientType {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    public static final As3Type INT = new As3Type(null, "int", Integer.valueOf(0));
    public static final As3Type UINT = new As3Type(null, "uint", Integer.valueOf(0));
    public static final As3Type BOOLEAN = new As3Type(null, "Boolean", Boolean.valueOf(false));
    public static final As3Type NUMBER = new As3Type(null, "Number", "Number.NaN");
    public static final As3Type LONG = new As3Type("org.granite.math", "Long");
    public static final As3Type BIG_INTEGER = new As3Type("org.granite.math", "BigInteger");
    public static final As3Type BIG_DECIMAL = new As3Type("org.granite.math", "BigDecimal");
    public static final As3Type MATH_CONTEXT = new As3Type("org.granite.math", "MathContext");
    public static final As3Type ROUNDING_MODE = new As3Type("org.granite.math", "RoundingMode");
    public static final As3Type OBJECT = new As3Type(null, "Object");
    public static final As3Type STRING = new As3Type(null, "String");
    public static final As3Type ARRAY = new As3Type(null, "Array");
    public static final As3Type DATE = new As3Type(null, "Date");
    public static final As3Type XML = new As3Type(null, "XML");
    public static final As3Type BYTE_ARRAY = new As3Type("flash.utils", "ByteArray");
    public static final As3Type DICTIONARY = new As3Type("flash.utils", "Dictionary");

    public static final As3Type LIST_COLLECTION_VIEW = new As3Type("mx.collections", "ListCollectionView");
    public static final As3Type ARRAY_COLLECTION = new As3Type("mx.collections", "ArrayCollection");
    public static final As3Type ILIST = new As3Type("mx.collections", "IList");
    public static final As3Type IMAP = new As3Type("org.granite.collections", "IMap");
    public static final As3Type ENUM = new As3Type("org.granite.util", "Enum");

    public static final As3Type PAGE = new As3Type("org.granite.tide.data.model", "Page");
    public static final As3Type PAGE_INFO = new As3Type("org.granite.tide.data.model", "PageInfo");
    public static final As3Type SORT_INFO = new As3Type("org.granite.tide.data.model", "SortInfo");

    private final String packageName;
    private final String name;
    private final String qualifiedName;
    private final Object nullValue;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public As3Type(String packageName, String simpleName) {
        this(packageName, simpleName, null);
    }
    public As3Type(String packageName, String name, Object nullValue) {
        this.packageName = (packageName != null ? packageName : "");
        this.name = name;
        this.qualifiedName = (hasPackage() ? (packageName + '.' + name) : name);
        this.nullValue = nullValue;
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

    @Override
	public Object getNullValue() {
        return nullValue;
    }

    public boolean isNumber() {
        return NUMBER.equals(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods
    
    @Override
	public Set<String> getImports() {
    	if (hasPackage())
    		return Collections.singleton(qualifiedName);
    	
    	return Collections.emptySet();
    }
    
    @Override
    public void addImports(Set<String> imports) {    	
    }
    
    @Override
	public As3Type toArrayType() {
    	return ARRAY;
    }
    
    @Override
	public As3Type translatePackage(PackageTranslator translator) {
    	return new As3Type(translator.translate(packageName), getName());
    }
    
    @Override
    public As3Type translatePackages(List<PackageTranslator> translators) {
    	boolean translate = false;
    	
        PackageTranslator translator = PackageTranslator.forPackage(translators, packageName);
        String translatedPackageName = packageName;
        if (translator != null) {
        	translate = true;
        	translatedPackageName = translator.translate(packageName);
        }
    	
    	if (!translate)
    		return this;
    	
    	return new As3Type(translatedPackageName, getName());
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
        if (!(obj instanceof As3Type))
            return false;
        return qualifiedName.equals(((As3Type)obj).qualifiedName);
    }

    @Override
    public String toString() {
        return qualifiedName;
    }
}
