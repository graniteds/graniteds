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

package org.granite.builder.properties;

import org.granite.generator.as3.PackageTranslator;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Franck WOLFF
 */
@XStreamAlias(value="translator")
public class Gas3Translator implements Validable {

	@XStreamAsAttribute
	private String java;
	
	@XStreamAsAttribute
	private String as3;
	
	private transient PackageTranslator packageTranslator = null;
	
	public Gas3Translator(String java, String as3) {
		this.java = java;
		this.as3 = as3;
	}

	public String getJava() {
		return java;
	}

	public void setJava(String java) {
		this.java = java;
	}

	public String getAs3() {
		return as3;
	}

	public void setAs3(String as3) {
		this.as3 = as3;
	}

	@Override
	public void validate(ValidationResults results) {
		if (java == null || as3 == null)
			results.getErrors().add("translator: java and as3 cannot be null");
	}
	
	public PackageTranslator getPackageTranslator() {
		if (packageTranslator == null)
			packageTranslator = new PackageTranslator(java, as3);
		return packageTranslator;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Gas3Translator))
			return false;
		Gas3Translator g3t = (Gas3Translator)obj;
		return (java == null ? g3t.java == null : java.equals(g3t.java));
	}

	@Override
	public int hashCode() {
		return java != null ? java.hashCode() : 0;
	}
}
