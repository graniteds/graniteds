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
package org.granite.tide.validation;

import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.granite.logging.Logger;
import org.granite.tide.validators.EntityValidator;
import org.granite.tide.validators.InvalidValue;

/**
 * @author William DRAI
 */
public class BeanValidation implements EntityValidator {
	
	private static final Logger log = Logger.getLogger(BeanValidation.class);
    
    private ValidatorFactory validatorFactory = null;
    
    
    public BeanValidation() {
    	try {
	    	InitialContext ic = new InitialContext();
	    	this.validatorFactory = (ValidatorFactory)ic.lookup("java:comp/ValidatorFactory");
    	}
    	catch (NamingException e) {
    		log.info("ValidatorFactory not found in JNDI, build default");
    		this.validatorFactory = Validation.buildDefaultValidatorFactory();
    	}
    }
    
    public BeanValidation(ValidatorFactory validatorFactory) {
    	this.validatorFactory = validatorFactory;
    }
    
    
    @SuppressWarnings("unchecked")
    public InvalidValue[] getPotentialInvalidValues(Class<?> entityClass, String propertyName, Object value) {
        Validator validator = validatorFactory.getValidator();
        
        Set<?> constraintViolations = validator.validateValue(entityClass, propertyName, value);
        return convertConstraintViolations((Set<ConstraintViolation<?>>)constraintViolations);
    }


    public static InvalidValue[] convertConstraintViolations(Set<ConstraintViolation<?>> constraintViolations) {
        InvalidValue[] converted = new org.granite.tide.validators.InvalidValue[constraintViolations.size()];
        int i = 0;
        for (ConstraintViolation<?> cv : constraintViolations) {
            if (cv.getRootBean() == null) {
                converted[i++] = new InvalidValue(
                    cv.getRootBeanClass(),
                    cv.getPropertyPath().toString(),
                    cv.getInvalidValue(),
                    cv.getMessage()
                );
            }
            else {
                converted[i++] = new InvalidValue(
                    cv.getRootBean(),
                    cv.getLeafBean() != null ? cv.getLeafBean() : cv.getRootBean(),
                    cv.getPropertyPath().toString(),
                    cv.getInvalidValue(),
                    cv.getMessage()
                );
            }
        }
        return converted;
    }
}
