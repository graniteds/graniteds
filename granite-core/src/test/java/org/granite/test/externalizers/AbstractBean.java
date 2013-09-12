/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.externalizers;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.messaging.annotations.Include;

@ExternalizedBean(type=DefaultExternalizer.class)
public abstract class AbstractBean {

	private String prop1;
	private String prop3;

	public String getProp1() {
		return prop1;
	}

	public void setProp1(String prop1) {
		this.prop1 = prop1;
	}
	
	@Include
    public abstract String getProp2();

	public String getProp3() {
		return prop3;
	}

	public void setProp3(String prop3) {
		this.prop3 = prop3;
	}
}
