/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

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

package org.granite.tide.hibernate;

import java.util.concurrent.ConcurrentHashMap;

import org.granite.tide.validators.EntityValidator;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;


public class HibernateValidator implements EntityValidator {
    
    private ConcurrentHashMap<Class<?>, ClassValidator<?>> validators = new ConcurrentHashMap<Class<?>, ClassValidator<?>>(20);
    
    
    public HibernateValidator() {
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public org.granite.tide.validators.InvalidValue[] getPotentialInvalidValues(Class<?> entityClass, String propertyName, Object value) {
        ClassValidator<?> validator = validators.get(entityClass);
        if (validator == null) {
            validator = new ClassValidator(entityClass);
            ClassValidator<?> tmpValidator = validators.putIfAbsent(entityClass, validator); 
            if (tmpValidator != null) 
            	validator = tmpValidator; 
        }
        
        org.hibernate.validator.InvalidValue[] invalidValues = validator.getPotentialInvalidValues(propertyName, value);
        return convertInvalidValues(invalidValues);
    }


    public static org.granite.tide.validators.InvalidValue[] convertInvalidValues(InvalidValue[] values) {
        org.granite.tide.validators.InvalidValue[] converted = new org.granite.tide.validators.InvalidValue[values.length];
        for (int i = 0; i < values.length; i++) {
            InvalidValue value = values[i];
            if (value.getBean() == null) {
                converted[i] = new org.granite.tide.validators.InvalidValue(
                    value.getBeanClass(),
                    value.getPropertyPath(),
                    value.getValue(),
                    value.getMessage()
                );
            }
            else {
                converted[i] = new org.granite.tide.validators.InvalidValue(
                    value.getRootBean() != null ? value.getRootBean() : value.getBean(),
                    value.getPropertyPath(),
                    value.getValue(),
                    value.getMessage()
                );
            }
        }
        return converted;
    }
}
