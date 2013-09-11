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

package org.granite.seam21;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.util.TypeUtil;
import org.jboss.seam.core.ConversationEntry;

/**
 * @author William DRAI
 */
public class ConversationEntryConverter extends Converter implements Reverter {
    
    public ConversationEntryConverter(Converters converters) {
        super(converters);
    }

    @Override
    protected boolean internalCanConvert(Object value, Type targetType) {
        Class<?> targetClass = TypeUtil.classOfType(targetType);
        return (
            targetClass != Object.class && targetClass.isAssignableFrom(ConversationEntry.class) &&
            (value instanceof Map<?, ?> || value == null)
        );
    }

    @Override
    protected Object internalConvert(Object value, Type targetType) {
    	throw new RuntimeException("ConversationEntry cannot be deserialized");
    }

    public boolean canRevert(Object value) {
        return value instanceof ConversationEntry;
    }

    public Object revert(Object value) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	ConversationEntry convEntry = (ConversationEntry)value;
    	map.put("id", convEntry.getId());
    	map.put("description", convEntry.getDescription());
    	map.put("startDatetime", convEntry.getStartDatetime());
    	map.put("lastDatetime", convEntry.getLastDatetime());
    	map.put("lastRequestTime", convEntry.getLastRequestTime());
        return map;
    }

}
