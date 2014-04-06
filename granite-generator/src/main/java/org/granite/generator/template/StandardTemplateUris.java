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
package org.granite.generator.template;

/**
 * @author Franck WOLFF
 */
public interface StandardTemplateUris {

	public static final String BEAN = "class:org/granite/generator/template/bean.gsp";
	public static final String BEAN_BASE = "class:org/granite/generator/template/beanBase.gsp";
	public static final String TIDE_BEAN_BASE = "class:org/granite/generator/template/tideBeanBase.gsp";
	public static final String LCDS_BEAN_BASE = "class:org/granite/generator/template/lcdsBeanBase.gsp";

	public static final String ENTITY = "class:org/granite/generator/template/entity.gsp";
	public static final String ENTITY_BASE = "class:org/granite/generator/template/entityBase.gsp";
	public static final String TIDE_ENTITY_BASE = "class:org/granite/generator/template/tideEntityBase.gsp";

	public static final String INTERFACE = "class:org/granite/generator/template/interface.gsp";

	public static final String ENUM = "class:org/granite/generator/template/enum.gsp";
	
	public static final String REMOTE = "class:org/granite/generator/template/remote.gsp";
	public static final String REMOTE_BASE = "class:org/granite/generator/template/remoteBase.gsp";
	public static final String TIDE_REMOTE = "class:org/granite/generator/template/remote.gsp";
	public static final String TIDE_REMOTE_BASE = "class:org/granite/generator/template/tideRemoteBase.gsp";
}
