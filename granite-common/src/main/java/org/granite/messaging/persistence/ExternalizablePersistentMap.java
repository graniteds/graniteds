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
package org.granite.messaging.persistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class ExternalizablePersistentMap extends AbstractExternalizablePersistentCollection {

	private static final long serialVersionUID = 1L;
	
	public ExternalizablePersistentMap() {
	}

	public ExternalizablePersistentMap(Map<?, ?> content, boolean initialized, boolean dirty) {
		super(null, initialized, dirty);
		setContentFromMap(content);
	}

	public ExternalizablePersistentMap(Object[] content, boolean initialized, boolean dirty) {
		super(content, initialized, dirty);
	}
	
	public Map<?, ?> getContentAsMap(Type target) {
		return getContentAsMap(target, null);
	}
		
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<?, ?> getContentAsMap(Type target, Comparator comparator) {
		Map map = null;
		if (content != null) {
			if (SortedMap.class.isAssignableFrom(TypeUtil.classOfType(target))) {
				if (comparator != null)
					map = new TreeMap(comparator);
				else
					map = new TreeMap();
			}
			else
				map = new HashMap(content.length);
			
			ConvertersConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
            Converters converters = config.getConverters();
			Type[] typeArguments = null;
			if (target instanceof ParameterizedType)
				typeArguments = ((ParameterizedType)target).getActualTypeArguments();
			
			for (int i = 0; i < content.length; i++) {
	            Object[] entry = (Object[])content[i];
	            
	            if (typeArguments != null)
	            	map.put(converters.convert(entry[0], typeArguments[0]), converters.convert(entry[1], typeArguments[1]));
	            else
	            	map.put(entry[0], entry[1]);
	        }
		}
        return map;
	}
	
	public void setContentFromMap(Map<?, ?> map) {
		if (map == null)
			content = null;
		else {
	        content = new Object[map.size()];
	        int index = 0;
	        for (Map.Entry<?, ?> entry : map.entrySet())
	            content[index++] = new Object[]{entry.getKey(), entry.getValue()};
		}
	}
}
