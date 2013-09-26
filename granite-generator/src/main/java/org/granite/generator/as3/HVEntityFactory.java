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

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.as3.reflect.JavaTypeFactory;
import org.granite.generator.as3.reflect.JavaValidatableBean;
import org.granite.generator.as3.reflect.JavaValidatableEntityBean;


/**
 * 	Entity factory for converting Hibernate Validator 3.x annotations to Flex
 */
public class HVEntityFactory extends DefaultEntityFactory {
	
	private static final List<String> SPECIAL_ANNOTATIONS = Collections.singletonList("org.hibernate.validator.Valid");
	
	private static final Map<String, String> NAME_CONVERSIONS = new HashMap<String, String>();
	static {
		NAME_CONVERSIONS.put("org.hibernate.validator.Length", "javax.validation.constraints.Size");
		NAME_CONVERSIONS.put("org.hibernate.validator", "javax.validation.constraints");
	}

	@Override
	public JavaType newBean(JavaTypeFactory provider, Class<?> type, URL url) {
		return new JavaValidatableBean(provider, type, url, "org.hibernate.validator.ValidatorClass", SPECIAL_ANNOTATIONS, NAME_CONVERSIONS);
	}
	
	@Override
	public JavaType newEntity(JavaTypeFactory provider, Class<?> type, URL url) {
		return new JavaValidatableEntityBean(provider, type, url, "org.hibernate.validator.ValidatorClass", SPECIAL_ANNOTATIONS, NAME_CONVERSIONS);
	}
}
