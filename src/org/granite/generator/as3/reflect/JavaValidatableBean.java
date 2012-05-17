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

package org.granite.generator.as3.reflect;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity reflection for converting validation annotations to Flex.
 * 
 * @author William DRAI
 * @author Franck WOLFF
 */
public class JavaValidatableBean extends JavaBean {

	private Map<JavaProperty, List<JavaConstraint>> constraints = new HashMap<JavaProperty, List<JavaConstraint>>();

    public JavaValidatableBean(JavaTypeFactory provider, Class<?> type, URL url, String metaAnnotationName, List<String> specialAnnotationNames, Map<String, String> nameConversions) {
        super(provider, type, url);
        
        ValidatableBean validatableBean = new ValidatableBean(type, metaAnnotationName, specialAnnotationNames, nameConversions);
        validatableBean.buildConstraints(properties, constraints);
    }
    
    public Map<JavaProperty, List<JavaConstraint>> getConstraints() {
    	return constraints;
    }
}
