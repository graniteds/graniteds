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

import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.as3.reflect.JavaTypeFactory;
import org.granite.generator.as3.reflect.JavaValidatableEntityBean;


/**
 * 	Entity factory for converting Bean Validation / JSR-303 annotations to Flex
 */
public class BVEntityFactory extends DefaultEntityFactory {
	
	private static final List<String> SPECIAL_ANNOTATIONS = Collections.singletonList("javax.validation.Valid");

	@SuppressWarnings("unchecked")
	@Override
	public JavaType newEntity(JavaTypeFactory provider, Class<?> type, URL url) {
		return new JavaValidatableEntityBean(provider, type, url, "javax.validation.Constraint", SPECIAL_ANNOTATIONS, Collections.EMPTY_MAP);
	}
}
