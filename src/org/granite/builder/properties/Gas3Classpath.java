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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Franck WOLFF
 */
@XStreamAlias(value="classpath")
public class Gas3Classpath implements Validable {
	
	@XStreamAsAttribute
	private String path;

	public Gas3Classpath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void validate(ValidationResults results) {
		if (path == null)
			results.getErrors().add("classpath: path cannot be null");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Gas3Classpath))
			return false;
		Gas3Classpath g3c = (Gas3Classpath)obj;
		return (path == null ? g3c.path == null : path.equals(g3c.path));
	}

	@Override
	public int hashCode() {
		return (path == null ? 0 : path.hashCode());
	}
}
