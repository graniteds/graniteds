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

package org.granite.generator.as3;

import java.util.List;


/**
 * @author Franck WOLFF
 */
public class PackageTranslator {

    private String java = null;
    private String as3 = null;
    private int weight = -1;

    public PackageTranslator() {
    }

    public PackageTranslator(String java, String as3) {
        setJava(java);
        setAs3(as3);
    }

    public String getJava() {
        return java;
    }
    public void setJava(String java) {
        this.java = java;
        this.weight = (java != null ? java.split("\\Q.\\E", -1).length : -1);
    }

    public String getAs3() {
        return as3;
    }
    public void setAs3(String as3) {
        this.as3 = as3;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isValid() {
        return java != null && java.length() > 0 && as3 != null && as3.length() > 0;
    }

    /* (non-Javadoc)
	 * @see org.granite.generator.as3.PackageTranslator#match(java.lang.String)
	 */
    public int match(String pkg) {
        if (!pkg.startsWith(java))
            return 0;
        if (pkg.equals(java))
            return Integer.MAX_VALUE;
        return weight;
    }

    /* (non-Javadoc)
	 * @see org.granite.generator.as3.PackageTranslator#translate(java.lang.String)
	 */
    public String translate(String pkg) {
        return as3 + pkg.substring(java.length());
    }
    
    public static PackageTranslator forPackage(List<PackageTranslator> translators, String packageName) {
        PackageTranslator translator = null;

        int weight = 0;
        for (PackageTranslator t : translators) {
            int w = t.match(packageName);
            if (w > weight) {
                weight = w;
                translator = t;
            }
        }
		
        return translator;
    }

    @Override
    public int hashCode() {
        return (java != null ? java.hashCode() : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PackageTranslator))
            return false;
        return (
            java == null ?
            ((PackageTranslator)o).java == null :
            java.equals(((PackageTranslator)o).java)
        );
    }

    @Override
    public String toString() {
        return getClass().getName() + "{java=\"" + java + "\", as=\"" + as3 + "\"}";
    }
}
