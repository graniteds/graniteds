/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
