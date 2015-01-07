/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.messaging.jmf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.messaging.AliasRegistry;
import org.granite.messaging.DefaultAliasRegistry;
import org.granite.messaging.jmf.codec.std.impl.util.ClassNameUtil;
import org.granite.messaging.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class DefaultSharedContext implements SharedContext {

	protected static List<String> JAVA_DEFAULT_CLASS_NAMES = Arrays.asList(
		
		Boolean.class.getName(),
		Character.class.getName(),
		Byte.class.getName(),
		Short.class.getName(),
		Integer.class.getName(),
		Long.class.getName(),
		Float.class.getName(),
		Double.class.getName(),
		
		String.class.getName(),
		Object.class.getName(),

		Date.class.getName(),
		
		List.class.getName(),
		ArrayList.class.getName(),
		
		Set.class.getName(),
		HashSet.class.getName(),
		
		Map.class.getName(),
		HashMap.class.getName(),
		
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentList",
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentMap",
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentSet",
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentBag",
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentSortedSet",
		JMFConstants.CLIENT_PERSISTENCE_COLLECTION_PACKAGE + ".PersistentSortedMap"
	);
	
	protected final CodecRegistry codecRegistry;
	protected final Reflection reflection;
	protected final List<String> initialClassNameDictionary;
	protected final AliasRegistry aliasRegistry;
	
	public DefaultSharedContext() {
		this(null, null, null, null);
	}
	
	public DefaultSharedContext(CodecRegistry codecRegistry) {
		this(codecRegistry, null, null, null);
	}

	public DefaultSharedContext(CodecRegistry codecRegistry, List<String> defaultClassNames) {
		this(codecRegistry, defaultClassNames, null, null);
	}
	
	public DefaultSharedContext(CodecRegistry codecRegistry, List<String> defaultClassNames, Reflection reflection, AliasRegistry aliasRegistry) {
		this.codecRegistry = (codecRegistry != null ? codecRegistry : new DefaultCodecRegistry());
		
		List<String> initialClassNameDictionary = new ArrayList<String>();
		for (String className : JAVA_DEFAULT_CLASS_NAMES)
			ClassNameUtil.initClassNameDictionary(initialClassNameDictionary, className);
		if (defaultClassNames != null) {
			for (String className : defaultClassNames)
				ClassNameUtil.initClassNameDictionary(initialClassNameDictionary, className);
		}
		this.initialClassNameDictionary = Collections.unmodifiableList(initialClassNameDictionary);
		
		this.reflection = (reflection != null ? reflection : new Reflection(null));
		
		this.aliasRegistry = aliasRegistry != null ? aliasRegistry : new DefaultAliasRegistry();
	}

	public CodecRegistry getCodecRegistry() {
		return codecRegistry;
	}

	public Reflection getReflection() {
		return reflection;
	}

	@Override
	public List<String> getInitialClassNameDictionary() {
		return initialClassNameDictionary;
	}
	
	public AliasRegistry getAliasRegistry() {
		return aliasRegistry;
	}
	
	public String getRemoteAlias(String className) {
		return aliasRegistry.getAliasForType(className);
	}

	public String getClassName(String remoteAlias) {
		return aliasRegistry.getTypeForAlias(remoteAlias);
	}
}
