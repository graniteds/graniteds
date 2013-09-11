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

package org.granite.tide.spring;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class SpringValidationExceptionConverter implements ExceptionConverter {
    
    public static final String VALIDATION_FAILED = "Validation.Failed";
    

    public boolean accepts(Throwable t, Throwable finalException) {
        return t.getClass().equals(SpringValidationException.class);
    }

    public ServiceException convert(Throwable t, String detail, Map<String, Object> extendedData) {
    	Errors errors = ((SpringValidationException)t).getErrors();
        extendedData.put("invalidValues", convertErrors(errors));
        
        ServiceException se = new ServiceException(VALIDATION_FAILED, t.getMessage(), detail, t);
        se.getExtendedData().putAll(extendedData);
        return se;
    }

    
    public static org.granite.tide.validators.InvalidValue[] convertErrors(Errors errors) {
    	Object bean = null;
        GraniteContext context = GraniteContext.getCurrentInstance();
        ServletContext sc = ((HttpGraniteContext)context).getServletContext();
        ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(sc);
    	
    	BindingResult bindingResult = null;
    	if (errors instanceof BindingResult) {
    		bindingResult = (BindingResult)errors;
    		bean = bindingResult.getTarget();
    	}
    	
        org.granite.tide.validators.InvalidValue[] converted = new org.granite.tide.validators.InvalidValue[errors.getErrorCount()];
    	List<? extends ObjectError> allErrors = errors.getAllErrors();
    	int i = 0;
    	for (ObjectError error : allErrors) {
    		if (error instanceof FieldError) {
    			FieldError ferror = (FieldError)error;
    			converted[i++] = new org.granite.tide.validators.InvalidValue(bean != null ? bean : ferror.getObjectName(), 
    					ferror.getField(),
    					ferror.getRejectedValue(),
    					springContext.getMessage(ferror, Locale.FRENCH));    					
    		}
    		else {
    			converted[i++] = new org.granite.tide.validators.InvalidValue(bean != null ? bean : error.getObjectName(), 
    					null,
    					null,
    					springContext.getMessage(error, Locale.FRENCH));    					
    		}
    	}
        return converted;
    }
}
