/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

/**
 * @author William DRAI
 */
public class DefaultNotifyingValidator implements NotifyingValidator {
    
    private final Validator validator;
    private final ValidationNotifier validationNotifier;
    
    public DefaultNotifyingValidator(Validator validator, ValidationNotifier validationNotifier) {
        this.validator = validator;
        this.validationNotifier = validationNotifier;
    }
    
    @Override
    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
        
        Set<ConstraintViolation<?>> genericsAreAPainInTheAss = new HashSet<ConstraintViolation<?>>(constraintViolations.size());
        genericsAreAPainInTheAss.addAll(constraintViolations);
        
        notifyConstraintViolations(object, genericsAreAPainInTheAss);
        
        return constraintViolations;
    }
    
    @SuppressWarnings("unchecked")
    public <T> void notifyConstraintViolations(T root, Set<ConstraintViolation<?>> constraintViolations) {
        Map<Object, Set<ConstraintViolation<Object>>> violationsMap = new HashMap<Object, Set<ConstraintViolation<Object>>>();
        
        for (ConstraintViolation<?> violation : constraintViolations) {
            Object rootBean = violation.getRootBean();
            Object leafBean = violation.getLeafBean();
            Object bean = leafBean != null ? leafBean : rootBean;
            
            Set<ConstraintViolation<Object>> violations = violationsMap.get(bean);
            if (violations == null) {
                violations = new HashSet<ConstraintViolation<Object>>();
                violationsMap.put(bean, violations);
            }           
            violations.add((ConstraintViolation<Object>)violation);
        }
        
        for (Object bean : violationsMap.keySet()) {
            if (bean != root)
                validationNotifier.notifyConstraintViolations(bean, violationsMap.get(bean));
        }
        
        validationNotifier.notifyConstraintViolations(root, violationsMap.get(root));
    }
    
    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
        return validator.validateProperty(object, propertyName, groups);
    }
    
    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        return validator.validateValue(beanType, propertyName, value, groups);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> entityClass) {
        return validator.getConstraintsForClass(entityClass);
    }

    @Override
    public <T> T unwrap(Class<T> entityClass) {
        return validator.unwrap(entityClass);
    }
    
    
    @Override
    public <T> void addConstraintViolationsHandler(T entity, ConstraintViolationsHandler<T> handler) {
        validationNotifier.addConstraintViolationsHandler(entity, handler);
    }

    @Override
    public <T> void removeConstraintViolationsHandler(T entity, ConstraintViolationsHandler<T> handler) {
        validationNotifier.removeConstraintViolationsHandler(entity, handler);
    }


}
