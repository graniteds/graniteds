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

package org.granite.tide.validation;

import java.util.Map;
import java.util.Set;

import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceException;
import javax.validation.ConstraintViolationException;
import javax.validation.ConstraintViolation;

/**
 * @author William DRAI
 */
public class BeanValidationExceptionConverter implements ExceptionConverter {
    
    public static final String VALIDATION_FAILED = "Validation.Failed";
    
    public boolean accepts(Throwable t, Throwable finalException) {
        return t.getClass().equals(ConstraintViolationException.class);
    }

    public ServiceException convert(Throwable t, String detail, Map<String, Object> extendedData) {
        Set<ConstraintViolation<?>> constraintViolations = ((ConstraintViolationException)t).getConstraintViolations();
        extendedData.put("invalidValues", BeanValidation.convertConstraintViolations(constraintViolations));
        
        ServiceException se = new ServiceException(VALIDATION_FAILED, t.getMessage(), detail, t);
        se.getExtendedData().putAll(extendedData);
        return se;
    }

}
