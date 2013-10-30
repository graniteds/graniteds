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
