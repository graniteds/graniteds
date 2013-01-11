package org.granite.test.config;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.util.ClassUtil;

public class TestDateConverter extends Converter implements Reverter {

	public TestDateConverter(Converters converters) {
		super(converters);
	}
	
	@Override
	protected boolean internalCanConvert(Object value, Type targetType) {
        Class<?> targetClass = ClassUtil.classOfType(targetType);
        return (
            targetClass.isAssignableFrom(Date.class) ||
            targetClass.isAssignableFrom(Calendar.class) ||
            targetClass.isAssignableFrom(java.sql.Timestamp.class) ||
            targetClass.isAssignableFrom(java.sql.Time.class) ||
            targetClass.isAssignableFrom(java.sql.Date.class)
        ) && (
            value == null || value instanceof Date
        );
	}

	@Override
	protected Object internalConvert(Object value, Type targetType) {
        if (value == null)
            return null;

        Date date = (Date)value;
        return new Long(date.getTime());

	}

	@Override
	public boolean canRevert(Object value) {
		return value instanceof Long;
	}

	@Override
	public Object revert(Object value) {
		return new Date((Long)value);
	}
	
}