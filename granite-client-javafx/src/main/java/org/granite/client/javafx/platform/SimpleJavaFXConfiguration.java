/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
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
