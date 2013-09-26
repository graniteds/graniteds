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

package org.granite.generator.java.template;

/**
 * @author Franck WOLFF
 */
public interface JavaTemplateUris {
	
	public static final String BEAN = "class:org/granite/generator/java/template/bean.gsp";
	public static final String BEAN_BASE = "class:org/granite/generator/java/template/beanBase.gsp";
	public static final String TIDE_BEAN_BASE = "class:org/granite/generator/java/template/beanBase.gsp";
	
	public static final String ENTITY = "class:org/granite/generator/java/template/entity.gsp";
	public static final String ENTITY_BASE = "class:org/granite/generator/java/template/entityBase.gsp";
	public static final String TIDE_ENTITY_BASE = "class:org/granite/generator/java/template/entityBase.gsp";
	
	public static final String INTERFACE = "class:org/granite/generator/java/template/interface.gsp";
	
	public static final String ENUM = "class:org/granite/generator/java/template/enum.gsp";
	
	public static final String REMOTE = "class:org/granite/generator/java/template/remote.gsp";
	public static final String REMOTE_BASE = "class:org/granite/generator/java/template/remoteBase.gsp";
	public static final String TIDE_REMOTE = "class:org/granite/generator/java/template/tideRemote.gsp";
	public static final String TIDE_REMOTE_BASE = "class:org/granite/generator/java/template/tideRemoteBase.gsp";
}
