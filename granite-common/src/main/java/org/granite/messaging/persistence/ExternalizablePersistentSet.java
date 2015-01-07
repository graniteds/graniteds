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
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class ExternalizablePersistentSet extends AbstractExternalizablePersistentCollection {

    private static final long serialVersionUID = 1L;

    public ExternalizablePersistentSet() {
    }
    
	public ExternalizablePersistentSet(Set<?> content, boolean initialized, boolean dirty) {
		super(null, initialized, dirty);
		setContentFromSet(content);
	}
    
	public ExternalizablePersistentSet(Object[] content, boolean initialized, boolean dirty) {
		super(content, initialized, dirty);
	}

	public Set<?> getContentAsSet(Type target) {
		return getContentAsSet(target, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<?> getContentAsSet(Type target, Comparator comparator) {
    	Set set = null;
    	if (content != null) {
        	if (SortedSet.class.isAssignableFrom(TypeUtil.classOfType(target))) {
        		if (comparator != null)
        			set = new TreeSet(comparator);
        		else
        			set = new TreeSet();
        	}
        	else
        		set = new HashSet(content.length);

            ConvertersConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
            Converters converters = config.getConverters();
			Type[] typeArguments = null;
			if (target instanceof ParameterizedType)
				typeArguments = ((ParameterizedType)target).getActualTypeArguments();
			
        	for (Object o : content) {
        		if (typeArguments != null)
        			set.add(converters.convert(o, typeArguments[0]));
        		else
        			set.add(o);
        	}
    	}
		return set;
	}
	
	public void setContentFromSet(Set<?> set) {
		content = (set != null ? set.toArray() : null);
	}
}
