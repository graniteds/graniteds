/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.jmf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class DefaultSharedContext implements SharedContext {

	private static List<String> JAVA_DEFAULT_STORED_STRINGS = Arrays.asList(
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
		HashMap.class.getName()
	);
	
	
	private final CodecRegistry codecRegistry;
	private final ClassLoader classLoader;
	private final List<String> defaultStoredStrings;
	
	public DefaultSharedContext() {
		this(new DefaultCodecRegistry(), null, null);
	}
	
	public DefaultSharedContext(CodecRegistry codecRegistry) {
		this(codecRegistry, null, null);
	}
	
	public DefaultSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings) {
		this(codecRegistry, defaultStoredStrings, null);
	}
	
	public DefaultSharedContext(CodecRegistry codecRegistry, List<String> defaultStoredStrings, ClassLoader classLoader) {
		this.codecRegistry = codecRegistry;
		
		this.defaultStoredStrings = new ArrayList<String>();
		this.defaultStoredStrings.addAll(JAVA_DEFAULT_STORED_STRINGS);
		if (defaultStoredStrings != null)
			this.defaultStoredStrings.addAll(defaultStoredStrings);

		this.classLoader = classLoader;
	}

	public CodecRegistry getCodecRegistry() {
		return codecRegistry;
	}

	public ClassLoader getClassLoader() {
		return (classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
	}

	public List<String> getDefaultStoredStrings() {
		return defaultStoredStrings;
	}
}
