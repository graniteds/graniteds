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

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * @author William DRAI
 */
public class DefaultNotifyingValidatorFactory implements NotifyingValidatorFactory {
    
    private final ValidatorFactory validatorFactory;
    private final ValidationNotifier validationNotifier = new ValidationNotifier();
    
    public DefaultNotifyingValidatorFactory(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    public NotifyingValidator getValidator() {
        return new DefaultNotifyingValidator(validatorFactory.getValidator(), validationNotifier);
    }

    public ValidatorContext usingContext() {
        return validatorFactory.usingContext();
    }

    public MessageInterpolator getMessageInterpolator() {
        return validatorFactory.getMessageInterpolator();
    }

    public TraversableResolver getTraversableResolver() {
        return validatorFactory.getTraversableResolver();
    }

    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return validatorFactory.getConstraintValidatorFactory();
    }

    public <T> T unwrap(Class<T> type) {
        return validatorFactory.unwrap(type);
    }

}
