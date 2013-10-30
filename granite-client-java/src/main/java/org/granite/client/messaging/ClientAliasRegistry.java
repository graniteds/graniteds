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
package org.granite.client.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.granite.client.platform.Platform;
import org.granite.logging.Logger;
import org.granite.messaging.AliasRegistry;


/**
 * Client-side implementation of AliasRegistry that scan specified packaged to find classes
 * annotated with {@link org.granite.client.messaging.RemoteAlias}
 *
 * @author William DRAI
 */
public class ClientAliasRegistry implements AliasRegistry {
	
	private static final Logger log = Logger.getLogger(ClientAliasRegistry.class);
	
	private Map<String, String> serverToClientAliases = new HashMap<String, String>();
	private Map<String, String> clientToServerAliases = new HashMap<String, String>();
	
	public void scan(Set<String> packageNames) {
		if (packageNames != null && !packageNames.isEmpty()) {
			RemoteAliasScanner scanner = Platform.getInstance().newRemoteAliasScanner();
			
			Set<Class<?>> aliases = scanner.scan(packageNames);
			for (Class<?> alias : aliases)
				registerAlias(alias);
			
			log.debug("Using remote aliases: %s", aliases);
		}
	}
	
	public void registerAlias(Class<?> remoteAliasAnnotatedClass) {
		RemoteAlias remoteAlias = remoteAliasAnnotatedClass.getAnnotation(RemoteAlias.class);
		if (remoteAlias == null)
			throw new IllegalArgumentException(remoteAliasAnnotatedClass.getName() + " isn't annotated with " + RemoteAlias.class.getName());
		registerAlias(remoteAliasAnnotatedClass.getName(), remoteAlias.value());
	}

	public void registerAliases(Class<?>... remoteAliasAnnotatedClasses) {
		for (Class<?> remoteAliasAnnotatedClass : remoteAliasAnnotatedClasses)
			registerAlias(remoteAliasAnnotatedClass);
	}

	public void registerAlias(String clientClassName, String serverClassName) {
		if (clientClassName.length() == 0 || serverClassName.length() == 0)
			throw new IllegalArgumentException("Empty class name: " + clientClassName + " / " + serverClassName);
		
		clientToServerAliases.put(clientClassName, serverClassName);
		serverToClientAliases.put(serverClassName, clientClassName);
	}

	public void registerAliases(Map<String, String> clientToServerAliases) {
		for (Map.Entry<String, String> clientToServerAlias : clientToServerAliases.entrySet())
			registerAlias(clientToServerAlias.getKey(), clientToServerAlias.getValue());
	}

	public String getAliasForType(String className) {
		String alias = clientToServerAliases.get(className);
		return (alias != null ? alias : className);
	}
	
	public String getTypeForAlias(String alias) {
		String className = serverToClientAliases.get(alias);
		return className != null ? className : alias;
	}
}
