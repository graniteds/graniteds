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
package org.granite.client.javafx.platform;

import org.granite.client.configuration.ClientGraniteConfig;
import org.granite.client.configuration.SimpleConfiguration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;

/**
 * @author Franck WOLFF
 */
public class SimpleJavaFXConfiguration extends SimpleConfiguration {

	public SimpleJavaFXConfiguration() {
		super("org/granite/client/javafx/platform/granite-config-javafx.xml", null);
	}

	@Override
	public void postLoad(ClientGraniteConfig graniteConfig) {
		ClientAliasRegistry aliasRegistry = new ClientAliasRegistry();
		aliasRegistry.registerAlias(PersistentSet.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentSet");
		aliasRegistry.registerAlias(PersistentBag.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentBag");
		aliasRegistry.registerAlias(PersistentList.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentList");
		aliasRegistry.registerAlias(PersistentMap.class.getName(), "org.granite.messaging.persistence.ExternalizablePersistentMap");
		aliasRegistry.registerAlias("org.granite.client.validation.InvalidValue", "org.granite.tide.validators.InvalidValue");
		graniteConfig.setAliasRegistry(aliasRegistry);
	}
}
