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
package org.granite.client.messaging.jmf;

import java.util.List;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.platform.Platform;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class DefaultClientSharedContext extends DefaultSharedContext implements ClientSharedContext {
	
	public DefaultClientSharedContext() {
		this(null, null, null, null);
	}
	
	public DefaultClientSharedContext(CodecRegistry codecRegistry) {
		this(codecRegistry, null, null, null);
	}

	public DefaultClientSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings) {
		this(codecRegistry, defaultStoredStrings, null, null);
	}

	public DefaultClientSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings, Reflection reflection, AliasRegistry aliasRegistry) {
		super(codecRegistry, defaultStoredStrings, (reflection != null ? reflection : Platform.reflection()), aliasRegistry);
	}

	@Override
	public ClientAliasRegistry getAliasRegistry() {
		return (ClientAliasRegistry)super.getAliasRegistry();
	}
}
