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
package org.granite.client.messaging.jmf;

import java.util.List;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.platform.Platform;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.reflect.Reflection;

/**
 * Default implementation for a client shared context
 *
 * @author Franck WOLFF
 */
public class DefaultClientSharedContext extends DefaultSharedContext implements ClientSharedContext {

    /**
     * Create a default shared context
     */
	public DefaultClientSharedContext() {
		this(null, null, null, null);
	}

    /**
     * Create a shared context with the specified codec registry
     * @param codecRegistry codec registry
     */
	public DefaultClientSharedContext(CodecRegistry codecRegistry) {
		this(codecRegistry, null, null, null);
	}

    /**
     * Create a shared context with the specified codec registry and stored strings
     * @param codecRegistry codec registry
     * @param defaultStoredStrings stored strings
     */
	public DefaultClientSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings) {
		this(codecRegistry, defaultStoredStrings, null, null);
	}

    /**
     * Create a shared context with the specified parameters
     * @param codecRegistry codec registry
     * @param defaultStoredStrings stored strings
     * @param reflection reflection provider
     * @param aliasRegistry client alias registry
     */
	public DefaultClientSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings, Reflection reflection, AliasRegistry aliasRegistry) {
		super(codecRegistry, defaultStoredStrings, (reflection != null ? reflection : Platform.reflection()), aliasRegistry);
	}

	@Override
	public ClientAliasRegistry getAliasRegistry() {
		return (ClientAliasRegistry)super.getAliasRegistry();
	}
}
